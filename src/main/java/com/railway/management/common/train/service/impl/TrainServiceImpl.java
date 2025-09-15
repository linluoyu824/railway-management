package com.railway.management.common.train.service.impl;

import cn.hutool.core.lang.Assert;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.railway.management.common.department.model.Department;
import com.railway.management.common.department.service.DepartmentService;
import com.railway.management.common.dto.ExcelImportResult;
import com.railway.management.common.train.dto.TrainCreateDto;
import com.railway.management.common.train.dto.TrainDto;
import com.railway.management.common.train.dto.TrainExportDto;
import com.railway.management.common.train.dto.TrainImportDto;
import com.railway.management.common.train.dto.TrainImportFailureDto;
import com.railway.management.common.train.dto.TrainUpdateDto;
import com.railway.management.common.train.mapper.TrainMapper;
import com.railway.management.common.train.model.Train;
import com.railway.management.common.train.model.TrainStatus;
import com.railway.management.common.train.service.TrainService;
import com.railway.management.utils.TrainImportListener;
import com.railway.management.utils.ExcelResponseUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainServiceImpl extends ServiceImpl<TrainMapper, Train> implements TrainService {

    private final DepartmentService departmentService;

    @Override
    @Transactional
    public TrainDto createTrain(TrainCreateDto createDto) {
        // Check if train number already exists
        Assert.isFalse(this.exists(new QueryWrapper<Train>().eq("train_number", createDto.getTrainNumber())),
                "列车号 {} 已存在", createDto.getTrainNumber());

        Train train = new Train();
        BeanUtils.copyProperties(createDto, train);
        train.setStatus(TrainStatus.IN_SERVICE); // Default status

        this.save(train);
        return convertToDto(train);
    }

    @Override
    @Transactional
    public TrainDto updateTrain(TrainUpdateDto updateDto) {
        Train train = this.getById(updateDto.getId());
        Assert.notNull(train, "列车不存在: {}", updateDto.getId());

        // Check for unique train number if it's being changed
        if (StringUtils.hasText(updateDto.getTrainNumber()) && !updateDto.getTrainNumber().equals(train.getTrainNumber())) {
            Assert.isFalse(this.exists(new QueryWrapper<Train>().eq("train_number", updateDto.getTrainNumber())),
                    "列车号 {} 已存在", updateDto.getTrainNumber());
        }

        BeanUtils.copyProperties(updateDto, train, "id");
        this.updateById(train);
        return convertToDto(this.getById(train.getId()));
    }

    @Override
    public void deleteTrain(Long id) {
        this.removeById(id);
    }

    @Override
    public TrainDto getTrainDtoById(Long id) {
        Train train = this.getById(id);
        Assert.notNull(train, "列车不存在: {}", id);
        return convertToDto(train);
    }

    @Override
    public IPage<TrainDto> getTrainPage(IPage<Train> page, String trainNumber, Long departmentId) {
        QueryWrapper<Train> wrapper = new QueryWrapper<>();
        wrapper.like(StringUtils.hasText(trainNumber), "train_number", trainNumber);
        wrapper.eq(departmentId != null, "department_id", departmentId);
        wrapper.orderByDesc("create_time");

        IPage<Train> trainPage = this.page(page, wrapper);
        return trainPage.convert(this::convertToDto);
    }

    @Override
    @Transactional
    public ExcelImportResult<TrainImportFailureDto> importTrains(InputStream inputStream) {
        ExcelImportResult<TrainImportFailureDto> result = new ExcelImportResult<>();
        TrainImportListener listener = new TrainImportListener(this, departmentService, result);
        EasyExcel.read(inputStream, TrainImportDto.class, listener).sheet().doRead();
        return result;
    }

    @Override
    public void exportTrains(HttpServletResponse response, String trainNumber, Long departmentId) throws IOException {
        QueryWrapper<Train> wrapper = new QueryWrapper<>();
        wrapper.like(StringUtils.hasText(trainNumber), "train_number", trainNumber);
        wrapper.eq(departmentId != null, "department_id", departmentId);
        wrapper.orderByDesc("create_time");

        List<Train> trains = this.list(wrapper);
        List<TrainExportDto> exportData = trains.stream().map(train -> {
            TrainDto dto = convertToDto(train);
            TrainExportDto exportDto = new TrainExportDto();
            BeanUtils.copyProperties(dto, exportDto);
            return exportDto;
        }).collect(Collectors.toList());

        ExcelResponseUtils.writeFailedExcel(response, exportData, TrainExportDto.class);
    }

    private TrainDto convertToDto(Train train) {
        if (train == null) {
            return null;
        }
        TrainDto dto = new TrainDto();
        BeanUtils.copyProperties(train, dto);

        // Set department name
        if (train.getDepartmentId() != null) {
            Department department = departmentService.getById(train.getDepartmentId());
            if (department != null) {
                dto.setDepartmentName(department.getName());
            }
        }
        return dto;
    }
}