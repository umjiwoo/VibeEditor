package com.ssafy.vibe.auth.jwt;

import com.ssafy.vibe.auth.domain.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.ssafy.vibe.common.exception.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.ssafy.vibe.common.exception.ExceptionCode.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        if(!jwtUtil.isValidAuthorization(authorizationHeader)) {
            throw new AuthenticationException(INVALID_TOKEN);
        }

        String token=authorizationHeader.substring(7);
        log.info("token: {}", token);
        if(jwtUtil.isExpired(token)){
            log.info("Token expired");
            throw new AuthenticationException(INVALID_TOKEN);
        }

        Long userId = jwtUtil.getUserId(token);
        log.info("userId: {}", userId);

        UserPrincipal userPrincipal = new UserPrincipal(userId);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
