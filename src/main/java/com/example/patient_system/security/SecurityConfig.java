package com.example.patient_system.security;

import com.example.patient_system.model.Patient;
import com.example.patient_system.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.authority.AuthorityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private PatientRepository patientRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            logger.debug("Loading user by email: {}", username);
            String normalizedUsername = username.toLowerCase().trim();
            Patient user = patientRepository.findByEmailIgnoreCase(normalizedUsername);
            if (user == null) {
                logger.warn("User not found: {}", normalizedUsername);
                throw new UsernameNotFoundException("User not found: " + normalizedUsername);
            }
            logger.debug("User found: {}, password: {}", user.getEmail(), user.getPassword());
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(), user.getPassword(), AuthorityUtils.createAuthorityList("ROLE_USER"));
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring Spring Security filter chain");
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/logout")
                )
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/", "/index", "/register", "/login", "/css/**", "/js/**", "/webjars/**").permitAll()
                        .requestMatchers("/appointments", "/appointments/book", "/medications", "/medications/add", "/medications/delete/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/login")
                );

        return http.build();
    }
}