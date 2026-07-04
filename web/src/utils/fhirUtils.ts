import type { CodeInfo, FhirTerminologyCode } from "@/types/types";

export function codeEquals(codeA: FhirTerminologyCode, codeB: FhirTerminologyCode) {
  return codeA.system === codeB.system && codeA.code === codeB.code;
}

export function toFhirCode(codeInfo: CodeInfo): FhirTerminologyCode {
  return { ...codeInfo.self, inactive: codeInfo.inactive || undefined };
}
