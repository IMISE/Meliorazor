import auth from "@/auth";
import type { Ref } from "vue";
import getEnv from "./env";
import type { CreateVersion, Questionnaire } from "@/types/types";

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
    if (response.status === 403) {
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

export async function getProject(
  id: string | number | undefined,
  oldId: string | number | undefined,
  loading: Ref<boolean> | undefined = undefined,
) {
  if ((loading && loading.value) || !id || id === oldId) {
    return;
  } else {
    const response = await request(`/questionnaire/${id}`);
    if (!response?.ok) {
      console.error("Project response not ok :(");
      return;
    }

    return (await response.json()) as Questionnaire | undefined;
  }
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

  return await request(`/questionnaire/${projectId}/version`, "POST", JSON.stringify(version));
}
