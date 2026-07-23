import type { FhirKeyValue, FhirTerminologyCode } from "@/types/fhir";
import type { CodeInfo, CodeMapping } from "@/types/types";

export function codeEquals(codeA: CodeMapping | undefined, codeB: CodeMapping | undefined) {
  return (
    codeA === codeB ||
    (codeA !== undefined &&
      codeB !== undefined &&
      codeA.system === codeB.system &&
      (!codeA.systemVersion ||
        !codeB.systemVersion ||
        codeA.systemVersion === "*" ||
        codeB.systemVersion === "*" ||
        codeA.systemVersion === codeB.systemVersion) &&
      codeA.code === codeB.code)
  );
}

export function toFhirCode(codeInfo: CodeInfo): FhirTerminologyCode {
  return { ...codeInfo.self, inactive: codeInfo.inactive || undefined };
}

/**
 * Create a string that can be POSTed as a value set.
 * @param implicit whether the url encodes a codesystem or a valueset
 * @param url url of codesystem or valueset
 * @param version version of resource referenced by url; is omitted if empty
 * @param additionalParameters additional parameters for the object stringified, e.g. `filter`, `includeDesignations` or `count` (see the FHIR docs for the operation you want to do for reference)
 * @returns stringified object that can be POSTed to ValueSet/$expand
 */
export function postValueSet(
  implicit: boolean,
  url: string,
  version: string,
  additionalParameters: FhirKeyValue[] = [],
) {
  return JSON.stringify({
    resourceType: "Parameters",
    parameter: [
      ...(implicit
        ? [
            {
              name: "valueSet",
              resource: {
                resourceType: "ValueSet",
                compose: {
                  include: [
                    {
                      url: url,
                      version: version || undefined,
                    },
                  ],
                },
              },
            },
          ]
        : [
            {
              name: "url",
              valueUri: url,
            },
            ...(version
              ? [
                  {
                    name: "valueSetVersion",
                    valueUri: version,
                  },
                ]
              : []),
          ]),
      ...additionalParameters,
    ],
  });
}
