package com.foodlab.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodlab.common.result.PageResult;
import com.foodlab.common.result.Result;
import com.foodlab.system.dto.LoginDTO;
import com.foodlab.system.entity.SysUser;
import com.foodlab.system.service.SysUserService;
import com.foodlab.system.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = sysUserService.login(loginDTO);
        return Result.success(loginVO);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        sysUserService.logout(token);
        return Result.success();
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/info")
    public Result<SysUser> getUserInfo(@RequestHeader("userId") Long userId) {
        SysUser user = sysUserService.getById(userId);
        user.setPassword(null);
        return Result.success(user);
    }

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public Result<PageResult<SysUser>> getUserPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<SysUser> page = sysUserService.getUserPage(pageNum, pageSize, keyword);
        page.getRecords().forEach(u -> u.setPassword(null));
        PageResult<SysUser> pageResult = PageResult.of(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
        return Result.success(pageResult);
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Void> addUser(@RequestBody SysUser user) {
        sysUserService.addUser(user);
        return Result.success();
    }

    @Operation(summary = "修改用户")
    @PutMapping
    public Result<Void> updateUser(@RequestBody SysUser user) {
        sysUserService.updateUser(user);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.success();
    }
}
