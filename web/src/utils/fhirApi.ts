import type { CodeInfo, CodeMapping, FhirValueSetDefinition, InformedCodeMapping } from "@/stores/types";

async function fetchFhir(url: string, expectedResourceType: string) {
  const response = await fetch(url);
  if (!response.ok) return undefined;
  const json = await response.json();
  return json?.resourceType === expectedResourceType ? json : undefined;
}

function getPropertyCodes(properties: { code: string | undefined, part: any[] }[]) {
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
  const response = await fetchFhir(`${server}/CodeSystem/$lookup?code=${code}&system=${system}`, "Parameters") as { resourceType: "Parameters", parameter: any[] };
  if (!response.parameter?.length) return undefined;
  const info = {} as CodeInfo;
  info.self = {
    code,
    system,
    display: response.parameter.find((x: any) => x.name === "display").valueString,
  };
  const names = response.parameter.filter((p) => p.name === "designation" && p?.part?.length).flatMap((des: { name: "designation", part: any[] }) => des.part.filter((x: any) => x.name === "value" ).map((x: { name: "value", valueString: string }) => x.valueString));
  info.alternativeNames = [...new Set(names.filter((name) => name))];
  info.definition = response.parameter.find((p) => p.name === "definition")?.valueString;
  const properties: { code: string | undefined, part: any[] }[] = response.parameter
    .filter((e: any) => e?.name === "property" && e.part?.length >= 2)
    .map((e: any) => ({ code: e.part.find((f: any) => f.name === "code")?.valueCode, part: e.part }));
  info.inactive = properties.filter((e) => e.code === "inactive")?.[0]?.part.find((e) => e.name === "value")?.valueBoolean;
  info.parents = getPropertyCodes(properties.filter((e) => e.code === "parent"));
  info.children = getPropertyCodes(properties.filter((e) => e.code === "child"));
  return info;
}
