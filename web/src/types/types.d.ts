import type { CommentType, Role, Status } from "./enums";
import type { FhirTerminologyCode, FhirValueSetDefinition } from "./fhir";

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
  type: string | null | undefined;
  choices: Choice[] | null | undefined;
  metadata: KeyValuePair[];
}

export interface KeyValuePair {
  key: string;
  value: string;
}

export interface Choice extends Annotatable {
  code: string;
}

export interface CreateAnnotation {
  code: CodeMapping;
  confidence: number;
  comment: string;
  predicate: string;
  authorName: string;
}

export interface BaseAnnotation {
  code: CodeMapping;
  predicate: string;
}

interface AuthorSpecificAnnotation {
  id: number;
  authorName: string;
  status: Status;
  comments: AnnotationComment[];
  confidence: number;
  comment: string;
}

export interface Annotation extends BaseAnnotation, AuthorSpecificAnnotation {}
export interface MultiAuthorAnnotation extends BaseAnnotation {
  authors: AuthorSpecificAnnotation[];
}

export interface AnnotationComment {
  id: number;
  authorName: string;
  contents: string;
  type: CommentType;
  submittedAt: number;
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
