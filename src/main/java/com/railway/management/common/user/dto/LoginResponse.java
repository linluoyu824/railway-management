package com.railway.management.common.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    // In a real application, you might also include user details here
    // e.g., user ID, name, roles, etc.
}