package com.railway.management.common.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.railway.management.common.department.service.DepartmentService;
import com.railway.management.common.equipment.dto.AccessControlCreateDto;
import com.railway.management.common.equipment.dto.AccessControlLogCreateDto;
import com.railway.management.common.equipment.dto.AccessControlUpdateDto;
import com.railway.management.common.equipment.mapper.AccessControlLogMapper;
import com.railway.management.common.equipment.mapper.AccessControlMapper;
import com.railway.management.common.equipment.model.AccessControl;
import com.railway.management.common.equipment.model.AccessControlLog;
import com.railway.management.common.equipment.model.AccessControlStatus;
import com.railway.management.common.equipment.service.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccessControlServiceImpl extends ServiceImpl<AccessControlMapper, AccessControl> implements AccessControlService {

    private final AccessControlLogMapper accessControlLogMapper;
    private final DepartmentService departmentService;

    @Override
    public IPage<AccessControl> listAccessControls(IPage<AccessControl> page, String departmentPath) {
        QueryWrapper<AccessControl> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("department_path", departmentPath);
        queryWrapper.orderByDesc("created_at");
        return this.page(page, queryWrapper);
    }

    @Override
    @Transactional
    public AccessControl createAccessControl(AccessControlCreateDto createDto) {
        AccessControl accessControl = new AccessControl();
        accessControl.setName(createDto.getName());
        accessControl.setLocation(createDto.getLocation());
        accessControl.setIpAddress(createDto.getIpAddress());
        accessControl.setDepartmentId(createDto.getDepartmentId());
        accessControl.setStatus(AccessControlStatus.ONLINE); // 默认状态

        // 构建并设置部门路径
        String departmentPath = departmentService.buildDepartmentPath(createDto.getDepartmentId());
        if (departmentPath == null) {
            throw new IllegalArgumentException("无效的部门ID: " + createDto.getDepartmentId());
        }
        accessControl.setDepartmentPath(departmentPath);

        this.save(accessControl);
        return accessControl;
    }

    @Override
    @Transactional
    public AccessControl updateAccessControl(AccessControlUpdateDto updateDto) {
        AccessControl existing = this.getById(updateDto.getId());
        if (existing == null) {
            throw new IllegalArgumentException("门禁设备不存在，ID: " + updateDto.getId());
        }
        existing.setName(updateDto.getName());
        existing.setLocation(updateDto.getLocation());
        existing.setIpAddress(updateDto.getIpAddress());
        if (updateDto.getStatus() != null) {
            existing.setStatus(updateDto.getStatus());
        }
        this.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void deleteAccessControl(Long id) {
        if (!this.removeById(id)) {
            throw new IllegalArgumentException("删除失败，门禁设备不存在，ID: " + id);
        }
    }

    @Override
    @Transactional
    public AccessControlLog recordAction(AccessControlLogCreateDto logDto) {
        if (this.getById(logDto.getAccessControlId()) == null) {
            throw new IllegalArgumentException("门禁设备不存在，ID: " + logDto.getAccessControlId());
        }

        AccessControlLog log = new AccessControlLog();
        log.setAccessControlId(logDto.getAccessControlId());
        log.setUserId(logDto.getUserId());
        log.setAction(logDto.getAction());
        log.setSuccess(logDto.isSuccess());
        log.setRemark(logDto.getRemark());
        log.setTimestamp(LocalDateTime.now());

        accessControlLogMapper.insert(log);
        return log;
    }

    @Override
    public IPage<AccessControlLog> getLogsByAccessControlId(IPage<AccessControlLog> page, Long accessControlId) {
        QueryWrapper<AccessControlLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("access_control_id", accessControlId);
        queryWrapper.orderByDesc("timestamp");
        return accessControlLogMapper.selectPage(page, queryWrapper);
    }
}