# AnnoTool

## KeyCloak configuration

- Create realm with the name configured in the env variable `$VITE_OAUTH_REALM`
- Create a client with the ID configured in the env variable `$VITE_OAUTH_CLIENT_ID` of type `OpenID Connect`, with the following options:
    - `Client authentication`: disabled; it's a public client
    - `Standard flow`: enabled
    - `Require PKCE`: enabled
    - `Root URL`: `http[s]://$BASE_URL`
    - `Valid redirect URIs`: `http[s]://$BASE_URL/*`
    - `Web origins`: `http[s]://$BASE_URL`
- Add your users, remember setting passwords for them
