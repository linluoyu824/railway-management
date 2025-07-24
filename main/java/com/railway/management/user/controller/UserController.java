package com.railway.management.user.controller;

import com.railway.management.user.dto.LoginRequest;
import com.railway.management.user.dto.LoginResponse;
import com.railway.management.user.dto.UserImportResultDto;
import com.railway.management.user.dto.UserRegistrationDto;
import com.railway.management.user.exception.UserAlreadyExistsException;
import com.railway.management.user.model.User;
import com.railway.management.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户与认证管理", description = "提供用户注册、登录、导入等功能")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "注册新用户", description = "创建一个新的用户账户")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            User newUser = userService.registerUser(registrationDto);
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        } catch (UserAlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "通过用户名和密码进行认证，成功后返回Token")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        boolean isValid = userService.validateCredentials(loginRequest.getUsername(), loginRequest.getPassword());
        if (isValid) {
            String token = userService.generateLoginToken(loginRequest.getUsername());
            return ResponseEntity.ok(new LoginResponse(token));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名或密码错误");
        }
    }

    @PostMapping("/import-batch")
    @Operation(summary = "批量导入用户", description = "通过上传Excel文件批量创建用户")
    public ResponseEntity<UserImportResultDto> importUsers(
            @Parameter(description = "包含用户信息的Excel文件", required = true) @RequestParam("file") MultipartFile file
    ) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        UserImportResultDto result = userService.importUsersFromExcel(file.getInputStream());
        return ResponseEntity.ok(result);
    }
}