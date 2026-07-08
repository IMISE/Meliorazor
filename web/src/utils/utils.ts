import type { LocationQueryValue } from "vue-router";

export function clean(input: string | null | undefined, length: number | undefined = undefined) {
  return (input || "undef")
    .toLowerCase()
    .replaceAll("ä", "ae")
    .replaceAll("ö", "oe")
    .replaceAll("ü", "ue")
    .replaceAll("ß", "ss")
    .replace(/[^a-z0-9-]/gi, "_")
    .replace(/_{2,}/g, "_")
    .replace(/(^_|_$)/g, "")
    .substring(0, length);
}

/**
 * When params contain a comma, vue-router returns it as an array.
 * Sometimes we just want a string though, maybe because we expect only one argument.
 * This turns a `string[]` into a `string` by joining with a comma (`,`) and converts `undefined` to `""`.
 * @param param Value of the parsed parameter.
 * @returns A type-safe string.
 */
export function paramToString(param: string | string[] | undefined): string {
  return typeof param === "object" ? param.join(",") : (param ?? "");
}

/**
 * Sometimes we just want a string[] param, even if there's just one argument.
 * Similiar to {@link paramToString}.
 * @param param Value of the parsed parameter.
 * @returns An array with at least zero elements.
 */

export function queryToStringArray(
  param: LocationQueryValue | LocationQueryValue[] | undefined,
): string[] {
  const arr =
    typeof param === "object" ? (param ?? []) : param !== undefined ? param.split(",") : [];
  return arr.filter((val) => typeof val === "string");
}

export function isUrl(input: string | undefined) {
  if (!input) return false;
  try {
    new URL(input);
    return true;
  } catch {
    return false;
  }
}

export async function isFhirServer(input: string | undefined) {
  if (isUrl(input)) {
    try {
      const result = await fetch(`${input}/metadata`);
      const metadata = await result.json();
      return metadata?.resourceType === "CapabilityStatement";
    } catch (e) {
      return false;
    }
  } else {
    return false;
  }
}
