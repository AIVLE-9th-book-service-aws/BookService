package com.aivle.bookapp.controller;

import com.aivle.bookapp.domain.Member;
import com.aivle.bookapp.dto.LoginRequest;
import com.aivle.bookapp.dto.LoginResponse;
import com.aivle.bookapp.dto.SignupRequest;
import com.aivle.bookapp.dto.SignupResponse;
import com.aivle.bookapp.service.MemberService;
import com.aivle.bookapp.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Member API", description = "멤버 관련 API")
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    @PostMapping("/members/signup")
    public SignupResponse signup(
            @Valid @RequestBody SignupRequest request) {
        return memberService.signup(request);
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호를 입력하여 로그인 합니다.")
    @PostMapping("/members/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {
        return memberService.login(request);
    }

    @Operation(summary = "내 정보 조회", description = "내 정보를 조회합니다.")
    @GetMapping("/members/me")
    public ResponseEntity<Map<String, Object>> getMe(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.getEmail(token);

        Member member = memberService.findByEmail(email);

        Map<String, Object> body = Map.of(
                "id", member.getId(),
                "name", member.getName(),
                "email", member.getEmail()
        );
        return ResponseEntity.ok(body);
    }
}