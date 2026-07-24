import type { CommentType, Role, Status } from "./enums";
import type { FhirTerminologyCode } from "./fhir";

export interface Questionnaire {
  id: number;
  title: string;
  description: string;
  dataElements: Variable[];
  options: QuestionnaireOptions;
  participants: Participant[];
  valueSets: FhirValueSetDefinition[];
}

export type QuestionnaireOut = Questionnaire & {
  id: undefined | null;
  title: undefined | null;
  description: undefined | null;
  dataElements: undefined | null;
  options: undefined | null;
  participants: undefined | null;
  valueSets: undefined | null;
};

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

export type Annotation = PositiveAnnotation | NegativeAnnotation;

export interface PositiveAnnotation {
  id: number;
  negate: false;
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
