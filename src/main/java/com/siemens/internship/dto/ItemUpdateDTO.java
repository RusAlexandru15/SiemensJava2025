package com.siemens.internship.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO used for updating an existing Item
 * All fields are optional
 * Jakarta Validation is applied to ensure field constraints max length and email format
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemUpdateDTO {

    @Size(max = 20, message = "Name cannot exceed 20 characters")
    private String name;

    @Size(max = 150, message = "Description cannot exceed 150 characters")
    private String description;

    @Size(max = 20, message = "Status cannot exceed 20 characters")
    private String status;

    @Email(message = "Invalid email format")
    @Size(max = 50, message = "Email cannot exceed 50 characters")
    private String email;
}
