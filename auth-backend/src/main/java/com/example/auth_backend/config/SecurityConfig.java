package com.example.auth_backend.config;

import com.example.auth_backend.dtos.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,AuthenticationSuccessHandler authenticationSuccessHandler)
    {
        this.jwtAuthenticationFilter=jwtAuthenticationFilter;
        this.authenticationSuccessHandler=authenticationSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.csrf(AbstractHttpConfigurer::disable)
                        .cors(Customizer.withDefaults())
                                .sessionManagement(sm->sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth->auth
                        .requestMatchers("/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/auth/logout").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2->oauth2.successHandler(authenticationSuccessHandler).failureHandler(null))
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex->ex
                        .authenticationEntryPoint(((request, response, e) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            String message=e.getMessage();
                            String error=(String)request.getAttribute("error");
                            if(error != null) message=error;
                            var apiError= ApiError.of(HttpStatus.UNAUTHORIZED.value(),"Unauthorized Acccess",message,request.getRequestURI(),true);
                            new ObjectMapper().writeValue(response.getWriter(),apiError);

                        }))
                        )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
//    @Bean
//    public UserDetailsService users()
//    {
//        User.UserBuilder userBuilder=User.withDefaultPasswordEncoder();UserDetails user1 = userBuilder.username("ankit").password("abc").roles("ADMIN").build();
//        UserDetails user2 = userBuilder.username("shiva").password("xyz").roles("ADMIN").build();
//        UserDetails user3 = userBuilder.username("durgesh").password("").roles("USER").build();
//        return new InMemoryUserDetailsManager(user1, user2, user3);
//    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
