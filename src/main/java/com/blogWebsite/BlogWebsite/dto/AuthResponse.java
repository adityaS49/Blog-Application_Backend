package com.blogWebsite.BlogWebsite.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private Set<String> roles;
}
