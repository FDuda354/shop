package pl.dudios.shopmvn.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pl.dudios.shopmvn.security.user.model.Role;

@Configuration
public class SecurityConfig {

    private final String secret;

    public SecurityConfig(@Value("${jwt.secret}") String secret) {
        this.secret = secret;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authenticationManager,
                                                   UserDetailsService userDetailsService

    ) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole(Role.ROLE_ADMIN.getName())
                .requestMatchers(HttpMethod.GET, "/orders").authenticated()
                .requestMatchers("/actuator/health/**", "/actuator/health").permitAll()
                .requestMatchers("/actuator/**").hasRole(Role.ROLE_ADMIN.getName())
                .anyRequest().permitAll()
        );

        http.csrf(AbstractHttpConfigurer::disable);
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilter(new JwtFilter(authenticationManager, userDetailsService, secret));

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }


    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://54.37.137.7:7082","https://54.37.137.7:7082", "http://shop.dudios.pl","https://shop.dudios.pl")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                        .allowedHeaders("*")
                        .exposedHeaders("Access-Control-Allow-Origin");
            }
        };
    }

}
