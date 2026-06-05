import type { CodeMapping } from "@/types/types";

export function compareCodes(codeA: CodeMapping, codeB: CodeMapping) {
  return codeA.code === codeB.code && codeA.system === codeB.system;
}
