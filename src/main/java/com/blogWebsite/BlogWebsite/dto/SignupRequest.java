package com.blogWebsite.BlogWebsite.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {
    private String username;
    private String name;
    private String email;
    private String profilePicBase64; // base64 encoded string
    private String password;
    private Set<String> roles;

}
