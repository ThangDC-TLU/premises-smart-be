package com.badmintonhub.premisessmartbe.dto;

import com.badmintonhub.premisessmartbe.entity.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO trả về khi đăng nhập thành công
 * access_token + thông tin user
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResLoginDTO {

    @JsonProperty("access_token")
    private String accessToken;

    private UserInfo user;

    // =============================
    // User hiển thị ra ngoài
    // =============================
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
        private String fullName;
        private String phone;
        private Role role;
    }

    // =============================
    // Dữ liệu nhúng trong JWT (payload)
    // =============================
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInsideToken {
        private Long id;
        private String email;
        private String fullName;
        private Role role;
    }
}
