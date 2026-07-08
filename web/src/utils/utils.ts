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
