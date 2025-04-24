package com.ssafy.vibe.user.controller;

import com.ssafy.vibe.auth.domain.UserPrincipal;
import com.ssafy.vibe.auth.service.UserAuthService;
import com.ssafy.vibe.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
//    private final UserService userService;

    @GetMapping("/test")
    public ResponseEntity<?> test(@AuthenticationPrincipal UserPrincipal userPrincipal) {
       log.info("test-userId: {}", userPrincipal.getUserId());
       return ResponseEntity.ok("");
    }
}
