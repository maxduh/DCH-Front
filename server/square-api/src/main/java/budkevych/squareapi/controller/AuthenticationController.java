package budkevych.squareapi.controller;

import budkevych.squareapi.config.ConfigProperties;
import budkevych.squareapi.dto.mapper.UserMapper;
import budkevych.squareapi.dto.request.UserLoginRequestDto;
import budkevych.squareapi.dto.request.UserRequestDto;
import budkevych.squareapi.dto.response.LoginResponseDto;
import budkevych.squareapi.dto.response.UserResponseDto;
import budkevych.squareapi.exception.AuthenticationException;
import budkevych.squareapi.model.User;
import budkevych.squareapi.security.AuthenticationService;
import budkevych.squareapi.security.jwt.JwtTokenProvider;
import budkevych.squareapi.service.UserService;
import budkevych.squareapi.service.impl.MailService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class AuthenticationController {
    private static final String LOGIN_ENDPOINT = "/login-email.html?token=";

    private final ConfigProperties configProperties;
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final MailService mailService;

    @PostMapping("/login")
    @CrossOrigin
    @Operation(summary = "login as an existing user with basic authentication")
    public ResponseEntity<?> login(@RequestBody @Valid UserLoginRequestDto userLoginDto) {
        User user = authenticationService.login(userLoginDto.getLogin(),
                userLoginDto.getPassword());
        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRoles().stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.toList()));
        LoginResponseDto response = new LoginResponseDto();
        response.setEmail(userLoginDto.getLogin());
        response.setToken(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login-email")
    @CrossOrigin
    @Operation(summary = "login as an existing user with email confirmation only")
    public ResponseEntity<?> emailOnlyLogin(@RequestBody @Valid UserLoginRequestDto userLoginDto) {
        User user = userService
                .findByEmail(userLoginDto.getLogin())
                .orElseThrow(() -> new AuthenticationException("Unknown email"));
        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRoles().stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.toList()));
        mailService.sendEmail(
                userLoginDto.getLogin(),
                "Your one click authentication",
                "Click <a href='%s%s%s'>here</a> to authenticate"
                        .formatted(configProperties.getAddress(), LOGIN_ENDPOINT, token));
        return ResponseEntity.ok("Check your email " + userLoginDto.getLogin());
    }

    @PostMapping("/register")
    @Operation(summary = "register a user")
    public UserResponseDto register(@Valid @RequestBody UserRequestDto requestDto) {
        User user = authenticationService.register(
                requestDto.getEmail(),
                requestDto.getUsername(),
                requestDto.getPassword());
        return userMapper.mapToDto(user);
    }
}
