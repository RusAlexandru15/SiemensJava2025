package com.siemens.internship.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO used for creating a new Item.
 * Name, status, and email are required and must not be blank.
 * Description is optional.
 * Field constraints:
 * - Name: max 20 characters
 * - Description: max 150 characters
 * - Status: max 20 characters
 * - Email: must be valid format and max 50 characters
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemCreateDTO {

    @NotBlank(message = "Name must not be blank")
    @Size(max = 20, message = "Name cannot exceed 20 characters")
    private String name;

    @Size(max = 150, message = "Description cannot exceed 150 characters")
    private String description;

    @NotBlank(message = "Status must not be blank")
    @Size(max = 20, message = "Status cannot exceed 20 characters")
    private String status;

    @NotBlank(message = "Email must not be blank")
    @Size(max = 50, message = "Email cannot exceed 50 characters")
    @Email(message = "Invalid email format")
    private String email;
}