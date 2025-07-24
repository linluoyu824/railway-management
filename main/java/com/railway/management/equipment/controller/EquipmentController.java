package com.railway.management.equipment.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.railway.management.equipment.dto.EquipmentDetailDto;
import com.railway.management.equipment.dto.EquipmentAssignDto;
import com.railway.management.equipment.dto.EquipmentUpdateDto;
import com.railway.management.equipment.model.Equipment;
import com.railway.management.equipment.service.EquipmentService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/equipments")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    /**
     * 分页获取设备列表
     */
    @GetMapping
    public ResponseEntity<IPage<Equipment>> listEquipment(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
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
    public ResponseEntity<String> uploadGuideDocument(
            @PathVariable Long equipmentId,
            @RequestParam("file") MultipartFile file) {
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
    public ResponseEntity<String> importEquipments(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("请选择要上传的文件。");
        }
        try {
            int importedCount = equipmentService.importEquipment(file.getInputStream());
            return ResponseEntity.ok("成功导入 " + importedCount + " 条设备记录。");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("文件导入失败: " + e.getMessage());
        }
    }

    /**
     * 批量分配设备给指定用户
     * @param assignDto 包含设备ID列表和用户ID的DTO
     * @return 操作结果
     */
    @PostMapping("/assign-batch")
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
    public void downloadGuideDocument(@PathVariable Long equipmentId, HttpServletResponse response) {
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
    public ResponseEntity<?> getEquipmentDetail(@PathVariable Long id) {
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
    public ResponseEntity<List<EquipmentDetailDto>> getEquipmentsByAdminUser(@PathVariable Long adminUserId) {
        List<EquipmentDetailDto> equipmentList = equipmentService.getEquipmentsByAdminUser(adminUserId);
        return ResponseEntity.ok(equipmentList);
    }
}