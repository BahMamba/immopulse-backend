package com.mamba.immopulse_backend.service.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.mamba.immopulse_backend.config.JwtUtil;
import com.mamba.immopulse_backend.model.dto.LoginRequest;
import com.mamba.immopulse_backend.model.dto.LoginResponse;
import com.mamba.immopulse_backend.model.dto.UserProfileReponse;
import com.mamba.immopulse_backend.model.entity.User;
import com.mamba.immopulse_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // Methode de la logique d'authentification a l'appli
    public LoginResponse login(LoginRequest req){
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByEmail(userDetails.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found"));
        
        return new LoginResponse(jwt, user.getEmail(), user.getRoleUser().name());

    }

    // Methode pour les info's du User connecter
    public UserProfileReponse userProfile(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserProfileReponse(user.getFullname(), user.getEmail(), user.getRoleUser());
    }

}
