import type { FhirTerminologyCode } from "@/types/fhir";
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
