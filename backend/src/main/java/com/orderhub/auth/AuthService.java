package com.orderhub.auth;

import com.orderhub.auth.AuthDtos.LoginRequest;
import com.orderhub.auth.AuthDtos.LoginResponse;
import com.orderhub.auth.AuthDtos.UserResponse;
import com.orderhub.common.exception.ResourceNotFoundException;
import com.orderhub.security.TokenService;
import com.orderhub.security.TokenService.IssuedToken;
import com.orderhub.user.User;
import com.orderhub.user.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = users.findByEmailIgnoreCase(request.email().trim())
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                // Same error for unknown email and wrong password: don't leak which accounts exist.
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        IssuedToken token = tokenService.issue(user);
        return new LoginResponse(token.value(), "Bearer", token.expiresAt(), UserResponse.from(user));
    }

    public UserResponse currentUser(String email) {
        return users.findByEmailIgnoreCase(email)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }
}
