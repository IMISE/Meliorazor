package eu.brunsch.terminology.annotator.utils;

import java.net.URL;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public final class Utils {
    /**
     * Check if the candidate is a valid URI.
     * 
     * @param candidate possibly a url
     * @return true if valid, false if invalid
     */
    public static final boolean isUri(final String candidate) {
        try {
            final URL url = new URL(candidate);
            url.toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @returns username of currently logged-in user
     */
    public static String username() {
        final Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaimAsString("preferred_username");
    }

}
