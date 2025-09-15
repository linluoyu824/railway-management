package com.railway.management.common.train.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.railway.management.common.dto.ExcelImportResult;
import com.railway.management.common.train.dto.TrainCreateDto;
import com.railway.management.common.train.dto.TrainDto;
import com.railway.management.common.train.dto.TrainImportFailureDto;
import com.railway.management.common.train.dto.TrainUpdateDto;
import com.railway.management.common.train.model.Train;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;

/**
 * 列车信息服务接口
 */
public interface TrainService extends IService<Train> {
    /**
     * 创建一辆新列车
     *
     * @param createDto 包含列车信息的DTO
     * @return 创建后的列车信息DTO
     */
    TrainDto createTrain(TrainCreateDto createDto);

    /**
     * 更新现有列车信息
     *
     * @param updateDto 包含要更新的列车信息的DTO
     * @return 更新后的列车信息DTO
     */
    TrainDto updateTrain(TrainUpdateDto updateDto);

    /**
     * 根据ID删除列车
     *
     * @param id 要删除的列车ID
     */
    void deleteTrain(Long id);

    /**
     * 根据ID获取列车详情
     *
     * @param id 列车ID
     * @return 列车详情DTO
     */
    TrainDto getTrainDtoById(Long id);

    /**
     * 分页查询列车列表
     *
     * @param page         分页参数
     * @param trainNumber  列车号 (可选, 用于模糊查询)
     * @param departmentId 部门ID (可选, 用于精确查询)
     * @return 分页后的列车信息DTO列表
     */
    IPage<TrainDto> getTrainPage(IPage<Train> page, String trainNumber, Long departmentId);

    /**
     * 从Excel文件导入列车信息
     *
     * @param inputStream Excel文件的输入流
     * @return 导入结果，包含成功和失败的记录
     */
    ExcelImportResult<TrainImportFailureDto> importTrains(InputStream inputStream);

    /**
     * 将列车信息导出到Excel文件
     *
     * @param response     HTTP响应对象
     * @param trainNumber  列车号 (可选, 用于筛选)
     * @param departmentId 部门ID (可选, 用于筛选)
     * @throws IOException IO异常
     */
    void exportTrains(HttpServletResponse response, String trainNumber, Long departmentId) throws IOException;
}