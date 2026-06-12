package com.foodlab.system.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LoginVO implements Serializable {

    private String token;

    private Long userId;

    private String username;

    private String realName;

    private String avatar;

    private List<String> roles;

    private List<String> permissions;
}
