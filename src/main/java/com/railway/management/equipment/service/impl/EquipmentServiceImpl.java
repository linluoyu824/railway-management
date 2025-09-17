package com.railway.management.equipment.service.impl;

import cn.hutool.core.lang.Assert;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.railway.management.common.dto.ExcelImportResult;
import com.railway.management.common.user.mapper.UserMapper;
import com.railway.management.common.user.model.User;
import com.railway.management.equipment.dto.*;
import com.railway.management.equipment.mapper.EquipmentInspectionLogMapper;
import com.railway.management.equipment.mapper.EquipmentMapper;
import com.railway.management.equipment.mapper.ParameterHistoryMapper;
import com.railway.management.equipment.model.*;
import com.railway.management.equipment.service.EquipmentService;
import com.railway.management.equipment.service.ParameterService;
import com.railway.management.utils.EquipmentImportListener;
import com.railway.management.utils.SecurityUtils;
import com.railway.management.workorder.mapper.WorkOrderMapper;
import com.railway.management.workorder.model.WorkOrder;
import com.railway.management.workorder.model.WorkOrderStatus;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl extends ServiceImpl<EquipmentMapper, Equipment> implements EquipmentService {


    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final EquipmentInspectionLogMapper inspectionLogMapper;
    private final WorkOrderMapper workOrderMapper;
    private final ParameterService parameterService;
    private final ParameterHistoryMapper parameterHistoryMapper;

    @Value("${file.upload-dir.guides}")
    private String uploadDir;



    @Override
    public IPage<Equipment> listEquipment(IPage<Equipment> page) {
        // 使用 QueryWrapper 添加默认排序条件，与 DepartmentServiceImpl 保持一致
        QueryWrapper<Equipment> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created_at");
        // 使用MyBatis-Plus的分页查询，多租户插件会自动添加 department_id 条件
        return this.page(page, queryWrapper);
    }

    @Override
    @Transactional
    public void uploadGuideDocument(Long equipmentId, String originalFilename, InputStream inputStream) throws IOException {
        // 1. 检查设备是否存在
        Equipment equipment = this.getById(equipmentId);
        if (equipment == null) {
            throw new IllegalArgumentException("ID为 " + equipmentId + " 的设备不存在。");
        }

        // 2. 清理文件名并生成唯一存储名
        String cleanedFilename = StringUtils.cleanPath(originalFilename);
        String fileExtension = StringUtils.getFilenameExtension(cleanedFilename);
        // 生成唯一文件名，避免冲突
        String storedFilename = UUID.randomUUID() + "." + fileExtension;

        // 确保上传目录存在
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 3. 将文件流保存到目标位置
        try (InputStream in = inputStream) {
            Path destinationPath = uploadPath.resolve(storedFilename);
            Files.copy(in, destinationPath, StandardCopyOption.REPLACE_EXISTING);

            // 4. 更新设备的指导文档路径
            equipment.setGuideDocumentPath(destinationPath.toString());
            this.updateById(equipment);
        }
    }

    @Override
    @Transactional
    public Equipment updateEquipment(EquipmentUpdateDto updateDto) {
        // 1. 检查设备是否存在
        Equipment existingEquipment = this.getById(updateDto.getId());
        if (existingEquipment == null) {
            throw new IllegalArgumentException("ID为 " + updateDto.getId() + " 的设备不存在。");
        }

        // 2. 将DTO中的值映射到已存在的实体上，这样可以正确处理null值的更新
        existingEquipment.setName(updateDto.getName());
        existingEquipment.setType(updateDto.getType());
        existingEquipment.setSerialNumber(updateDto.getSerialNumber());
        existingEquipment.setPurchaseDate(updateDto.getPurchaseDate());
        existingEquipment.setStatus(updateDto.getStatus());
        existingEquipment.setAdminUserId(updateDto.getAdminUserId());
        existingEquipment.setParameters(updateDto.getParameters());
        existingEquipment.setSectionId(updateDto.getSectionId());

        // 3. 更新设备信息 (多租户插件会保证只能更新本部门的设备)
        this.updateById(existingEquipment);
        return existingEquipment; // 直接返回更新后的对象，减少一次数据库查询
    }

    @Override
    @Transactional
    public ExcelImportResult<EquipmentImportDto> importEquipment(InputStream inputStream) {
        ExcelImportResult<EquipmentImportDto> result = new ExcelImportResult<>();
        EquipmentImportListener listener = new EquipmentImportListener(this, objectMapper, result);
        EasyExcel.read(inputStream, EquipmentImportDto.class, listener).sheet().doRead();
        return result;
    }

    @Override
    @Transactional
    public int assignEquipmentsToUser(List<Long> equipmentIds, Long adminUserId) {
        // 1. 验证用户是否存在
        if (userMapper.selectById(adminUserId) == null) {
            throw new IllegalArgumentException("ID为 " + adminUserId + " 的用户不存在。");
        }

        // 2. 批量更新 (多租户插件会保证只能更新本部门的设备)
        LambdaUpdateWrapper<Equipment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Equipment::getId, equipmentIds)
                     .set(Equipment::getAdminUserId, adminUserId);

        return baseMapper.update(null, updateWrapper);
    }

    @Override
    public void downloadGuideDocument(Long equipmentId, HttpServletResponse response) throws IOException {
        // 1. 查找设备 (多租户插件会保证只能查找本部门的设备)
        Equipment equipment = this.getById(equipmentId);
        if (equipment == null) {
            throw new IllegalArgumentException("ID为 " + equipmentId + " 的设备不存在。");
        }

        String docPath = equipment.getGuideDocumentPath();
        if (!StringUtils.hasText(docPath)) {
            throw new IllegalArgumentException("该设备没有关联的指导文档。");
        }

        Path filePath = Paths.get(docPath);
        if (!Files.exists(filePath)) {
            throw new IOException("文件未找到: " + docPath);
        }

        String filename = filePath.getFileName().toString();
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"");

        Files.copy(filePath, response.getOutputStream());
        response.getOutputStream().flush();
    }

    @Override
    public EquipmentDetailDto getEquipmentDetail(Long equipmentId) {
        // baseMapper 对应的是 EquipmentMapper
        return baseMapper.selectDetailById(equipmentId);
    }

    @Override
    public List<EquipmentDetailDto> getEquipmentsByAdminUser(Long adminUserId, String departmentPath) {

        return baseMapper.selectDetailsByAdminUserId(adminUserId,departmentPath);
    }

    @Override
    @Transactional
    public EquipmentInspectionLog processNfcScan(String nfcId) {
        Assert.notBlank(nfcId, "NFC ID不能为空");

        // 1. 根据NFC ID查找设备
        Equipment equipment = baseMapper.selectOne(new QueryWrapper<Equipment>().eq("nfc_id", nfcId));
        Assert.notNull(equipment, "未找到NFC ID为 {} 的设备", nfcId);

        // 2. 获取当前用户
        User currentUser = SecurityUtils.getCurrentUser();

        // 3. 创建并保存巡检日志
        EquipmentInspectionLog log = new EquipmentInspectionLog();
        log.setEquipmentId(equipment.getId());
        log.setEquipmentName(equipment.getName());
        log.setInspectorId(currentUser.getId());
        log.setInspectorName(currentUser.getUsername());
        log.setInspectionTime(LocalDateTime.now());
        // log.setRemark("NFC自动巡检"); // You can add remarks if needed

        inspectionLogMapper.insert(log);
        return log;
    }

    @Override
    @Transactional
    public void updateParameters(EquipmentParameterUpdateRequest request) {
        Long equipmentId = request.getEquipmentId();

        // 1. 检查是否存在与该设备关联的、状态为“维修中”的工单
        QueryWrapper<WorkOrder> workOrderWrapper = new QueryWrapper<WorkOrder>()
                .eq("equipment_id", equipmentId)
                .eq("status", WorkOrderStatus.IN_PROGRESS)
                .last("LIMIT 1");
        WorkOrder activeWorkOrder = workOrderMapper.selectOne(workOrderWrapper);
        Assert.notNull(activeWorkOrder, "设备 {} 没有处于“维修中”状态的工单，无法编辑参数", equipmentId);

        User currentUser = SecurityUtils.getCurrentUser();

        for (ParameterUpdateDto paramUpdate : request.getParameters()) {
            // 2. 获取要更新的当前参数
            Parameter existingParam = parameterService.getById(paramUpdate.getParameterId());
            Assert.notNull(existingParam, "ID为 {} 的参数不存在", paramUpdate.getParameterId());
            Assert.isTrue(existingParam.getEquipmentId().equals(equipmentId),
                    "参数 {} 不属于设备 {}", existingParam.getName(), equipmentId);

            // 3. 如果值有变化，则记录历史并更新
            if (!existingParam.getValue().equals(paramUpdate.getValue())) {
                // 3.1 创建历史记录
                ParameterHistory history = new ParameterHistory();
                history.setEquipmentId(equipmentId);
                history.setParameterId(existingParam.getId());
                history.setParameterName(existingParam.getName());
                history.setOldValue(existingParam.getValue());
                history.setNewValue(paramUpdate.getValue());
                history.setWorkOrderId(activeWorkOrder.getId());
                history.setUpdatedBy(currentUser.getId());
                history.setUpdatedByName(currentUser.getUsername());
                history.setUpdateTime(LocalDateTime.now());
                parameterHistoryMapper.insert(history);

                // 3.2 更新参数值
                existingParam.setValue(paramUpdate.getValue());
                parameterService.updateById(existingParam);
            }
        }
    }


    @Override
    @Transactional
    public Equipment createFromNfc(EquipmentCreateNfcDto dto) {
        Assert.notBlank(dto.getNfcId(), "NFC ID不能为空");
        Assert.notBlank(dto.getName(), "新设备名称不能为空");
        Assert.notBlank(dto.getCode(), "新设备编码不能为空");
        Assert.notNull(dto.getDepartmentId(), "所属部门不能为空");

        // 1. Check for duplicates
        Assert.isFalse(this.exists(new QueryWrapper<Equipment>().eq("nfc_id", dto.getNfcId())),
                "NFC ID {} 已被注册", dto.getNfcId());
        Assert.isFalse(this.exists(new QueryWrapper<Equipment>().eq("code", dto.getCode())),
                "设备编码 {} 已存在", dto.getCode());

        // 2. Create and save
        Equipment equipment = new Equipment();
        equipment.setNfcId(dto.getNfcId());
        equipment.setName(dto.getName());
        equipment.setCode(dto.getCode());
        equipment.setDepartmentId(dto.getDepartmentId());
        equipment.setStatus(EquipmentStatus.NORMAL); // Default status
        this.save(equipment);

        log.info("通过NFC成功注册新设备: {}", equipment.getName());
        return equipment;
    }
}