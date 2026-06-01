import { UserManager, WebStorageStateStore } from "oidc-client-ts"

export const kc = {
  authorityUrl: `${import.meta.env.VITE_OAUTH_BASE}/realms/master`,
  realm: 'master',
  clientId: 'annotator_oid_client'
};

const auth = new UserManager({
  authority: `${kc.authorityUrl}`,
  client_id: kc.clientId,
  redirect_uri: `${window.location.origin}/auth`,
  post_logout_redirect_uri: `${window.location.origin}`,
  response_type: 'code',
  userStore: new WebStorageStateStore(),
  loadUserInfo: true
});

export default auth;
