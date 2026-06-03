import type { Role, Status } from "./enums";
import type { User } from "./user";

export interface Questionnaire {
  id: number;
  title: string;
  description: string;
  dataElements: Variable[];
  options: QuestionnaireOptions;
  participants: Participant[];
  valueSets: FhirValueSetDefinition[],
}

export interface QuestionnaireOptions {
  reqNegReviews: number,
  reqPosReviews: number,
  reqNegReviewRatio: number,
  reqPosReviewRatio: number,
}

export interface Participant {
  username: string;
  roles: Role[];
}

export interface Variable {
  id: number | undefined;
  variableName: ?string;
  label: string;
  type: ?string;
  choices: ?Choice[];
  metadata: KeyValuePair[];
  annotations: Annotation[];
}

export interface KeyValuePair {
  key: string,
  value: string,
}

export interface Choice {
  id: number | undefined;
  code: string;
  name: string;
  annotations: Annotation[];
}

export interface Annotation {
  id: number;
  authorName: string;
  code: CodeMapping;
  status: Status;
  comments: AnnotationComment[];
}

export interface AnnotationComment {
  id: number;
  authorName: string;
  contents: string;
  type: CommentType;
  submittedAt: number;
}

export type CodeMapping = {
  system: string,
  code: string,
  display: string,
};

export type FhirValueSetDefinition = {
  display: string,
  server: string,
  valueSet: string,
};

export interface User {
  username: string;
  displayName: string;
  email: string;
}

export type Predicate = "owl:sameAs" | "owl:equivalentClass" | "owl:equivalentProperty" | "rdfs:subClassOf" | "rdfs:subPropertyOf" | "skos:relatedMatch" | "skos:closeMatch" | "skos:exactMatch" | "skos:narrowMatch" | "skos:broadMatch" | "oboInOwl:hasDbXref" | "rdfs:seeAlso";

export type InformedCodeMapping = CodeMapping & { valueSet: FhirValueSetDefinition };

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
