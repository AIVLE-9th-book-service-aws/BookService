package com.aivle.bookapp.service;

import com.aivle.bookapp.domain.Member;
import com.aivle.bookapp.dto.LoginRequest;
import com.aivle.bookapp.dto.LoginResponse;
import com.aivle.bookapp.dto.SignupRequest;
import com.aivle.bookapp.dto.SignupResponse;
import com.aivle.bookapp.exception.DuplicateEmailException;
import com.aivle.bookapp.exception.InvalidPasswordException;
import com.aivle.bookapp.exception.MemberNotFoundException;
import com.aivle.bookapp.repository.MemberRepository;
import com.aivle.bookapp.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .build();

        Member savedMember = memberRepository.save(member);

        return new SignupResponse(
                savedMember.getId(),
                savedMember.getName(),
                savedMember.getEmail(),
                "회원가입 완료"
        );
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new MemberNotFoundException(request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new InvalidPasswordException();
        }

        String accessToken = jwtUtil.generateAccessToken(member.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(member.getEmail());

        return new LoginResponse(
                member.getId(),
                member.getName(),
                "로그인 성공",
                accessToken,
                refreshToken
        );
    }

    @Transactional(readOnly = true)
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException(email));
    }
}