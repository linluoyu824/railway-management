package com.railway.managementsystem.user.dto;

import lombok.Data;

@Data
public class UserSimpleDto {
    private Long id;
    private String fullName;
    private String employeeId;
    private String jobTitle;
    private String mobilePhone;
}