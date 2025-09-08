package com.railway.management.common.user.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String fullName;
    private String employeeId;
    // You can include other relevant fields here, but exclude sensitive ones like password.
    // e.g.,
    // private String departmentName;
    // private String positionName;
    // private String mobilePhone;
}