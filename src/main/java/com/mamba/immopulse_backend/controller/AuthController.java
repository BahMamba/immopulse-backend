package com.mamba.immopulse_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mamba.immopulse_backend.model.dto.LoginRequest;
import com.mamba.immopulse_backend.model.dto.LoginResponse;
import com.mamba.immopulse_backend.model.dto.UserProfileReponse;
import com.mamba.immopulse_backend.service.utils.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // Methode pour le login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req){
        LoginResponse response = authService.login(req);
        return ResponseEntity.ok(response);
    }

    // Methode pour recuperer les info ud user connecter
    @GetMapping("/me")
    public ResponseEntity<UserProfileReponse> userProfile() {
        UserProfileReponse profile = authService.userProfile();
        return ResponseEntity.ok(profile);
    }

}   
