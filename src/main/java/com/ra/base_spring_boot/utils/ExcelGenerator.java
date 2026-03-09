package com.ra.base_spring_boot.utils;

import com.ra.base_spring_boot.model.entity.Roles;
import com.ra.base_spring_boot.model.entity.User;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ExcelGenerator {

    public static ByteArrayInputStream usersToExcel(List<User> list) {

        String[] columns = {
                "ID", "Full Name", "Username", "Email", "Avatar URL",
                "Bio", "Roles", "Status", "Created At", "Updated At"
        };

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Users");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // Date format
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // HEADER ROW
            Row header = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // BODY
            int rowIdx = 1;
            for (User u : list) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(u.getId());
                row.createCell(1).setCellValue(u.getFullName());
                row.createCell(2).setCellValue(u.getUsername());
                row.createCell(3).setCellValue(u.getEmail());
                row.createCell(4).setCellValue(u.getAvatarUrl() != null ? u.getAvatarUrl() : "");
                row.createCell(5).setCellValue(u.getBio() != null ? u.getBio() : "");

                String roles = u.getRoles()
                        .stream()
                        .map(r -> r.getRoleName().name())
                        .collect(Collectors.joining(", "));
                row.createCell(6).setCellValue(roles);

                // Boolean -> Active/Inactive
                row.createCell(7).setCellValue(
                        u.getStatus() != null ? u.getStatus().name() : ""
                );

                row.createCell(8).setCellValue(
                        u.getCreatedAt() != null ? u.getCreatedAt().format(dtf) : ""
                );

                row.createCell(9).setCellValue(
                        u.getUpdatedAt() != null ? u.getUpdatedAt().format(dtf) : ""
                );
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Fail to generate Excel: " + e.getMessage());
        }
    }
}
