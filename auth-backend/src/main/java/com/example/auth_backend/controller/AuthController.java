package com.example.auth_backend.controller;

import com.example.auth_backend.auth.entities.RefreshToken;
import com.example.auth_backend.auth.entities.Users;
import com.example.auth_backend.auth.payload.LoginRequest;
import com.example.auth_backend.auth.payload.TokenResponse;
import com.example.auth_backend.auth.payload.UserDTO;
import com.example.auth_backend.repositories.RefreshTokenRepository;
import com.example.auth_backend.repositories.UserRepository;
import com.example.auth_backend.services.AuthService;
import com.example.auth_backend.services.impl.CookieService;
import com.example.auth_backend.services.impl.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository usersRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final CookieService cookieService;
    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    //login
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response)
    {
        //authenticate
        Authentication authenticate=authenticate(loginRequest);
        Users user=usersRepository.findByEmail(loginRequest.email()).orElseThrow(()->new BadCredentialsException("Invalid username and password"));
        if(!user.isEnable())
        {
         throw new DisabledException("User is disabled");
        }

        String jti=UUID.randomUUID().toString();
        var refreshTokenObj= RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenObj);

        //generate token
        String accessToken=jwtService.generateAccessToken(user);
        String refreshToken=jwtService.generateRefreshToken(user,refreshTokenObj.getJti());

        //use cookie service to attach refresh token in cookie
        cookieService.attachRefreshCookie(response,refreshToken,(int)jwtService.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);


        TokenResponse tokenResponse=TokenResponse.of(accessToken,refreshToken,jwtService.getAccessTtlSeconds(),modelMapper.map(user,UserDTO.class));
        logger.info("Successful authentication"+tokenResponse);

        return ResponseEntity.ok(tokenResponse);

    }

    private Authentication authenticate(LoginRequest loginRequest)
    {
        try{
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(),loginRequest.password()));
        }catch (Exception e)
        {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    //register user
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO userDTO)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDTO));
    }

}
