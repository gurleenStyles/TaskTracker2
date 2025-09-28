package com.tasktracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.tasktracker.repo.AppUserRepository;

@Configuration//makes this as a spring configuration class
public class SecurityConfig {

    private final AppUserRepository users;

    public SecurityConfig(AppUserRepository users) {//dependency injection
        this.users = users;
    }

    // Password encoder
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    //HERE WE ARE SHOWING SPRING DATA JPA+ SECURITY INTEGRATION
   //if the username is matched with any of the username in appuser table then the password provided by the user is checked with the password stored in the db
    //using BCryptPasswordEncoder
    //it queries your AppUserRepository
    @Bean
    public UserDetailsService userDetailsService(AppUserRepository users) {
        return username -> users.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword()) // ✅ use passwordHash here
                        .roles(user.getRole().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    // Authentication provider by the password provided by the user plus the password stored in the db
    //which checks DB via AppUserRepository it validates the [password] using BCryptPasswordEncoder
    @Bean
    public DaoAuthenticationProvider authProvider(UserDetailsService userDetailsService,
            BCryptPasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(encoder);
        return provider;
    }
    //as soon as the authentification is successful the user is redirected to the home page homecontroller.java


    // Security filter chain
    //spring security mvc - handles authentication and authorization
    //configures HTTP security for your application.
    //It sets up which URLs are public, which require login, and how login/logout work
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider authProvider) throws Exception {
        http.authenticationProvider(authProvider)
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());

        return http.build();
    }

}



