import type { Role } from "@/types/enums";
import type { Annotatable, Participant, Questionnaire, Variable } from "@/types/types";
import type { LocationQueryValue } from "vue-router";

export function clean(input: string | null | undefined, length: number | undefined = undefined) {
  return (input || "undef")
    .toLowerCase()
    .split("ä")
    .join("ae")
    .split("ö")
    .join("oe")
    .split("ü")
    .join("ue")
    .split("ß")
    .join("ss")
    .replace(/[^a-z0-9-]/gi, "_")
    .replace(/_{2,}/g, "_")
    .replace(/(^_|_$)/g, "")
    .substring(0, length);
}

export function shorten(input: string | null | undefined, length: number): string {
  const cutoff = length < 3 ? 0 : length - 3;
  return (input?.length ?? 0) > cutoff + 3 ? (input ?? "") : `${input!.substring(0, cutoff)}…`;
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

/**
 * Gets the longest string with the specified prefix from a list.
 * @param prefix substring at the start
 * @param strings list of all possible strings/names/..., must not necessarily all start with the prefix
 * @returns the longest string for which the requirements are true
 */
export function getLongestStartingWith(prefix: string, strings: string[]) {
  return strings
    .filter((s) => s.startsWith(prefix))
    .reduce((longest, val) => (val.length > longest.length ? val : longest));
}

export function getAnnotatable(annotationId: number, dataElement: Variable): Annotatable {
  return (
    dataElement.choices?.find((choice) =>
      choice.annotations.some((annotation) => annotation.id === annotationId),
    ) ?? dataElement
  );
}

export function getAnnotatableFromProject(
  annotationId: number,
  project: Questionnaire,
): Annotatable | undefined {
  for (const dataElement of project.dataElements) {
    if (dataElement.annotations.some((annotation) => annotation.id === annotationId)) {
      return dataElement;
    } else if (
      dataElement?.unit?.annotations.some((annotation) => annotation.id === annotationId)
    ) {
      return dataElement.unit;
    } else {
      for (const choice of dataElement.choices ?? []) {
        if (choice.annotations.some((annotation) => annotation.id === annotationId)) {
          return choice;
        }
      }
    }
  }
}

export function download(format: string, filename: string, contents: string) {
  const element = document.createElement("a");
  element.setAttribute("href", `data:${format};charset=utf-8,${encodeURIComponent(contents)}`);
  element.setAttribute("download", filename);

  element.style.display = "none";
  document.body.appendChild(element);

  element.click();

  document.body.removeChild(element);
}

export function hasRole(
  username: string | undefined,
  role: Role,
  participants: Participant[],
): boolean {
  return participants.find((p) => p.username === username)?.roles.includes(role) ?? false;
}
