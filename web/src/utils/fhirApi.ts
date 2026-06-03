import type { Designation, Parameters, Properties, StringValue } from "@/types/fhir";
import type { CodeInfo, CodeMapping, FhirValueSetDefinition, InformedCodeMapping } from "@/types/types";

async function fetchFhir(url: string, expectedResourceType: string) {
  const response = await fetch(url);
  if (!response.ok) return undefined;
  const json = await response.json();
  return json?.resourceType === expectedResourceType ? json : undefined;
}

function getPropertyCodes(properties: Properties[]) {
  return properties.map((e) => e.part.find((f: any) => f.name === "value")?.valueCode).filter((e) => e);
}

export async function existsInValueSet(code: CodeMapping, valueSet: FhirValueSetDefinition): Promise<boolean | undefined> {
  const response = await fetchFhir(`${valueSet.server}/ValueSet/$validate-code?code=${code.code}&system=${code.system}&url=${valueSet.valueSet}`, "Parameters");
  if (!response?.parameter?.length) return undefined;
  return response.parameter.find((x: any) => x.name === "result")?.valueBoolean;
}

export async function expandValueSet(filter: string, valueSet: FhirValueSetDefinition): Promise<InformedCodeMapping[]> {
  const response = await fetchFhir(`${valueSet.server}/ValueSet/$expand?url=${valueSet.valueSet}&filter=${filter}`, "ValueSet");
  return response?.expansion?.contains?.length ? response.expansion.contains.map((e: CodeMapping) => ({ ...e, valueSet })) : [];
}

export async function getCode(code: string, system: string, server: string): Promise<CodeInfo | undefined> {
  const response = await fetchFhir(`${server}/CodeSystem/$lookup?code=${code}&system=${system}`, "Parameters") as Parameters;
  if (!response.parameter?.length) return undefined;
  return toCodeInfo(code, system, response);
}

export async function findCode(code: string, system: string, servers: string[]) {
  // filter out duplicate servers
  const deduped = [...new Set(...servers)];

  // find code on servers
  const promises = deduped.map(async (server) => ({
    params: await fetchFhir(`${server}/CodeSystem/$lookup?code=${code}&system=${system}`, "Parameters") as Parameters,
    server,
  }));
  const firstResult = (await Promise.allSettled(promises))
    // double filtering because typing annotations work better that way
    .filter((result) => result.status === "fulfilled")
    .filter((result) => result.value)[0]?.value;
  return firstResult ?
    { code: toCodeInfo(code, system, firstResult.params), server: firstResult.server }
  : { code: undefined, server: undefined };
}

function toCodeInfo(code: string, system: string, response: Parameters) {
  const info = {} as CodeInfo;
  info.self = {
    code,
    system,
    display: response.parameter.find((x: any) => x.name === "display").valueString,
  };
  const names = response.parameter
    .filter((p) => p.name === "designation" && p?.part?.length)
    .flatMap((des: Designation) => des.part
      .filter((x: any) => x.name === "value")
      .map((x: StringValue) => x.valueString)
    );
  info.alternativeNames = [...new Set(names.filter((name) => name))];
  info.definition = response.parameter.find((p) => p.name === "definition")?.valueString;
  const properties: Properties[] = response.parameter
    .filter((e: any) => e?.name === "property" && e.part?.length >= 2)
    .map((e: any) => ({ code: e.part.find((f: any) => f.name === "code")?.valueCode, part: e.part }));
  info.inactive = properties.filter((e) => e.code === "inactive")?.[0]?.part.find((e) => e.name === "value")?.valueBoolean;
  info.parents = getPropertyCodes(properties.filter((e) => e.code === "parent"));
  info.children = getPropertyCodes(properties.filter((e) => e.code === "child"));
  return info;
}
