package com.ssafy.vibe.auth.jwt;

import static com.ssafy.vibe.common.exception.ExceptionCode.*;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ssafy.vibe.auth.domain.UserPrincipal;
import com.ssafy.vibe.common.exception.AuthenticationException;
import com.ssafy.vibe.user.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String authorizationHeader = request.getHeader("Authorization");
		if (!jwtUtil.isValidAuthorization(authorizationHeader)) {
			throw new AuthenticationException(INVALID_TOKEN);
		}

		String token = authorizationHeader.substring(7);
		log.info("token: {}", token);
		if (jwtUtil.isExpired(token)) {
			log.info("Token expired");
			throw new AuthenticationException(INVALID_TOKEN);
		}

		Long userId = jwtUtil.getUserId(token);

		UserPrincipal userPrincipal = new UserPrincipal(userId);
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userPrincipal, null, userPrincipal.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(authentication);
		filterChain.doFilter(request, response);
	}
}
