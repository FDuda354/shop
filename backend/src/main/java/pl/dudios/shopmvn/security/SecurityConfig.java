package pl.dudios.shopmvn.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    /**
     * Provider celowo NIE jest beanem — inaczej Spring Security ostrzega, że beany
     * {@link UserDetailsService} nie zostaną użyte do automatycznego logowania
     * (patrz {@code InitializeUserDetailsBeanManagerConfigurer}).
     */
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    /**
     * Hasła w bazie są zapisane z prefiksem formatu ("{bcrypt}$2a$..."), więc zamiast
     * gołego BCryptPasswordEncoder używamy encodera delegującego, który ten prefiks
     * rozumie i dokłada go przy encode().
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
