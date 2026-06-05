package com.yashwanth.pms.auth;

import com.yashwanth.pms.auth.dto.LoginRequest;
import com.yashwanth.pms.auth.dto.RegisterRequest;
import com.yashwanth.pms.auth.service.AuthService;
import com.yashwanth.pms.security.JwtUtil;
import com.yashwanth.pms.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    @PostMapping("/register")
    public String register(@RequestBody @Valid RegisterRequest request) {

        // Allow public registration for now. If you want to restrict registration
        // to admins only, re-enable @PreAuthorize("hasRole('ADMIN')").
        authService.createUser(request);
        return "Registered user successfully";
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody @Valid LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

    String token = jwtUtil.generateToken(principal);

    // return token and basic user info expected by frontend
    return Map.of(
        "token", token,
        "id", principal.getId().toString(),
        "name", principal.getName(),
        "email", principal.getUsername(),
        "role", principal.getRole()
    );
    }


}
