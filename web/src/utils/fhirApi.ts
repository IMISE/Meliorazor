import type {
  CodeSystemIndex,
  FhirKeyValue,
  Parameters,
  Properties,
  ValidateCode,
  ValueSetIndex,
  VersionlessFhirTerminologyCodeWithDesignation,
} from "@/types/fhir";
import type {
  CodeInfo,
  CodeMapping,
  FhirValueSetDefinition,
  InformedCodeMapping,
} from "@/types/types";
import { getLongestStartingWith, isUrl } from "./utils";
import { postValueSet } from "./fhirUtils";

async function fetchFhir(
  url: string,
  expectedResourceType: string,
  method: "GET" | "POST" = "GET",
  body: string | undefined = undefined,
  acceptLanguage: string = "en-US",
) {
  const response = await fetchFhirRaw(url, method, body, acceptLanguage);
  return await checkFhirResponse(response, expectedResourceType);
}

async function fetchFhirRaw(
  url: string,
  method: "GET" | "POST" = "GET",
  body: string | undefined = undefined,
  acceptLanguage: string | undefined = "en-US",
) {
  const options = {
    method,
    body,
    headers: {
      "Content-Type": "application/fhir+json",
      "Accept-Language": acceptLanguage,
    },
  };

  return await fetch(url, options);
}

async function checkFhirResponse(response: Response, expectedResourceType: string) {
  if (!response.ok) return undefined;
  const json = await response.json();
  return json?.resourceType === expectedResourceType ? json : undefined;
}

function getPropertyCodes(properties: Properties[]) {
  return properties
    .map((e) => e.part.find((f) => f.name === "value")?.valueCode)
    .filter((e) => e !== undefined);
}

/**
 * Uses FHIR's $validate-code operation on ValueSet (if implicit: CodeSystem) to check if a certain code is contained in the value set.
 * @param code code itself and its system is checked.
 * @param valueSet
 * @returns
 */
export async function existsInValueSet(
  code: CodeMapping,
  valueSet: FhirValueSetDefinition,
): Promise<boolean | undefined> {
  const response: ValidateCode = await fetchFhir(
    `${valueSet.server}/${valueSet.implicit ? "CodeSystem" : "ValueSet"}/$validate-code?code=${code.code}&url=${valueSet.url}` +
      (valueSet.implicit
        ? `&version=${code.systemVersion}`
        : `&system=${code.system}&systemVersion=${code.systemVersion}&valueSetVersion=${valueSet.version}`),
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
      .map(
        (entry) =>
          ({
            implicit: false,
            display: entry.resource!.title ?? entry.resource!.name!,
            server,
            url: entry.resource!.url!,
            version: entry.resource!.version ?? "",
          }) as FhirValueSetDefinition,
      ) ?? []
  );
}

/**
 * Get all code systems of a server.
 * @param server Valid and functional FHIR server to query all Code Systems of.
 * @param keepInactive Whether to keep or scrap inactive Code Systems in the response.
 * @returns An array of all code systems saved as implicit value sets grouped by their name.
 */
export async function allCodeSystemsAsImplicitValueSets(
  server: string,
  keepInactive: boolean = false,
): Promise<{ [index: string]: FhirValueSetDefinition[] }> {
  const response = (await fetchFhir(`${server}/CodeSystem`, "Bundle")) as
    | CodeSystemIndex
    | undefined;
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
      .reduce(
        (acc, val) => {
          // name holds something such as SNOMED_CT, title is title for version
          const index = val.resource!.name ?? val.resource!.title ?? "";
          if (acc[index] === undefined) {
            acc[index] = [];
          }
          acc[index]!.push({
            implicit: true,
            display: val.resource!.title ?? val.resource!.name ?? val.resource.url!,
            server,
            url: val.resource!.url!,
            version: val.resource!.version ?? "",
          });
          return acc;
        },
        {} as { [index: string]: FhirValueSetDefinition[] },
      ) ?? {}
  );
}

export async function expandValueSet(
  search: string,
  valueSet: FhirValueSetDefinition,
  acceptLanguage: string,
  limit: number | undefined = 1000,
) {
  const versionMap: { [index: string]: { version: string; display: string } } = {};
  const _response = await expandValueSetRaw(search, valueSet, true, acceptLanguage, limit);
  const response = await checkFhirResponse(_response, "ValueSet");

  if (response?.expansion?.contains?.length) {
    if (valueSet.implicit) {
      versionMap[valueSet.url] = { version: valueSet.version, display: valueSet.display };
    } else {
      response.expansion.parameter
        ?.filter(
          (entry: FhirKeyValue) =>
            entry.valueUri?.includes("|") && ["used-codesystem", "version"].includes(entry.name),
        )
        .map((entry: FhirKeyValue) => entry.valueUri?.split("|"))
        .forEach((entry: string[]) => {
          versionMap[entry[0]!] = { version: entry[1]!, display: "" };
        });

      // fetch names of codesystem versions
      await Promise.allSettled(
        Object.keys(versionMap).map(async (systemName) => {
          const name = await getCodeSystemName(
            systemName,
            versionMap[systemName]!.version,
            valueSet.server,
          );
          versionMap[systemName]!.display = name ?? "";
        }),
      );
    }
    return response.expansion.contains.map((e: VersionlessFhirTerminologyCodeWithDesignation) => ({
      code: e.code,
      display: getLongestStartingWith(e.display, [
        e.display,
        ...e.designation.map((des) => des.value),
      ]),
      system: e.system,
      systemVersion: versionMap[e.system]!.version,
      inactive: e.inactive,
      valueSet: {
        implicit: valueSet.implicit,
        display: valueSet.display || response.name || "",
        server: valueSet.server,
        url: valueSet.url,
        version: valueSet.version || "", // TODO!!!
      } as FhirValueSetDefinition,
      codeSystemName: versionMap[e.system]!.display,
    })) as InformedCodeMapping[];
  }
}

/**
 * Searches a ValueSet or CodeSystem for a string.
 * See FHIR valueset-operation-expand.
 *
 * CodeSystems are searched by expanding an implicit ValueSet containing the entire CodeSystem.
 * $find-matches was considered but wasn't ready yet at this time (FHIR r4, r5 and draft of r6).
 *
 * For explicit ValueSets, this could have been a GET query; this was decided against to just have one method dealing with all this.
 *
 * @param search search value to expand the value set with
 * @param valueSet value set to query
 * @param includeDesignations include different names of the codes in the query result?
 * @param acceptLanguage try to force language to this string; TODO: format unclear
 * @param limit number of codes to return; must be >= 0. If = 0, just the count is returned.
 * @returns the raw response without any parsing.
 */
export async function expandValueSetRaw(
  search: string | undefined,
  valueSet: FhirValueSetDefinition,
  includeDesignations: boolean,
  acceptLanguage: string | undefined,
  limit: number | undefined = 1000,
) {
  return await fetchFhirRaw(
    `${valueSet.server}/ValueSet/$expand`,
    "POST",
    postValueSet(valueSet.implicit, valueSet.url, valueSet.version, [
      ...(search?.length
        ? [
            {
              name: "filter",
              valueString: search,
            },
          ]
        : []),
      {
        name: "includeDesignations",
        valueBoolean: includeDesignations,
      },
      ...(limit !== undefined && limit >= 0
        ? [
            {
              name: "count",
              valueInteger: limit,
            },
          ]
        : []),
    ] as FhirKeyValue[]),
    acceptLanguage,
  );
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
