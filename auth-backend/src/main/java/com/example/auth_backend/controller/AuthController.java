package com.example.auth_backend.controller;

import com.example.auth_backend.auth.entities.RefreshToken;
import com.example.auth_backend.auth.entities.Users;
import com.example.auth_backend.auth.payload.LoginRequest;
import com.example.auth_backend.auth.payload.TokenResponse;
import com.example.auth_backend.auth.payload.UserDTO;
import com.example.auth_backend.auth.payload.RefreshTokenRequest;
import com.example.auth_backend.repositories.RefreshTokenRepository;
import com.example.auth_backend.repositories.UserRepository;
import com.example.auth_backend.services.AuthService;
import com.example.auth_backend.services.impl.CookieService;
import com.example.auth_backend.services.impl.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Array;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
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

    //access and refresh token lai renew garnako lai api
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody(required = false) RefreshTokenRequest body,
                                                      HttpServletResponse response,
                                                      HttpServletRequest request)
    {
     String refreshToken=readRefreshTokenFromRequest(body,request).orElseThrow(()->new BadCredentialsException("Refresh token is missing"));
     if(!jwtService.isRefreshToken(refreshToken))
     {
         throw new BadCredentialsException("Invalid Refresh token type");
     }

     String jti=jwtService.getJti(refreshToken);
     UUID userId=jwtService.getUserId(refreshToken);
     RefreshToken storedRefreshToken=refreshTokenRepository.findByJti(jti).orElseThrow(()->new BadCredentialsException("Refresh token not reconized."));

     if(storedRefreshToken.isRevoked())
     {
         throw new BadCredentialsException("Refresh token expired or revoked");
     }

     if(storedRefreshToken.getExpiresAt().isBefore(Instant.now()))
     {
         throw new BadCredentialsException("Refresh token expired");
     }

     if(!storedRefreshToken.getUser().getId().equals(userId))
     {
         throw new BadCredentialsException("Refresh token does not belong to this user.");
     }

     //refresh token ko rotate
        storedRefreshToken.setRevoked(true);
        String newJti=UUID.randomUUID().toString();
        storedRefreshToken.setReplacedByToken(newJti);
        refreshTokenRepository.save(storedRefreshToken);

        Users user=storedRefreshToken.getUser();

        var newRefreshTokenObj=RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(newRefreshTokenObj);
        String newAccessToken=jwtService.generateAccessToken(user);
        String newRefreshToken=jwtService.generateRefreshToken(user,newRefreshTokenObj.getJti());

        cookieService.attachRefreshCookie(response,newRefreshToken,(int)jwtService.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);

        return ResponseEntity.ok(TokenResponse.of(newAccessToken,newRefreshToken,jwtService.getAccessTtlSeconds(),modelMapper.map(user,UserDTO.class)));
    }

    //this method read refresh token from request or body
    private Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {
        //1. prefer reading refresh token from cookie
        if(request.getCookies() != null)
        {
            Optional<String> fromCookie= Arrays.stream(request.getCookies())
                    .filter(c->cookieService.getRefreshTokenCookieName().equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v-> !v.isBlank())
                    .findFirst();

            if(fromCookie.isPresent())
            {
                return fromCookie;
            }
        }

        //2. body
        if(body != null && body.refreshToken() != null && !body.refreshToken().isBlank())
        {
            return Optional.of(body.refreshToken());
        }

        //3. custom header
        String refreshHeader=request.getHeader("X-Refresh-Token");
        if(refreshHeader != null && !refreshHeader.isBlank())
        {
            return Optional.of(refreshHeader.trim());
        }

        //Authorization =Bearer <token>
        String authHeader=request.getHeader(HttpHeaders.AUTHORIZATION);
        if(authHeader != null && authHeader.regionMatches(true,0,"Bearer ",0,7))
        {
            String candidate=authHeader.substring(7).trim();
            if(!candidate.isEmpty())
            {
                try{
                    if(jwtService.isRefreshToken(candidate))
                    {
                        return Optional.of(candidate);
                    }
                }catch (Exception ignored)
                {

                }
            }
        }

        return Optional.empty();

    }

    //register user
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO userDTO)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDTO));
    }

    //log out

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,HttpServletResponse response)
    {
        readRefreshTokenFromRequest(null,request).ifPresent(token->{
            try{
                if(jwtService.isRefreshToken(token)){
                    String jti=jwtService.getJti(token);
                    refreshTokenRepository.findByJti(jti).ifPresent(rt->{
                        rt.setRevoked(true);
                        refreshTokenRepository.save(rt);
                    });
                }
            }catch (Exception ignored){

            }
        });

        //use cookieUtil (same behaviour)
        cookieService.clearRefreshCookie(response);
        cookieService.addNoStoreHeaders(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
