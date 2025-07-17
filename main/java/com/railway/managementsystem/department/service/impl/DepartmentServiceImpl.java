package com.railway.managementsystem.department.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.alibaba.excel.EasyExcel;
import com.railway.managementsystem.department.dto.DepartmentImportDto;
import com.railway.managementsystem.department.mapper.DepartmentMapper;
import com.railway.managementsystem.department.model.Department;
import com.railway.managementsystem.department.service.DepartmentService;
import com.railway.managementsystem.user.dto.UserImportResultDto;
import com.railway.managementsystem.user.dto.UserSimpleDto;
import com.railway.managementsystem.user.mapper.UserMapper;
import com.railway.managementsystem.utils.DepartmentImportListener;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private final DepartmentMapper departmentMapper;
    @Autowired
    private final UserMapper userMapper;

    @Override
    public List<Tree<Long>> getDepartmentTree() {
        List<Department> allDepartments = departmentMapper.selectList(null);

        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        treeNodeConfig.setWeightKey("id");
        treeNodeConfig.setDeep(3);

        return TreeUtil.build(allDepartments, null, treeNodeConfig,
                (department, tree) -> {
                    tree.setId(department.getId());
                    tree.setParentId(department.getParent() != null ? department.getParent().getId() : null);
                    tree.setName(department.getName());
                    tree.putExtra("level", department.getLevel());
                });
    }

    @Override
    public IPage<Department> listDepartments(IPage<Department> page) {
        return departmentMapper.selectPage(page, null);
    }

    @Override
    public IPage<UserSimpleDto> listUsersByDepartment(Long departmentId, IPage<UserSimpleDto> page) {
        if (departmentMapper.selectById(departmentId) == null) {
            throw new UnsupportedOperationException("Department not found with id: " + departmentId);
        }
        // This assumes you have a custom method in UserMapper to do this.
        // Let's define it in UserMapper.xml for clarity.
        return userMapper.selectUsersByDepartmentPage(page, departmentId);
    }

    @Override
    @Transactional // 保证导入操作的原子性
    public UserImportResultDto importDepartments(InputStream inputStream) {
        UserImportResultDto result = new UserImportResultDto();
        DepartmentImportListener listener = new DepartmentImportListener(departmentMapper, result);
        EasyExcel.read(inputStream, DepartmentImportDto.class, listener).sheet().doRead();
        return result;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // 为了兼容各种浏览器，对文件名进行URL编码
        String fileName = URLEncoder.encode("部门导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 创建一条示例数据写入模板，引导用户填写
        DepartmentImportDto sample = new DepartmentImportDto();
        sample.setLevelOneDepartment("集团公司");
        sample.setLevelTwoDepartment("上海机务段");
        sample.setLevelThreeDepartment("运用车间");
        sample.setLevelFourDepartment("沪通车队");

        EasyExcel.write(response.getOutputStream(), DepartmentImportDto.class).sheet("部门模板").doWrite(List.of(sample));
    }
}