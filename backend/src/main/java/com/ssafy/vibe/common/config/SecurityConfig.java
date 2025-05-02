package com.ssafy.vibe.common.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.ssafy.vibe.auth.handler.OAuth2AuthenticationFailureHandler;
import com.ssafy.vibe.auth.handler.OAuth2AuthenticationSuccessHandler;
import com.ssafy.vibe.auth.jwt.JwtAuthenticationEntryPoint;
import com.ssafy.vibe.auth.jwt.JwtAuthenticationFilter;
import com.ssafy.vibe.auth.service.UserAuthService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final UserAuthService userAuthService;
	private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
		OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler) throws Exception {
		http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.addFilterBefore(jwtAuthenticationFilter,
				UsernamePasswordAuthenticationFilter.class)
			.oauth2Login(oauth2 ->
				oauth2
					.authorizationEndpoint(endpoint ->
						endpoint
							.baseUri("/oauth2/authorization")
							.authorizationRequestRepository(new HttpSessionOAuth2AuthorizationRequestRepository()))
					.userInfoEndpoint(userInfo ->
						userInfo.userService(userAuthService))
					.successHandler(oAuth2AuthenticationSuccessHandler)
					.failureHandler(oAuth2AuthenticationFailureHandler))
			.authorizeHttpRequests(auth ->
				auth
					.requestMatchers("/oauth2/**", "/login/oauth2/**", "/api/health", "api/prometheus")
					.permitAll()
					.requestMatchers("/v3/api-docs/**", "/swagger-ui/**",
						"/swagger-ui/index.html/**", "/swagger-resources/**",
						"/webjars/**", "/favicon.ico").permitAll()
					.requestMatchers("/api/v1/prompt/**").permitAll()
					.anyRequest()
					.authenticated())
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(exception ->
				exception.authenticationEntryPoint(jwtAuthenticationEntryPoint));

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList(
			// 요청 허용 주소
			"http://localhost:8080",
			"http://localhost:5013",
			"https://vibeeditor.site",
			"https://vibeeditor.site:5173"
		));
		configuration.setAllowedMethods(Collections.singletonList("*"));
		configuration.setAllowedHeaders(Collections.singletonList("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}