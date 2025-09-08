package com.railway.management.common.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSimpleDto {
    private Long id;
    private String fullName;
    private Long employeeId;
    private String jobTitle;
    private String mobilePhone;

    public UserSimpleDto(Long id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }
}