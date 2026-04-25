package com.exampe.entity.auth;

import lombok.Data;

@Data
public class Account {
    private int id;
    String email;
    private String username;
    private String password;

}
