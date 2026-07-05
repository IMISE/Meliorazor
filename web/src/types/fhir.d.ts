export type Parameters = { resourceType: "Parameters"; parameter: FhirKeyValue[] };
export type Properties = { code: string | undefined; part: FhirKeyValue[] };
export type FhirKeyValue = {
  name: string;
  valueString: string | undefined;
  valueBoolean: boolean | undefined;
  valueInteger: number | undefined;
  valueDecimal: number | undefined;
  valueUri: string | undefined;
  valueCode: string | undefined;
  valueCoding: { system: string; code: string } | undefined;
  valueDateTime: string | undefined;
  part: FhirKeyValue[] | undefined;
};
/** https://fhir.hl7.org/fhir/valueset-operation-validate-code.html */
export type ValidateCode = {
  parameter: FhirKeyValue[];
};

export type ValueSetBundle = {
  fullUrl: string;
  resource: {
    resourceType: "ValueSet";
    id: string;
    meta: unknown;
    name: string | undefined;
    title: string | undefined;
    url: string | undefined;
    status: "preliminary" | "active" | "inactive" | "deleted" | undefined;
    version: string | undefined;
  };
};

export type ValueSetIndex = {
  resourceType: "Bundle";
  // ...
  entry: ValueSetBundle[];
};
