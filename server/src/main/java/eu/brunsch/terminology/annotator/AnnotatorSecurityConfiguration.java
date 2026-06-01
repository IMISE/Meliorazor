package eu.brunsch.terminology.annotator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import eu.brunsch.terminology.annotator.useraccount.KeycloakJwtAuthenticationConverter;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class AnnotatorSecurityConfiguration {

    /** Environment vars are loaded here. To be replaced some time. */
    @Autowired
    private Environment environment;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity security)
        throws Exception {

        // Allow CORS requests
        security.cors(Customizer.withDefaults());
        return security.authorizeHttpRequests(http -> http
                    // Allow access to Swagger
                    .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                    ).permitAll()
                    // Authenticate all other requests
                    .anyRequest().authenticated()
                // Use OAuth
                ).oauth2ResourceServer(oauth2 ->
                    oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(
                            new KeycloakJwtAuthenticationConverter()))
                ).build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {

        // without this, the oauth call to keycloak results in "Read timed out" error
        // TODO put this where it actually belongs
        System.setProperty("com.nimbusds.jose.jwk.source.RemoteJWKSet.defaultHttpReadTimeout", "2000");
        System.setProperty("com.nimbusds.jose.jwk.source.RemoteJWKSet.defaultHttpConnectTimeout", "2000");

        JwtDecoder jwtDecoder = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();

        return new JwtDecoder() {
            @Override
            public Jwt decode(String token) throws JwtException {
                System.out.println("token: " + token);
                Jwt jwt = jwtDecoder.decode(token);
                System.out.println("jwt: " + jwt);
                return jwt;
            }
        };
    }

    /**
     * Configure allowed Origins for CORS.
     *
     * @return A configurer that configures CORS allowed origins.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            /**
             * Configure allowed Origins for CORS.
             */
            @Override
            public void addCorsMappings(final CorsRegistry registry) {
                final String urls = environment.getProperty("whitelisted.cors.url");
                if (urls == null) {
                    throw new RuntimeException(
                        "Missing whitelisted CORS properties!"
                    );
                }
                registry.addMapping("/**")
                    .allowedOrigins(urls.split(","))
                    .allowedMethods("*");
            }
        };
    }

    /**
     * Log incoming requests (for debugging).
     *
     * @return Logging filter that logs the whole request.
     */
    @Bean
    @ConditionalOnProperty(
        value = "logging.level.org.springframework.web.filter.CommonsRequestLoggingFilter",
        havingValue = "DEBUG", // No TRACE because that would become too complex here
        matchIfMissing = false
    )
    public CommonsRequestLoggingFilter logFilter() {
        final int maxPayloadLength = 10000;

        CommonsRequestLoggingFilter filter
          = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(maxPayloadLength);
        filter.setIncludeHeaders(true);
        filter.setIncludeClientInfo(true);
        return filter;
    }

}
