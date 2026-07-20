import auth from "@/auth";
import getEnv from "./env";
import type {
  Annotation,
  CreateAnnotation,
  CreateVersion,
  Questionnaire,
  RichAction,
} from "@/types/types";

export default async function request(
  path: string,
  method: "DELETE" | "GET" | "POST" | "PUT" = "GET",
  body: string | undefined = undefined,
) {
  try {
    const u = await auth.getUser();
    if (!u?.access_token) return;

    const options = {
      method,
      body,
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${u.access_token}`,
      },
    };
    const response = await fetch(
      getEnv("VITE_API_BASE") + (path.startsWith("/") ? "" : "/") + path,
      options,
    );
    if (response.status === 401) {
      await auth.signoutRedirect();
      return;
    } else {
      return response;
    }
  } catch (e) {
    // TODO: error handling
    console.error(e);
  }
}

/**
 * Get a single project, if the IDs changed.
 * @param id new ID, required
 * @param oldId old ID, if undefined this does nothing
 * @returns single questionnaire or undefined if nothing changed or ID undefined
 */
export async function getProject(
  id: string | number | undefined,
  oldId: string | number | undefined = undefined,
) {
  if ((oldId !== undefined && id === oldId) || id === undefined) {
    return;
  } else {
    const response = await request(`/questionnaire/${id}`, "GET");
    if (!response?.ok) {
      console.error("Project response not ok :(");
      return;
    }

    return (await response.json()) as Questionnaire | undefined;
  }
}

/**
 * Get project index.
 * @returns All questionnaires seen by this user
 */
export async function getProjects() {
  const response = await request("/questionnaire", "GET");
  if (!response?.ok) {
    console.error("Project response not ok :(");
    return;
  }

  return ((await response.json()) ?? []) as Questionnaire[];
}

export async function getRecentActions(username: string) {
  const response = await request(`/user/${username}/actions/recent`, "GET");
  if (!response?.ok) {
    console.error("Users response not ok :(");
    return;
  }

  return ((await response.json()) ?? []) as RichAction[];
}

export async function getVersionKeys(projectId: string | number | undefined) {
  const response = await request(`/questionnaire/${projectId}/version`);
  if (!response?.ok) {
    console.error("Versions response not ok :(");
    return;
  }

  return (await response.json()) as string[];
}

export async function createVersion(projectId: number, version: CreateVersion) {
  return await request(`/questionnaire/${projectId}/version`, "POST", JSON.stringify(version));
}

export async function createAnnotation(annotation: CreateAnnotation, annotatableId: number) {
  return await request(
    `/annotatable/${annotatableId}/annotation`,
    "POST",
    JSON.stringify(annotation),
  );
}

export async function getAnnotation(
  annotatableId: number | undefined,
  annotationId: number | undefined,
) {
  const response = await request(`/annotatable/${annotatableId}/annotation/${annotationId}`, "GET");
  if (!response?.ok) {
    console.error("Annotation response not ok :(");
    return;
  }

  return (await response.json()) as Annotation;
}
