package com.badmintonhub.premisessmartbe.controller;

import com.badmintonhub.premisessmartbe.dto.ChangePasswordRequest;
import com.badmintonhub.premisessmartbe.dto.ReqLoginDTO;
import com.badmintonhub.premisessmartbe.dto.ResLoginDTO;
import com.badmintonhub.premisessmartbe.entity.Role;
import com.badmintonhub.premisessmartbe.entity.User;
import com.badmintonhub.premisessmartbe.repository.UserRepository;
import com.badmintonhub.premisessmartbe.utils.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ========================================
    // ĐĂNG KÝ
    // ========================================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã tồn tại!");
        }

        req.setPassword(passwordEncoder.encode(req.getPassword()));
        req.setRole(Role.USER);
        User savedUser = userRepository.save(req);

        return ResponseEntity.ok("Đăng ký thành công! ID: " + savedUser.getId());
    }

    // ========================================
    // ĐĂNG NHẬP
    // ========================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid ReqLoginDTO req) {
        User user = userRepository.findByEmail(req.getEmail()).orElse(null);
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Sai email hoặc mật khẩu!");
        }

        // Tạo đối tượng DTO phản hồi
        ResLoginDTO.UserInfo userInfo = new ResLoginDTO.UserInfo(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole()
        );

        ResLoginDTO resLoginDTO = new ResLoginDTO();
        resLoginDTO.setUser(userInfo);

        // Tạo Access Token
        String token = securityUtil.createAccessToken(user.getEmail(), resLoginDTO);
        resLoginDTO.setAccessToken(token);

        return ResponseEntity.ok(resLoginDTO);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody @Valid ChangePasswordRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String email = jwt.getSubject(); // lấy email từ token
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("Không tìm thấy người dùng!");
        }

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Mật khẩu cũ không chính xác!");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }

}
