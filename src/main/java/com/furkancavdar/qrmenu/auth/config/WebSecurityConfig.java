package com.furkancavdar.qrmenu.auth.config;

import com.furkancavdar.qrmenu.auth.adapter.api.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final CustomUserDetailsService userDetailsService;

  @Value("${app.cookie.secure}")
  private boolean secureCookie;

  @Qualifier("publicEndpoints")
  private final List<String> publicEndpoints;

  @Qualifier("authenticatedEndpoints")
  private final List<String> authenticatedEndpoints;

  @Qualifier("adminEndpoints")
  private final List<String> adminEndpoints;

  public WebSecurityConfig(
      JwtAuthenticationFilter jwtAuthenticationFilter,
      CustomUserDetailsService userDetailsService,
      List<String> publicEndpoints,
      List<String> authenticatedEndpoints,
      List<String> adminEndpoints) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.userDetailsService = userDetailsService;
    this.publicEndpoints = publicEndpoints;
    this.authenticatedEndpoints = authenticatedEndpoints;
    this.adminEndpoints = adminEndpoints;
  }

  @Bean
  public SecurityFilterChain configure(HttpSecurity http) throws Exception {
    CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    tokenRepository.setCookieCustomizer(
        cookieBuilder -> {
          cookieBuilder.sameSite("Lax");
          cookieBuilder.secure(secureCookie);
        });

    XorCsrfTokenRequestAttributeHandler delegate = new XorCsrfTokenRequestAttributeHandler();
    delegate.setCsrfRequestAttributeName("_csrf");
    CsrfTokenRequestHandler csrfHandler = delegate::handle;

    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(
            csrf ->
                csrf.csrfTokenRepository(tokenRepository)
                    .csrfTokenRequestHandler(csrfHandler)
                    .requireCsrfProtectionMatcher(
                        PathPatternRequestMatcher.withDefaults()
                            .matcher(HttpMethod.POST, "/api/v1/auth/refresh")))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // .httpBasic(Customizer.withDefaults())
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(
            logout ->
                logout
                    .addLogoutHandler(
                        new HeaderWriterLogoutHandler(
                            new ClearSiteDataHeaderWriter(
                                ClearSiteDataHeaderWriter.Directive.COOKIES)))
                    .addLogoutHandler(
                        (request, response, authentication) -> {
                          Cookie refreshCookie = new Cookie("refreshToken", null);
                          refreshCookie.setHttpOnly(true);
                          refreshCookie.setSecure(secureCookie);
                          refreshCookie.setPath("/api/v1/auth/refresh");
                          refreshCookie.setAttribute("SameSite", "Lax");
                          refreshCookie.setMaxAge(0);
                          response.addCookie(refreshCookie);
                        })
                    .logoutUrl("/api/v1/auth/logout")
                    .logoutSuccessHandler(
                        (request, response, authentication) -> {
                          response.setStatus(HttpStatus.OK.value());
                        })
                    // .deleteCookies("refreshToken")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true))
        .exceptionHandling(
            exception ->
                exception.authenticationEntryPoint(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(publicEndpoints.toArray(new String[0]))
                    .permitAll()
                    .requestMatchers(authenticatedEndpoints.toArray(new String[0]))
                    .authenticated()
                    .requestMatchers(adminEndpoints.toArray(new String[0]))
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .userDetailsService(userDetailsService)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(
        List.of(
            "http://localhost:5173",
            "http://localhost:3000",
            "http://192.168.1.15:5173",
            "http://192.168.1.13:5173",
            "https://fifty-apes-sing.loca.lt/"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(
        List.of(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "X-XSRF-TOKEN",
            "X-Request-Cookie-Secure",
            "X-Request-Cookie-SameSite"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
      throws Exception {
    return authConfig.getAuthenticationManager();
  }
}
