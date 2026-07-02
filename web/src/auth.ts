import { UserManager, WebStorageStateStore } from "oidc-client-ts"
import getEnv from "./utils/env";

const base = getEnv("VITE_OAUTH_BASE");
const realm = getEnv("VITE_OAUTH_REALM");
const clientId = getEnv("VITE_OAUTH_CLIENT_ID");

export const kc = {
  authorityUrl: `${base}/realms/${realm}`,
  realm,
  clientId,
};

const auth = new UserManager({
  authority: kc.authorityUrl,
  client_id: kc.clientId,
  redirect_uri: `${window.location.origin}/afterauth`,
  post_logout_redirect_uri: `${window.location.origin}`,
  response_type: 'code',
  userStore: new WebStorageStateStore(),
  loadUserInfo: true
});

export default auth;
