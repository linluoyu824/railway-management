package com.railway.management.equipment.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.railway.management.equipment.dto.EquipmentDetailDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.railway.management.equipment.dto.EquipmentImportDto;
import com.railway.management.equipment.dto.EquipmentUpdateDto;
import com.railway.management.equipment.mapper.EquipmentMapper;
import com.railway.management.equipment.model.Equipment;
import com.railway.management.equipment.model.EquipmentStatus;
import com.railway.management.equipment.model.Parameter;
import com.railway.management.equipment.service.EquipmentService;
import com.railway.management.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EquipmentServiceImpl extends ServiceImpl<EquipmentMapper, Equipment> implements EquipmentService {

    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Value("${file.upload-dir.guides}")
    private String uploadDir;

    public EquipmentServiceImpl(UserMapper userMapper, ObjectMapper objectMapper) {
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

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

        // 3. 更新设备信息 (多租户插件会保证只能更新本部门的设备)
        this.updateById(existingEquipment);
        return existingEquipment; // 直接返回更新后的对象，减少一次数据库查询
    }

    @Override
    @Transactional
    public int importEquipment(InputStream inputStream) {
        // 假设多租户插件会在插入时自动填充 department_id
        List<EquipmentImportDto> importData = EasyExcel.read(inputStream, EquipmentImportDto.class, null).sheet().doReadSync();
        if (importData == null || importData.isEmpty()) {
            return 0;
        }

        List<Equipment> equipmentList = importData.stream().map(dto -> {
            Equipment equipment = new Equipment();
            equipment.setName(dto.getName());
            equipment.setType(dto.getType());
            equipment.setSerialNumber(dto.getSerialNumber());
            equipment.setPurchaseDate(dto.getPurchaseDate());
            equipment.setAdminUserId(dto.getAdminUserId());

            try {
                // 增加对空状态的处理，防止NPE
                if (StringUtils.hasText(dto.getStatus())) {
                    equipment.setStatus(EquipmentStatus.valueOf(dto.getStatus().toUpperCase()));
                } else {
                    // 如果状态为空，可以设置一个默认值或直接跳过该行
                    equipment.setStatus(EquipmentStatus.IN_USE); // 例如，设置为默认“在用”状态
                }
            } catch (IllegalArgumentException e) {
                // 记录日志或处理无效状态，为简单起见，我们跳过此记录
                // log.warn("导入设备时发现无效的状态值: {}", dto.getStatus());
                return null;
            }

            try {
                // 从JSON字符串解析参数
                if (StringUtils.hasText(dto.getParametersJson())) {
                    List<Parameter> parameters = objectMapper.readValue(dto.getParametersJson(), new TypeReference<>() {});
                    equipment.setParameters(parameters);
                }
            } catch (IOException e) {
                // 记录日志或处理无效JSON，跳过此记录
                return null;
            }
            return equipment;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        if (!equipmentList.isEmpty()) {
            // 使用MyBatis-Plus的IService提供的标准批量插入方法
            this.saveBatch(equipmentList);
            return equipmentList.size();
        }
        return 0;
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
    public List<EquipmentDetailDto> getEquipmentsByAdminUser(Long adminUserId) {
        return baseMapper.selectDetailsByAdminUserId(adminUserId);
    }
}