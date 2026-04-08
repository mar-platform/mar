package ml2.mar.webserver.security;

import java.util.Arrays;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@Profile("production")
@EnableWebSecurity
@EnableMethodSecurity
public class ProductionSecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(ProductionSecurityConfig.class);

    public ProductionSecurityConfig() {
        logger.info("Profile production detected: Using ProductionSecurityConfig");
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // CORS configuration (ALLOW ALL TRAFFIC)
		http.cors(corsConfig -> corsConfig.configurationSource(request -> {
            CorsConfiguration config = new CorsConfiguration();
            //config.setAllowedOrigins(Arrays.asList("http://localhost:4173"));            
            config.setAllowedOrigins(Arrays.asList("*"));
            
            config.setAllowedMethods(Arrays.asList("*"));
            config.setAllowCredentials(true);
            config.setAllowedHeaders(Collections.singletonList("*"));
            config.setExposedHeaders(Collections.emptyList());
            config.setMaxAge(3600L);
            return config;
        }));

        http.csrf(csrf -> csrf.disable());
        http.httpBasic(hbc -> hbc.disable());
        http.formLogin(flc -> flc.disable());
        
        // While in peer-review
        http.headers(headers -> headers .frameOptions(frameOptions -> frameOptions.disable()) );
        
        return http.build();
    }
}
