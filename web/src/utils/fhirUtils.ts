import type { FhirTerminologyCode } from "@/types/fhir";
import type { CodeInfo, CodeMapping } from "@/types/types";

export function codeEquals(codeA: CodeMapping, codeB: CodeMapping) {
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
