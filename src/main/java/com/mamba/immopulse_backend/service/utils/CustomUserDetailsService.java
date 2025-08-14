package com.mamba.immopulse_backend.service.utils;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mamba.immopulse_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService{

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
            .map(user -> User.withUsername(user.getEmail())
                              .password(user.getPassword())
                              .roles(user.getRoleUser().name())
                              .build())
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
