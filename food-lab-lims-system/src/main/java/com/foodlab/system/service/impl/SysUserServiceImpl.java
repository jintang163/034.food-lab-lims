package com.foodlab.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodlab.common.constant.RedisConstants;
import com.foodlab.common.exception.BusinessException;
import com.foodlab.common.result.ResultCode;
import com.foodlab.common.utils.JsonUtils;
import com.foodlab.system.dto.LoginDTO;
import com.foodlab.system.entity.SysUser;
import com.foodlab.system.mapper.SysUserMapper;
import com.foodlab.system.service.SysUserService;
import com.foodlab.system.utils.JwtUtils;
import com.foodlab.system.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        SysUser user = getUserByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        String token = JwtUtils.generateToken(user.getId(), user.getUsername());

        String userKey = RedisConstants.USER_CACHE_KEY + user.getId();
        redisTemplate.opsForValue().set(userKey, JsonUtils.toJson(user), RedisConstants.USER_CACHE_KEY.length(), TimeUnit.SECONDS);

        String tokenKey = RedisConstants.TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, String.valueOf(user.getId()), RedisConstants.TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setAvatar(user.getAvatar());

        List<String> roles = baseMapper.selectRoleCodesByUserId(user.getId());
        loginVO.setRoles(roles);

        List<String> permissions = baseMapper.selectPermissionsByUserId(user.getId());
        loginVO.setPermissions(permissions);

        return loginVO;
    }

    @Override
    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            String tokenKey = RedisConstants.TOKEN_PREFIX + token;
            redisTemplate.delete(tokenKey);
        }
    }

    @Override
    public SysUser getUserByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public Page<SysUser> getUserPage(int pageNum, int pageSize, String keyword) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getPhone, keyword);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public boolean addUser(SysUser user) {
        SysUser existUser = getUserByUsername(user.getUsername());
        if (existUser != null) {
            throw new BusinessException(ResultCode.USER_EXIST);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return save(user);
    }

    @Override
    public boolean updateUser(SysUser user) {
        return updateById(user);
    }

    @Override
    public boolean deleteUser(Long userId) {
        return removeById(userId);
    }

    @Override
    public boolean resetPassword(Long userId, String newPassword) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }
}
