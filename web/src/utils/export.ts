import type {
  Annotatable,
  AnnotationComment,
  AnnotationSubmission,
  AnnotationVersioning,
  NormalAnnotation,
  Questionnaire,
} from "@/types/types";
import getEnv from "./env";
import { clean } from "./utils";
import { CommentType } from "@/types/enums";

function getAnnotatables(questionnaire: Questionnaire): Annotatable[] {
  return questionnaire.dataElements.flatMap((dataElement) => [
    dataElement,
    ...(dataElement.unit ? [dataElement.unit] : []),
    ...(dataElement.choices?.length ? dataElement.choices : []),
  ]);
}

function annotationToSssomMapping(
  projectUrl: string,
  elementId: number,
  elementLabel: string,
  annotation: NormalAnnotation,
  versionKey: string | undefined = undefined,
) {
  const submissions = annotation.actions.filter(
    (action) => action.actionType === "submission",
  ) as AnnotationSubmission[];
  const comments = annotation.actions.filter(
    (action) => action.actionType === "comment",
  ) as AnnotationComment[];
  const reviews = comments.filter((action) => action.commentType !== CommentType.NOTE);
  return {
    record_id: `${projectUrl}/elements/${elementId}/annotations/${annotation.id}`,
    subject_id: `${projectUrl}/elements/${elementId}`,
    subject_label: elementLabel,
    predicate_id: annotation.predicate,
    // predicate_label:
    predicate_modifier: annotation.negate ? "Not" : undefined,
    // TODO: check when this is incorrect
    object_id: `${annotation.code.system}/${annotation.code.code}`,
    object_label: annotation.code.display,
    // TODO: check if semapv needs to be defined
    mapping_justification: "semapv:ManualMappingCuration",
    // author_id:
    author_label: submissions.map((action) => action.authorName),
    // reviewer_id:
    reviewer_label: reviews.length ? reviews.map((action) => action.authorName) : undefined,
    // ...
    mapping_date: submissions.map((action) => new Date(action.submittedAt)).sort()[0] ?? undefined,
    // publication_date:
    review_date: annotation.actions.filter(
      (action) =>
        action.actionType === "versioning" &&
        (action as AnnotationVersioning).versionKey === versionKey,
    )[0]!.submittedAt,
    confidence:
      submissions.map((action) => action.confidence).reduce((acc, val) => acc + val, 0) /
      submissions.length,
    // TODO: should we rather have a confidence slider for reviewers as well?
    reviewer_agreement: reviews.length
      ? reviews.filter((action) => action.commentType === CommentType.ACCEPT).length /
        reviews.length
      : undefined,
    // ...
  };
}

export function exportToSssomMappingSetObject(questionnaire: Questionnaire, versionKey: string) {
  const projectUrl = `${getEnv("BASE_URL")}/projects/${questionnaire.id}-${clean(questionnaire.title)}`;
  return {
    sssom_version: "1.1",
    mappings: getAnnotatables(questionnaire)
      .filter((element) => element.annotations?.length)
      .flatMap((element) =>
        element.annotations
          .filter(
            (annotation) =>
              annotation.code !== null &&
              annotation.actions.some(
                (action) =>
                  action.actionType === "versioning" &&
                  (action as AnnotationVersioning).versionKey === versionKey,
              ),
          )
          .map((annotation) =>
            annotationToSssomMapping(
              projectUrl,
              element.id!,
              element.label,
              annotation as NormalAnnotation,
              versionKey,
            ),
          ),
      ),
    mapping_set_id: `${projectUrl}/version/${versionKey}`,
    mapping_set_title: `${questionnaire.title} ${versionKey}`,
    mapping_set_description: questionnaire.description,
    // mapping_set_confidence:
    // creator_id:
    // creator_label:
    // license:
    // ...
    mapping_tool: "https://github.com/IMISE/Meliorazor",
    // mapping_tool_id:
    // mapping_tool_version:
    // ...
  };
}

export function exportToSssomJson(questionnaire: Questionnaire, versionKey: string) {
  return JSON.stringify(exportToSssomMappingSetObject(questionnaire, versionKey));
}

export function exportToSssomTsv(questionnaire: Questionnaire, versionKey: string) {
  const mappingSet = exportToSssomMappingSetObject(questionnaire, versionKey);
  const metadata = Object.keys(mappingSet)
    .filter((key) => key !== "mappings" && mappingSet[key as keyof typeof mappingSet] !== undefined)
    .map((key) => `# ${key}: ${mappingSet[key as keyof typeof mappingSet]}`);
  const mappingsKeys = [...new Set(mappingSet.mappings.flatMap((mapping) => Object.keys(mapping)))];
  const mappingsHeaders = mappingsKeys.join("\t");
  const mappingsContents = mappingSet.mappings.map((mapping) =>
    mappingsKeys
      .map((key) => {
        const val = mapping[key as keyof typeof mapping];
        if (val === undefined) {
          return "";
        } else if (typeof val === "object") {
          if ((val as string[]).length !== undefined) {
            // we have a string[]
            return (val as string[])
              .map((entry) => '"' + entry.replaceAll("|", String.raw`\|`) + '"')
              .join("|");
          } else {
            return `"${Date.toString()}"`;
          }
        } else {
          return '"' + val.toString().replaceAll("|", String.raw`\|`) + '"';
        }
      })
      .join("\t"),
  );
  return `${metadata}\n${mappingsHeaders}\n${mappingsContents}`;
}
