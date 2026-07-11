import type { FhirTerminologyCode } from "@/types/fhir";
import type { CodeInfo } from "@/types/types";

export function codeEquals(codeA: FhirTerminologyCode, codeB: FhirTerminologyCode) {
  return (
    codeA.system === codeB.system &&
    (!codeA.systemVersion ||
      !codeB.systemVersion ||
      codeA.systemVersion === "*" ||
      codeB.systemVersion === "*" ||
      codeA.systemVersion === codeB.systemVersion) &&
    codeA.code === codeB.code
  );
}

export function toFhirCode(codeInfo: CodeInfo): FhirTerminologyCode {
  return { ...codeInfo.self, inactive: codeInfo.inactive || undefined };
}
