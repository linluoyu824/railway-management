package com.railway.management.common.user.controller;

import com.railway.management.common.dto.ExcelImportResult;
import com.railway.management.common.user.dto.LoginRequest;
import com.railway.management.common.user.dto.LoginResponse;
import com.railway.management.common.user.dto.UserImportDto;
import com.railway.management.common.user.dto.UserImportFailureDto;
import com.railway.management.common.user.dto.UserRegistrationDto;
import com.railway.management.common.user.exception.UserAlreadyExistsException;
import com.railway.management.common.user.model.User;
import com.railway.management.common.user.service.UserService;
import com.railway.management.utils.ExcelResponseUtils;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.ArrayList;
import java.util.List;

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
    public ResponseEntity<String> importUsers(
            @Parameter(description = "包含用户信息的Excel文件", required = true) @RequestParam("file") MultipartFile file,
            HttpServletResponse response
    ) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("请选择要上传的文件。");
        }
        ExcelImportResult<UserImportDto> result = userService.importUsersFromExcel(file.getInputStream());

        if (result.hasFailures()) {
            // 当存在导入失败的数据时，设置HTTP状态码为422 (Unprocessable Entity)
            // 前端可以根据此状态码，提示用户“导入失败，请下载错误数据文件”
            // 响应体将是包含失败记录的Excel文件
            response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
            List<UserImportFailureDto> failureDtos = new ArrayList<>();
            for (int i = 0; i < result.getFailedRows().size(); i++) {
                UserImportDto failedRow = result.getFailedRows().get(i);
                UserImportFailureDto failureDto = new UserImportFailureDto();
                failureDto.setEmployeeId(failedRow.getEmployeeId());
                failureDto.setFullName(failedRow.getFullName());
                failureDto.setMobilePhone(failedRow.getMobilePhone());
                failureDto.setSection(failedRow.getSection());
                failureDto.setWorkshop(failedRow.getWorkshop());
                failureDto.setTeam(failedRow.getTeam());
                failureDto.setGuidanceGroup(failedRow.getGuidanceGroup());
                failureDto.setJobTitle(failedRow.getJobTitle());
                failureDto.setFailureReason(result.getFailureReasons().get(i));
                failureDtos.add(failureDto);
            }
            ExcelResponseUtils.writeFailedExcel(response, failureDtos, UserImportFailureDto.class);
            return null;
        } else {
            String successMessage = "全部 " + result.getSuccessCount() + " 条用户记录导入成功。";
            return ResponseEntity.ok(successMessage);
        }
    }
}