package com.foodlab.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foodlab.system.dto.LoginDTO;
import com.foodlab.system.entity.SysUser;
import com.foodlab.system.vo.LoginVO;

public interface SysUserService extends IService<SysUser> {

    LoginVO login(LoginDTO loginDTO);

    void logout(String token);

    SysUser getUserByUsername(String username);

    Page<SysUser> getUserPage(int pageNum, int pageSize, String keyword);

    boolean addUser(SysUser user);

    boolean updateUser(SysUser user);

    boolean deleteUser(Long userId);

    boolean resetPassword(Long userId, String newPassword);
}
