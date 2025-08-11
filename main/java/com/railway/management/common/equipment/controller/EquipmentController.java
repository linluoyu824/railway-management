package com.railway.management.common.equipment.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.railway.management.common.dto.ExcelImportResult;
import com.railway.management.common.equipment.dto.EquipmentDetailDto;
import com.railway.management.common.equipment.dto.EquipmentAssignDto;
import com.railway.management.common.equipment.dto.EquipmentImportDto;
import com.railway.management.common.equipment.dto.EquipmentImportFailureDto;
import com.railway.management.common.equipment.dto.EquipmentUpdateDto;
import com.railway.management.common.equipment.model.Equipment;
import com.railway.management.common.equipment.service.EquipmentService;
import com.railway.management.utils.ExcelResponseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/equipments")
@Tag(name = "设备管理", description = "提供设备的增删改查、文档管理、批量操作等功能")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    /**
     * 分页获取设备列表
     */
    @GetMapping
    @Operation(summary = "分页查询设备列表")
    public ResponseEntity<IPage<Equipment>> listEquipment(
            @Parameter(description = "当前页码", example = "1") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页显示数量", example = "10") @RequestParam(defaultValue = "10") long size
    ) {
        IPage<Equipment> page = new Page<>(current, size);
        IPage<Equipment> equipmentPage = equipmentService.listEquipment(page);
        return ResponseEntity.ok(equipmentPage);
    }

    /**
     * 为指定设备上传指导文档
     * @param equipmentId 设备ID
     * @param file 上传的文件
     */
    @PostMapping("/{equipmentId}/guide")
    @Operation(summary = "上传设备指导文档", description = "为指定的设备上传一个指导文档文件")
    public ResponseEntity<String> uploadGuideDocument(
            @Parameter(description = "设备ID", required = true) @PathVariable Long equipmentId,
            @Parameter(description = "指导文档文件", required = true) @RequestParam("file") MultipartFile file) {
        try {
            equipmentService.uploadGuideDocument(equipmentId, file.getOriginalFilename(), file.getInputStream());
            return ResponseEntity.ok("指导文档上传成功。");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("文件上传失败: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 更新设备信息
     * @param updateDto 更新的设备信息
     * @return 更新后的设备信息
     */
    @PutMapping
    @Operation(summary = "更新设备信息")
    public ResponseEntity<?> updateEquipment(@Valid @RequestBody EquipmentUpdateDto updateDto) {
        try {
            Equipment updatedEquipment = equipmentService.updateEquipment(updateDto);
            return ResponseEntity.ok(updatedEquipment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 在实际项目中，应使用全局异常处理器
            return ResponseEntity.internalServerError().body("更新设备信息失败: " + e.getMessage());
        }
    }

    /**
     * 批量导入设备（从 Excel 文件）
     * @param file Excel 文件
     * @return 导入结果
     */
    @PostMapping("/import-batch")
    @Operation(summary = "批量导入设备", description = "通过上传Excel文件批量创建设备")
    public ResponseEntity<String> importEquipments(
            @Parameter(description = "包含设备信息的Excel文件", required = true) @RequestParam("file") MultipartFile file,
            HttpServletResponse response
    ) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("请选择要上传的文件。");
        }

        ExcelImportResult<EquipmentImportDto> result = equipmentService.importEquipment(file.getInputStream());

        if (result.hasFailures()) {
            // 当存在导入失败的数据时，设置HTTP状态码为422 (Unprocessable Entity)
            // 前端可以根据此状态码，提示用户“导入失败，请下载错误数据文件”
            // 响应体将是包含失败记录的Excel文件
            response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
            List<EquipmentImportFailureDto> failureDtos = new ArrayList<>();
            for (int i = 0; i < result.getFailedRows().size(); i++) {
                EquipmentImportDto failedRow = result.getFailedRows().get(i);
                EquipmentImportFailureDto failureDto = new EquipmentImportFailureDto();
                // BeanUtils.copyProperties(failedRow, failureDto); // Or manual mapping
                failureDto.setName(failedRow.getName());
                failureDto.setType(failedRow.getType());
                failureDto.setSerialNumber(failedRow.getSerialNumber());
                failureDto.setPurchaseDate(failedRow.getPurchaseDate());
                failureDto.setStatus(failedRow.getStatus());
                failureDto.setAdminUserId(failedRow.getAdminUserId());
                failureDto.setParametersJson(failedRow.getParametersJson());
                failureDto.setFailureReason(result.getFailureReasons().get(i));
                failureDtos.add(failureDto);
            }
            ExcelResponseUtils.writeFailedExcel(response, failureDtos, EquipmentImportFailureDto.class);
            return null;
        } else {
            String successMessage = "全部 " + result.getSuccessCount() + " 条设备记录导入成功。";
            return ResponseEntity.ok(successMessage);
        }
    }

    /**
     * 批量分配设备给指定用户
     * @param assignDto 包含设备ID列表和用户ID的DTO
     * @return 操作结果
     */
    @PostMapping("/assign-batch")
    @Operation(summary = "批量分配设备", description = "将一批设备批量分配给一个指定的用户")
    public ResponseEntity<String> assignEquipments(@Valid @RequestBody EquipmentAssignDto assignDto) {
        try {
            int assignedCount = equipmentService.assignEquipmentsToUser(assignDto.getEquipmentIds(), assignDto.getAdminUserId());
            return ResponseEntity.ok("成功为用户 " + assignDto.getAdminUserId() + " 分配了 " + assignedCount + " 台设备。");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("批量分配失败: " + e.getMessage());
        }
    }

    /**
     * 下载指定设备的指导文档
     * @param equipmentId 设备ID
     * @param response HttpServletResponse
     */
    @GetMapping("/{equipmentId}/guide/download")
    @Operation(summary = "下载设备指导文档")
    public void downloadGuideDocument(
            @Parameter(description = "设备ID", required = true) @PathVariable Long equipmentId,
            HttpServletResponse response) {
        try {
            equipmentService.downloadGuideDocument(equipmentId, response);
        } catch (Exception e) {
            // 实际项目中应由全局异常处理器统一处理，这里仅做演示
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * 获取单个设备的详细信息，包含管理人员信息
     * @param id 设备ID
     * @return 设备详情
     */
    @GetMapping("/{id}/details")
    @Operation(summary = "获取设备详情", description = "获取单个设备的详细信息，包含其管理人员的姓名和电话")
    public ResponseEntity<?> getEquipmentDetail(
            @Parameter(description = "设备ID", required = true) @PathVariable Long id
    ) {
        EquipmentDetailDto detailDto = equipmentService.getEquipmentDetail(id);
        if (detailDto == null) {
            // 如果找不到设备，返回 404 Not Found
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detailDto);
    }

    /**
     * 根据管理人员ID获取其管理的设备列表
     * @param adminUserId 管理人员ID
     * @return 设备详情列表
     */
    @GetMapping("/by-admin/{adminUserId}")
    @Operation(summary = "查询用户管理的设备", description = "根据管理人员ID获取其名下的所有设备列表")
     public ResponseEntity<List<EquipmentDetailDto>> getEquipmentsByAdminUser(
            @Parameter(description = "管理人员的用户ID", required = true) @PathVariable Long adminUserId,
            @Parameter(description = "部门路径", required = true) @RequestParam String departmentPath
    ) {
        List<EquipmentDetailDto> equipmentList = equipmentService.getEquipmentsByAdminUser(adminUserId,departmentPath);
        return ResponseEntity.ok(equipmentList);
    }
}