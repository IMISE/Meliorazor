import type { CommentType, Role, Status } from "./enums";
import type { FhirTerminologyCode } from "./fhir";

export interface Group {
  id: number;
  name: string;
  description: string;
  website: string | null | undefined;
  participants: Participant[];
  invitees: Participant[];
  subGroups: Group[];
  allowedServers: ServerDefinition[];
  projects: Questionnaire[];
}

export interface CreateGroup {
  name: string;
  description: string;
  website: string | null | undefined;
  participants: Participant[];
  parentGroupId: number | null;
}

export interface GroupInvitation {
  id: number;
  names: string[];
  description: string;
  website: string | null;
}

export type ServerDefinition = string;

export interface Questionnaire {
  id: number;
  title: string;
  description: string;
  dataElements: Variable[];
  options: QuestionnaireOptions;
  participants: Participant[];
  valueSets: FhirValueSetDefinition[];
}

export interface QuestionnaireOut {
  id?: number | null | undefined;
  title: string | null | undefined;
  description: string | null | undefined;
  dataElements: Variable[] | null | undefined;
  options: QuestionnaireOptions | null | undefined;
  valueSets: FhirValueSetDefinition[] | null | undefined;
  groupId: number | null | undefined;
}

export type FhirValueSetDefinition = {
  implicit: boolean;
  display: string;
  server: string;
  url: string;
  version: string;
};

export interface QuestionnaireOptions {
  reqNegReviews: number;
  reqPosReviews: number;
  reqNegReviewRatio: number;
  reqPosReviewRatio: number;
}

export interface Participant {
  username: string;
  roles: Role[];
}

export interface Annotatable {
  id: number | undefined;
  annotations: Annotation[];
  label: string;
}

export interface Variable extends Annotatable {
  variableName: string | null | undefined;
  fieldType: string | null | undefined;
  choices: Choice[] | null | undefined;
  unit: Unit | null | undefined;
  metadata: KeyValuePair[];
}

export interface KeyValuePair {
  key: string;
  value: string;
}

export interface Choice extends Annotatable {
  code: string;
}

export interface Unit extends Annotatable {
  min: number | null;
  max: number | null;
}

export interface CreateAnnotation {
  negate: boolean;
  code: CodeMapping | null;
  predicate: Predicate | null;
  actions: [CreateAnnotationSubmission];
}

export type Annotation = NormalAnnotation | NegativeAnnotation;

export interface NormalAnnotation {
  id: number;
  negate: boolean;
  code: CodeMapping;
  predicate: Predicate;
  actions: (AnnotationAction | AnnotationComment | AnnotationSubmission | AnnotationVersioning)[];
  status: Status;
}

export interface NegativeAnnotation {
  id: number;
  negate: true;
  code: null;
  predicate: null;
  actions: (AnnotationAction | AnnotationComment | AnnotationSubmission | AnnotationVersioning)[];
  status: Status;
}

export interface CreateAnnotationAction {
  authorName: string;
  description: string;
  actionType: "comment" | "submission" | "versioning";
}

export interface AnnotationAction {
  id: number;
  authorName: string;
  actionType: "comment" | "submission" | "versioning";
  description: string;
  submittedAt: string;
}

export interface RichAction {
  action: AnnotationAction | AnnotationComment | AnnotationSubmission | AnnotationVersioning;
  questionnaireId: number;
  questionnaireTitle: string;
  dataElementId: number;
  dataElementLabel: string;
  annotationId: number;
  annotationLabel: string;
  annotatableId: number;
  annotatableLabel: string;
}

export interface CreateAnnotationComment extends CreateAnnotationAction {
  commentType: CommentType;
  actionType: "comment";
}
export interface AnnotationComment extends AnnotationAction {
  commentType: CommentType;
  actionType: "comment";
}

export interface CreateAnnotationSubmission extends CreateAnnotationAction {
  confidence: number;
  actionType: "submission";
}
export interface AnnotationSubmission extends AnnotationAction {
  confidence: number;
  actionType: "submission";
}

export interface AnnotationVersioning extends AnnotationAction {
  versionKey: string;
  actionType: "versioning";
}

export type Code = {
  system: string;
  code: string;
  display: string;
};
export type CodeMapping = Code & {
  systemVersion: string;
};

export interface Version {
  key: string;
  description: string;
  annotations: Annotation[];
  submittedAt: string;
}

export interface CreateVersion {
  key: string;
  description: string;
  annotationIds: number[];
}

export interface User {
  username: string;
  displayName: string;
  email: string;
}

export type Predicate =
  | "owl:sameAs"
  | "owl:equivalentClass"
  | "owl:equivalentProperty"
  | "rdfs:subClassOf"
  | "rdfs:subPropertyOf"
  | "skos:relatedMatch"
  | "skos:closeMatch"
  | "skos:exactMatch"
  | "skos:narrowMatch"
  | "skos:broadMatch"
  | "oboInOwl:hasDbXref"
  | "rdfs:seeAlso";

export type InformedCodeMapping = FhirTerminologyCode & {
  valueSet: FhirValueSetDefinition;
  codeSystemName: string;
};

export interface CodeInfo {
  /** codes/IDs of children - must be in same CodeSystem */
  children: string[];
  /** codes/IDs of parents - must be in same CodeSystem */
  parents: string[];
  definition: string;
  inactive: boolean;
  self: CodeMapping;
  alternativeNames: string[];
}

export type CodeInfoMap = { [index: string]: CodeInfo };
