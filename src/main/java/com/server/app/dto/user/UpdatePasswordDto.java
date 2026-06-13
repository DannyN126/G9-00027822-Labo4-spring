package com.server.app.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePasswordDto {

    @NotBlank(message = "La contraseña actual es obligatoria")
    private String oldpassword;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(
            min = 8,
            max = 100,
            message = "La nueva contraseña debe tener entre 8 y 100 caracteres"
    )
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&._-]).+$",
            message = "La contraseña debe incluir al menos una mayúscula, una minúscula, un número y un carácter especial"
    )
    private String newpassword;

    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    private String confirmpassword;
}