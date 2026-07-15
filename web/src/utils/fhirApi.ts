import type {
  FhirKeyValue,
  FhirValueSetDefinition,
  Parameters,
  Properties,
  ValidateCode,
  ValueSetIndex,
  VersionlessFhirTerminologyCodeWithDesignation,
} from "@/types/fhir";
import type { CodeInfo, CodeMapping, InformedCodeMapping } from "@/types/types";
import { getLongestStartingWith, isUrl } from "./utils";

async function fetchFhir(
  url: string,
  expectedResourceType: string,
  acceptLanguage: string = "en-US",
) {
  const headers = {
    "Content-Type": "application/json",
    "Accept-Language": acceptLanguage,
  };

  const response = await fetch(url, { headers });
  if (!response.ok) return undefined;
  const json = await response.json();
  return json?.resourceType === expectedResourceType ? json : undefined;
}

function getPropertyCodes(properties: Properties[]) {
  return properties
    .map((e) => e.part.find((f) => f.name === "value")?.valueCode)
    .filter((e) => e !== undefined);
}

export async function existsInValueSet(
  code: CodeMapping,
  valueSet: FhirValueSetDefinition,
): Promise<boolean | undefined> {
  const response: ValidateCode = await fetchFhir(
    `${valueSet.server}/ValueSet/$validate-code?code=${code.code}&system=${code.system}&url=${valueSet.valueSet}`,
    "Parameters",
  );
  if (!response?.parameter?.length) return undefined;
  return response.parameter.find((x) => x.name === "result")?.valueBoolean;
}

export async function allValueSets(
  server: string,
  keepInactive: boolean = false,
): Promise<FhirValueSetDefinition[]> {
  const response = (await fetchFhir(`${server}/ValueSet`, "Bundle")) as ValueSetIndex | undefined;
  return (
    response?.entry
      .filter(
        (entry) =>
          entry?.resource &&
          // if someone wants an inactive or deleted valueset, they can add it themselves
          (keepInactive || ["preliminary", "active", undefined].includes(entry.resource.status)) &&
          (entry.resource.title || entry.resource.name) &&
          isUrl(entry.resource.url),
      )
      .map((entry) => ({
        display: entry.resource!.title ?? entry.resource!.name!,
        server,
        valueSet: entry.resource!.url!,
      })) ?? []
  );
}

export async function expandValueSet(
  filter: string,
  server: string,
  valueSet: string,
  acceptLanguage: string,
  limit: number | undefined = 1000,
): Promise<InformedCodeMapping[]> {
  const response = await fetchFhir(
    `${server}/ValueSet/$expand?url=${valueSet}&filter=${filter}&includeDesignations=true` +
      (limit !== undefined ? `&limit=${limit}` : ""),
    "ValueSet",
    acceptLanguage,
  );
  if (response?.expansion?.contains?.length) {
    // build CodeSystem => Version map
    const versionMap: { [index: string]: { version: string; name: string } } = {};
    response.expansion.parameter
      ?.filter(
        (entry: FhirKeyValue) =>
          entry.valueUri?.includes("|") && ["used-codesystem", "version"].includes(entry.name),
      )
      .map((entry: FhirKeyValue) => entry.valueUri?.split("|"))
      .forEach((entry: string[]) => {
        versionMap[entry[0]!] = { version: entry[1]!, name: "" };
      });

    // fetch names of codesystem versions
    await Promise.allSettled(
      Object.keys(versionMap).map(async (systemName) => {
        const name = await getCodeSystemName(systemName, versionMap[systemName]!.version, server);
        versionMap[systemName]!.name = name ?? "";
      }),
    );

    return response.expansion.contains.map((e: VersionlessFhirTerminologyCodeWithDesignation) => ({
      code: e.code,
      display: getLongestStartingWith(e.display, [
        e.display,
        ...e.designation.map((des) => des.value),
      ]),
      system: e.system,
      systemVersion: versionMap[e.system]!.version,
      inactive: e.inactive,
      valueSet: { display: "", server, valueSet } as FhirValueSetDefinition,
      codeSystemName: versionMap[e.system]!.name,
    })) as InformedCodeMapping[];
  } else return [];
}

export async function getCode(
  code: string,
  system: string,
  version: string,
  server: string,
): Promise<CodeInfo | undefined> {
  const response = (await fetchFhir(
    `${server}/CodeSystem/$lookup?code=${code}&system=${system}`,
    "Parameters",
  )) as Parameters;
  if (!response.parameter?.length) return undefined;
  return toCodeInfo(code, system, version, response);
}

export async function getCodeSystemName(
  system: string,
  version: string,
  server: string,
): Promise<string | undefined> {
  return (await fetchFhir(`${server}/CodeSystem?url=${system}&version=${version}`, "Bundle"))
    ?.entry?.[0]?.resource?.title;
}

export async function findCode(code: string, system: string, version: string, servers: string[]) {
  // filter out duplicate servers
  const deduped = [...new Set(servers)];

  // find code on servers
  const promises = deduped.map(async (server) => ({
    params: (await fetchFhir(
      `${server}/CodeSystem/$lookup?code=${code}&system=${system}&version=${version}`,
      "Parameters",
    )) as Parameters,
    server,
  }));
  const firstResult = (await Promise.allSettled(promises))
    // double filtering because typing annotations work better that way
    .filter((result) => result.status === "fulfilled")
    .filter((result) => result.value)[0]?.value;
  return firstResult
    ? { code: toCodeInfo(code, system, version, firstResult.params), server: firstResult.server }
    : { code: undefined, server: undefined };
}

function toCodeInfo(code: string, system: string, version: string, response: Parameters) {
  const info = {} as CodeInfo;
  info.self = {
    code,
    system,
    systemVersion: version,
    display: response.parameter.find((x) => x.name === "display")?.valueString ?? "",
  };
  const names = response.parameter
    .filter((p) => p.name === "designation" && p?.part?.length)
    .flatMap((des) => des.part?.filter((x) => x.name === "value").map((x) => x.valueString))
    .filter((x) => x !== undefined);
  info.alternativeNames = [...new Set(names.filter((name) => name))];
  info.definition = response.parameter.find((p) => p.name === "definition")?.valueString ?? "";
  const properties: Properties[] = response.parameter
    .filter((e) => e?.name === "property" && (e.part?.length ?? 0) >= 2)
    .map((e) => ({ code: e.part?.find((f) => f.name === "code")?.valueCode, part: e.part ?? [] }));
  info.inactive =
    properties.filter((e) => e.code === "inactive")?.[0]?.part.find((e) => e.name === "value")
      ?.valueBoolean ?? false;
  info.parents = getPropertyCodes(properties.filter((e) => e.code === "parent"));
  info.children = getPropertyCodes(properties.filter((e) => e.code === "child"));
  return info;
}
