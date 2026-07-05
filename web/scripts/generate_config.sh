cat <<EOF > ./public/config.js
window.config = {
    BASE_URL: "${BASE_URL:-/}",
    VITE_API_BASE: "${VITE_API_BASE:-undefined}",
    VITE_OAUTH_BASE: "${VITE_OAUTH_BASE:-undefined}",
    VITE_PORT: "${VITE_PORT:-undefined}",
    VITE_OAUTH_REALM: "${VITE_OAUTH_REALM:-undefined}",
    VITE_OAUTH_CLIENT_ID: "${VITE_OAUTH_CLIENT_ID:-undefined}",
};
EOF
