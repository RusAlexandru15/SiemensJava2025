package com.siemens.internship.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

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