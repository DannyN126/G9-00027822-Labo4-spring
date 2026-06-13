package com.server.app.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.server.app.config.JsonWebToken;
import com.server.app.dto.response.AuthResponse;
import com.server.app.dto.user.LoginDto;
import com.server.app.dto.user.UpdatePasswordDto;
import com.server.app.dto.user.UpdateProfileDto;
import com.server.app.dto.user.UserCreateDto;
import com.server.app.entities.User;
import com.server.app.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JsonWebToken jsonWebToken;

    public AuthController(
            UserService userService,
            JsonWebToken jsonWebToken
    ) {
        this.userService = userService;
        this.jsonWebToken = jsonWebToken;
    }

    /**
     * Inicia sesión y devuelve token + usuario.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginDto dto
    ) {

        User user = userService.login(dto);

        String token = jsonWebToken.createToken(user);

        return ResponseEntity.ok(
                new AuthResponse(token, user)
        );
    }

    /**
     * Registra un usuario con el rol predeterminado
     * definido en UserService.
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(
            @Valid @RequestBody UserCreateDto dto
    ) {

        User user = userService.signUp(dto);

        String token = jsonWebToken.createToken(user);

        return ResponseEntity.ok(
                new AuthResponse(token, user)
        );
    }

    /**
     * Devuelve los datos del usuario autenticado.
     */
    @GetMapping("/profile")
    public ResponseEntity<User> profile(
            @AuthenticationPrincipal User authenticatedUser
    ) {

        User user = userService.findById(
                authenticatedUser.getId()
        );

        return ResponseEntity.ok(user);
    }

    /**
     * Actualiza los datos del perfil y genera un token nuevo.
     */
    @PutMapping("/update/profile")
    public ResponseEntity<AuthResponse> updateProfile(
            @AuthenticationPrincipal User authenticatedUser,
            @Valid @RequestBody UpdateProfileDto dto
    ) {

        User updatedUser = userService.updateProfile(
                authenticatedUser.getId(),
                dto
        );

        String token =
                jsonWebToken.createToken(updatedUser);

        return ResponseEntity.ok(
                new AuthResponse(token, updatedUser)
        );
    }

    /**
     * Actualiza la contraseña del usuario autenticado.
     */
    @PutMapping("/update/password")
    public ResponseEntity<User> updatePassword(
            @AuthenticationPrincipal User authenticatedUser,
            @Valid @RequestBody UpdatePasswordDto dto
    ) {

        User updatedUser = userService.updatePassword(
                authenticatedUser.getId(),
                dto
        );

        return ResponseEntity.ok(updatedUser);
    }
}