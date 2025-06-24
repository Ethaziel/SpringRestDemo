package cz.psgs.SpringRestDemo.payload.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordDTO {

    @Size(min = 6, max = 20)
    @Schema(description = "Password", example = "password", 
            requiredMode = RequiredMode.REQUIRED, maxLength = 20, minLength = 6)
    private String password;
}
