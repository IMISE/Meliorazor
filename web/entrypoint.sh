#!/bin/sh

# stolen from https://www.baeldung.com/ops/vuejs-pass-environment-variables-runtime

# Path to the runtime config.js file
CONFIG_FILE=/usr/share/nginx/html/config.js

# load config correctly
sed -i "s|\./config\.js|https://${BASE_URL}/config.js|g" "/usr/share/nginx/html/index.html"

# Replace placeholders in config.js with environment variables
echo "Generating runtime configuration in $CONFIG_FILE"
cat <<EOF > $CONFIG_FILE
window.config = {
    BASE_URL: "${BASE_URL:-/}",
    VITE_API_BASE: "${VITE_API_BASE:-undefined}",
    VITE_OAUTH_BASE: "${VITE_OAUTH_BASE:-undefined}",
    VITE_PORT: "${VITE_PORT:-undefined}",
    VITE_OAUTH_REALM: "${VITE_OAUTH_REALM:-undefined}",
    VITE_OAUTH_CLIENT_ID: "${VITE_OAUTH_CLIENT_ID:-undefined}",
};
EOF

# Start Nginx
nginx -g "daemon off;"

