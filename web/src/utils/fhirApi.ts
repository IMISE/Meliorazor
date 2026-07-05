import type { Parameters, Properties, ValidateCode, ValueSetIndex } from "@/types/fhir";
import type {
  CodeInfo,
  CodeMapping,
  FhirTerminologyCode,
  FhirValueSetDefinition,
  InformedCodeMapping,
} from "@/types/types";
import { isUrl } from "./utils";

async function fetchFhir(url: string, expectedResourceType: string) {
  const response = await fetch(url);
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
  limit: number | undefined = undefined,
): Promise<InformedCodeMapping[]> {
  const response = await fetchFhir(
    `${server}/ValueSet/$expand?url=${valueSet}&filter=${filter}` + (limit !== undefined)
      ? `&limit=${limit}`
      : "",
    "ValueSet",
  );
  return response?.expansion?.contains?.length
    ? response.expansion.contains.map((e: FhirTerminologyCode) => ({ ...e, valueSet }))
    : [];
}

export async function getCode(
  code: string,
  system: string,
  server: string,
): Promise<CodeInfo | undefined> {
  const response = (await fetchFhir(
    `${server}/CodeSystem/$lookup?code=${code}&system=${system}`,
    "Parameters",
  )) as Parameters;
  if (!response.parameter?.length) return undefined;
  return toCodeInfo(code, system, response);
}

export async function findCode(code: string, system: string, servers: string[]) {
  // filter out duplicate servers
  const deduped = [...new Set(servers)];

  // find code on servers
  const promises = deduped.map(async (server) => ({
    params: (await fetchFhir(
      `${server}/CodeSystem/$lookup?code=${code}&system=${system}`,
      "Parameters",
    )) as Parameters,
    server,
  }));
  const firstResult = (await Promise.allSettled(promises))
    // double filtering because typing annotations work better that way
    .filter((result) => result.status === "fulfilled")
    .filter((result) => result.value)[0]?.value;
  return firstResult
    ? { code: toCodeInfo(code, system, firstResult.params), server: firstResult.server }
    : { code: undefined, server: undefined };
}

function toCodeInfo(code: string, system: string, response: Parameters) {
  const info = {} as CodeInfo;
  info.self = {
    code,
    system,
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
