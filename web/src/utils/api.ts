import auth from "@/auth";
import type { Ref } from "vue";
import getEnv from "./env";
import type { Annotation, CreateAnnotation, CreateVersion, Questionnaire } from "@/types/types";

export default async function request(
  path: string,
  method: "DELETE" | "GET" | "POST" | "PUT" = "GET",
  body: string | undefined = undefined,
  loading: Ref<boolean> | undefined = undefined,
) {
  try {
    if (loading) {
      loading.value = true;
    }
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
  } finally {
    if (loading) {
      loading.value = false;
    }
  }
}

/**
 * Get a single project, if the IDs changed.
 * @param id new ID, required
 * @param oldId old ID, if undefined this does nothing
 * @param loading loading state, mutated by this
 * @returns single questionnaire or undefined if nothing changed or ID undefined
 */
export async function getProject(
  id: string | number | undefined,
  oldId: string | number | undefined,
  loading: Ref<boolean> | undefined = undefined,
) {
  if ((loading && loading.value) || (oldId !== undefined && (!id || id === oldId))) {
    return;
  } else {
    const response = await request(`/questionnaire/${id}`, "GET", undefined, loading);
    if (!response?.ok) {
      console.error("Project response not ok :(");
      return;
    }

    return (await response.json()) as Questionnaire | undefined;
  }
}

/**
 * Get project index.
 * @param loading loading state, mutated by this
 * @returns All questionnaires seen by this user
 */
export async function getProjects(loading: Ref<boolean> | undefined = undefined) {
  if (loading && loading.value) return;

  const response = await request("/questionnaire", "GET", undefined, loading);
  if (!response?.ok) {
    console.error("Project response not ok :(");
    return;
  }

  return ((await response.json()) ?? []) as Questionnaire[];
}

export async function getVersionKeys(
  projectId: string | number | undefined,
  loading: Ref<boolean> | undefined = undefined,
) {
  if ((loading && loading.value) || !projectId) {
    return;
  } else {
    const response = await request(`/questionnaire/${projectId}/version`);
    if (!response?.ok) {
      console.error("Versions response not ok :(");
      return;
    }

    return (await response.json()) as string[];
  }
}

export async function createVersion(
  projectId: number,
  version: CreateVersion,
  loading: Ref<boolean> | undefined = undefined,
) {
  if (loading && loading.value) return;

  return await request(
    `/questionnaire/${projectId}/version`,
    "POST",
    JSON.stringify(version),
    loading,
  );
}

export async function createAnnotation(
  annotation: CreateAnnotation,
  annotatableId: number,
  loading: Ref<boolean> | undefined = undefined,
) {
  if (loading && loading.value) {
    return;
  }

  return await request(
    `/annotatable/${annotatableId}/annotation`,
    "POST",
    JSON.stringify(annotation),
    loading,
  );
}

export async function getAnnotation(
  annotatableId: number | undefined,
  annotationId: number | undefined,
  loading: Ref<boolean> | undefined,
) {
  if ((loading && loading.value) || annotatableId === undefined || annotationId === undefined)
    return;

  const response = await request(
    `/annotatable/${annotatableId}/annotation/${annotationId}`,
    "GET",
    undefined,
    loading,
  );
  if (!response?.ok) {
    console.error("Annotation response not ok :(");
    return;
  }

  return (await response.json()) as Annotation;
}
