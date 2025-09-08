package com.railway.management.utils;

import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExcelResponseUtils {
    public static <T> void writeFailedExcel(HttpServletResponse response, List<T> failedData, Class<T> clazz) throws IOException {
        String fileName = "导入失败记录.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"");
        EasyExcel.write(response.getOutputStream(), clazz).sheet("失败记录").doWrite(failedData);
    }
}