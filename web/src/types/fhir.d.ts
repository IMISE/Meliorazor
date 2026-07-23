import type { CodeMapping } from "./types";

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

export type Designation = {
  language: string;
  use: Code;
  value: string;
};

export type VersionlessFhirTerminologyCode = CodeMapping & { inactive: true | undefined };
export type VersionlessFhirTerminologyCodeWithDesignation = VersionlessFhirTerminologyCode & {
  designation: Designation[];
};
export type FhirTerminologyCode = VersionlessFhirTerminologyCode & { systemVersion: string };

/** https://fhir.hl7.org/fhir/valueset-operation-validate-code.html */
export type ValidateCode = {
  parameter: FhirKeyValue[];
};

export type ValueSetBundleItem = {
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

export type CodeSystemBundleItem = {
  fullUrl: string;
  resource: {
    resourceType: "CodeSystem";
    id: string;
    meta: unknown;
    /** TODO: I don't know what this is */
    identifier: {
      system: string;
      value: string;
    };
    version: string | undefined;
    name: string | undefined;
    title: string | undefined;
    url: string | undefined;
    experimental: boolean;
    status: "preliminary" | "active" | "inactive" | "deleted" | undefined; // TODO
    date: string;
    publisher: string;
    caseSensitive: boolean;
    valueSet: string;
    compositional: boolean;
    versionNeeded: boolean;
    content: unknown;
    hierarchyMeaning: string;
    filter: {
      code: string;
      description: string;
      operator: string[];
      value: string;
    }[];
    property: {
      code: string;
      description: string;
      type: string;
    };
  };
};

export type ValueSetIndex = {
  resourceType: "Bundle";
  // ...
  entry: ValueSetBundleItem[];
};

export type CodeSystemIndex = {
  resourceType: "Bundle";
  // ...
  entry: CodeSystemBundleItem[];
};
