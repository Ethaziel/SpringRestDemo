package cz.psgs.SpringRestDemo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import lombok.var;

import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private RSAKey rsaKeys;

   

    @Bean
    public JWKSource<SecurityContext> jwkSource(){
        rsaKeys = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKeys);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public InMemoryUserDetailsManager users(){
        return new InMemoryUserDetailsManager(
            User.withUsername("tatka")
                .password("{noop}tatka")
                .authorities("read")
                .build());
    }

    @Bean
    public AuthenticationManager authManager(UserDetailsService userDetailsService){
        var authProvider = new DaoAuthenticationProvider(userDetailsService);
        return new ProviderManager(authProvider);
    }

    @Bean
    JwtDecoder jwtDecoder() throws JOSEException{
        return NimbusJwtDecoder.withPublicKey(rsaKeys.toRSAPublicKey()).build();
    }

    @Bean
    JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwks){
        return new NimbusJwtEncoder(jwks);
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
            .authorizeHttpRequests((authz) -> authz
            .requestMatchers("/token").permitAll()
            .requestMatchers("/").permitAll()
            .requestMatchers("/swagger-ui/**").permitAll()
            .requestMatchers("v3/api-docs/**").permitAll()
            .requestMatchers("/test").authenticated()
            )
            .oauth2ResourceServer(oAuth -> oAuth.jwt(Customizer.withDefaults())
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

            // TODO only for testing
            http.csrf(AbstractHttpConfigurer::disable);
            http.headers(headers -> 
                headers.frameOptions(frameOptions -> 
                    frameOptions.disable()));

            return http.build();
            
    }
}
