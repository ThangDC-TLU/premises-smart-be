package com.badmintonhub.premisessmartbe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReqLoginDTO {

    @NotBlank
    @Email
    @Size(max = 191)
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;
}
