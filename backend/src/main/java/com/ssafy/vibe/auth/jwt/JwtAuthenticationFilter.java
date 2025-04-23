package com.ssafy.vibe.auth.jwt;

import com.ssafy.vibe.auth.domain.CustomOAuth2User;
import com.ssafy.vibe.user.domain.ProviderName;
import com.ssafy.vibe.user.service.dto.UserDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        if(!jwtUtil.isValidAuthorization(authorizationHeader)) {
            log.info("Authorization header not valid");
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            filterChain.doFilter(request, response);
            return;
        }

        String token=authorizationHeader.substring(7);
        if(jwtUtil.isExpired(token)){
            log.info("Token expired");
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = jwtUtil.getUserId(token);
        String email = jwtUtil.getEmail(token);
        String nickname = jwtUtil.getNickname(token);
        String providerNameStr = jwtUtil.getProvider(token);
        String providerUid = jwtUtil.getProviderUid(token);

        UserDto userDto = UserDto.createUserDto(nickname, email,ProviderName.valueOf(providerNameStr), providerUid);
        userDto.setUserId(userId);
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDto);

        Authentication authToken = new UsernamePasswordAuthenticationToken(
                customOAuth2User, null, customOAuth2User.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }
}
