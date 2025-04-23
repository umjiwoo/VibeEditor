package com.ssafy.vibe.auth.handler;

import java.io.IOException;

import com.ssafy.vibe.auth.domain.CustomOAuth2User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.ssafy.vibe.auth.jwt.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private static final long TOKEN_EXPIRATION = 60L * 60L * 24L * 365L;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomOAuth2User userInfo = (CustomOAuth2User)authentication.getPrincipal();

        Long userId = userInfo.getUserId();
        String email = userInfo.getName();
        String username = userInfo.getUserName();
        String provider = userInfo.getProvider();
        String providerUid = userInfo.getProviderUid();

        log.info("받아온 userId: {}", userId);

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        String token = jwtUtil.createJwt(userId, email, username, provider, providerUid);

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        String body = String.format("{\"accessToken\": \"%s\"}", token);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(body);
    }
}
