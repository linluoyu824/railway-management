package com.railway.managementsystem.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import cn.hutool.core.util.PageUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.railway.managementsystem.user.dto.*;
import com.railway.managementsystem.user.exception.UserAlreadyExistsException;
import com.railway.managementsystem.user.model.LoginError;
import com.railway.managementsystem.user.model.User;
import com.railway.managementsystem.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            if (userService.validateCredentials(loginRequest.getUsername(), loginRequest.getPassword())) {
                // 登录成功, 生成 Token 并返回
                String token = userService.generateLoginToken(loginRequest.getUsername());
                return ResponseEntity.ok().body(new LoginResponse(token));
            } else {
                // 登录失败，凭证无效
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            }
        } catch (UsernameNotFoundException e) {
            // 用户名不存在
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Account not found");
        } catch (Exception e) {
            // 其他未知错误
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }



    @PostMapping("/register")
    /**
     * 用户注册接口
     * @param registrationDto 注册信息数据传输对象
     *                        - username: 用户名 (String)
     *                        - password: 密码 (String)
     *                        - fullName: 姓名 (String)
     *                        - employeeId: 员工号 (String)
     */
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            User newUser = userService.registerUser(registrationDto);
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        } catch (UserAlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

   @PutMapping("/{userId}/position")
    /**
     * 更新用户职位接口
     * @param userId 用户ID (Long)
     * @param newPositionId 新职位ID (Long)
     * @return 更新后的用户信息 (User)
     */
    public ResponseEntity<User> updateUserPosition(@PathVariable Long userId, @RequestBody Long newPositionId) {
        User updatedUser = userService.updateUserPosition(userId, newPositionId);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/employeesByLevel")
    /**
     * 拉取管理所属职工列表
     * @param jobLevel 新职位ID (Long)
     * @return 更新后的用户信息 (User)
     */
    public ResponseEntity<List<User>> getEmployees(@RequestParam Integer jobLevel) {
        List<User> employees = userService.getEmployeesByLevel(jobLevel);
        if (employees != null) {
            return ResponseEntity.ok(employees);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 返回 403 Forbidden 表示无权访问
        }
    }

    @GetMapping("/employees")
    public ResponseEntity<IPage<UserDto>> getEmployees(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        // 创建MyBatis-Plus的Page对象
        Page<UserDto> page = new Page<>(current, size);
        IPage<UserDto> employees = userService.getManagedEmployees(page);
        return ResponseEntity.ok(employees);
    }


    /**
     * excel批量导入
     * @param file
     * @return
     */
    @PostMapping("/import-batch")
    public ResponseEntity<UserImportResultDto> importUsers(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.ok(userService.importUsersFromExcel(file.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to process Excel file", e);
        }
    }
}
