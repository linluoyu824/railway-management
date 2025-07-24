package com.railway.management.equipment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.railway.management.equipment.dto.EquipmentDetailDto;
import com.railway.management.equipment.model.Equipment;
import com.railway.management.equipment.dto.EquipmentUpdateDto;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface EquipmentService extends IService<Equipment> {

    /**
     * 分页查询设备列表
     * @param page 分页对象
     * @return 设备分页数据
     */
    IPage<Equipment> listEquipment(IPage<Equipment> page);

    /**
     * 上传设备指导文档并与设备绑定
     * @param equipmentId 设备ID
     * @param originalFilename 文件的原始名称
     * @param inputStream 文件输入流
     */
    void uploadGuideDocument(Long equipmentId, String originalFilename, InputStream inputStream) throws IOException;

    /**
     * 更新设备信息
     * @param updateDto 更新的设备信息
     * @return 更新后的设备信息
     */
    Equipment updateEquipment(EquipmentUpdateDto updateDto);

    /**
     * 从Excel批量导入设备
     * @param inputStream Excel文件输入流
     * @return 成功导入的数量
     */
    int importEquipment(InputStream inputStream) throws IOException;

    /**
     * 批量分配设备给指定用户
     * @param equipmentIds 设备ID列表
     * @param adminUserId 管理人员ID
     * @return 成功分配的数量
     */
    int assignEquipmentsToUser(List<Long> equipmentIds, Long adminUserId);

    /**
     * 下载指定设备的指导文档
     * @param equipmentId 设备ID
     * @param response HttpServletResponse对象
     */
    void downloadGuideDocument(Long equipmentId, HttpServletResponse response) throws IOException;

    /**
     * 获取设备及其管理人员的详细信息
     * @param equipmentId 设备ID
     * @return 设备详情DTO
     */
    EquipmentDetailDto getEquipmentDetail(Long equipmentId);

    /**
     * 根据管理员ID获取其管理的设备列表
     * @param adminUserId 管理员ID
     * @return 设备详情DTO列表
     */
    List<EquipmentDetailDto> getEquipmentsByAdminUser(Long adminUserId);
}