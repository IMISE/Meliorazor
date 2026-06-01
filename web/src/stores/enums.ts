export enum Role {
  ANNOTATE = "ANNOTATE",
  FEEDBACK = "FEEDBACK",
  VIEW = "VIEW",
  ADMIN = "ADMIN"
}

export const roles = [{ title: "Annotieren", value: "ANNOTATE" }, { title: "Korrigieren", value: "FEEDBACK" }, { title: "Sehen", value: "VIEW" }, { title: "Administrieren", value: "ADMIN" }];
export enum Status {
  OPEN = "OPEN",
  ACCEPTED = "ACCEPTED",
  REJECTED = "REJECTED"
}
export enum CommentType {
  ACCEPT = "ACCEPT",
  REJECT = "REJECT",
  NOTE = "NOTE"
}
