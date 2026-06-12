import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';

import 'login_controller.dart';
import '../../theme/app_theme.dart';

class LoginView extends GetView<LoginController> {
  const LoginView({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppTheme.bgColor,
      body: SafeArea(
        child: SingleChildScrollView(
          child: Container(
            padding: EdgeInsets.symmetric(horizontal: 32.w),
            height: 1.sh - 40.h,
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                _buildLogo(),
                SizedBox(height: 40.h),
                _buildTitle(),
                SizedBox(height: 48.h),
                _buildUsernameField(),
                SizedBox(height: 16.h),
                _buildPasswordField(),
                SizedBox(height: 16.h),
                _buildRememberMe(),
                SizedBox(height: 32.h),
                _buildLoginButton(),
                SizedBox(height: 24.h),
                _buildFooter(),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildLogo() {
    return Center(
      child: Container(
        width: 100.r,
        height: 100.r,
        decoration: BoxDecoration(
          color: AppTheme.primaryColor,
          borderRadius: BorderRadius.circular(24.r),
          boxShadow: [
            BoxShadow(
              color: AppTheme.primaryColor.withOpacity(0.3),
              blurRadius: 20,
              offset: const Offset(0, 10),
            ),
          ],
        ),
        child: Icon(
          Icons.science,
          color: Colors.white,
          size: 56.sp,
        ),
      ),
    );
  }

  Widget _buildTitle() {
    return Column(
      children: [
        Text(
          '食品检测LIMS',
          style: TextStyle(
            fontSize: 28.sp,
            fontWeight: FontWeight.bold,
            color: AppTheme.textPrimary,
          ),
        ),
        SizedBox(height: 8.h),
        Text(
          '实验室信息管理系统',
          style: TextStyle(
            fontSize: 14.sp,
            color: AppTheme.textSecondary,
          ),
        ),
      ],
    );
  }

  Widget _buildUsernameField() {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12.r),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: TextField(
        controller: controller.usernameController,
        keyboardType: TextInputType.text,
        textInputAction: TextInputAction.next,
        style: TextStyle(fontSize: 15.sp, color: AppTheme.textPrimary),
        decoration: InputDecoration(
          hintText: '请输入用户名',
          hintStyle: TextStyle(fontSize: 15.sp, color: AppTheme.textHint),
          prefixIcon: Icon(
            Icons.person_outline,
            color: AppTheme.primaryColor,
            size: 22.sp,
          ),
          border: InputBorder.none,
          contentPadding: EdgeInsets.symmetric(horizontal: 16.w, vertical: 16.h),
        ),
      ),
    );
  }

  Widget _buildPasswordField() {
    return Obx(() => Container(
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(12.r),
            boxShadow: [
              BoxShadow(
                color: Colors.grey.withOpacity(0.1),
                blurRadius: 10,
                offset: const Offset(0, 4),
              ),
            ],
          ),
          child: TextField(
            controller: controller.passwordController,
            obscureText: controller.obscurePassword.value,
            keyboardType: TextInputType.visiblePassword,
            textInputAction: TextInputAction.done,
            onSubmitted: (_) => controller.login(),
            style: TextStyle(fontSize: 15.sp, color: AppTheme.textPrimary),
            decoration: InputDecoration(
              hintText: '请输入密码',
              hintStyle: TextStyle(fontSize: 15.sp, color: AppTheme.textHint),
              prefixIcon: Icon(
                Icons.lock_outline,
                color: AppTheme.primaryColor,
                size: 22.sp,
              ),
              suffixIcon: GestureDetector(
                onTap: controller.togglePasswordVisibility,
                child: Icon(
                  controller.obscurePassword.value ? Icons.visibility_off_outlined : Icons.visibility_outlined,
                  color: AppTheme.textSecondary,
                  size: 22.sp,
                ),
              ),
              border: InputBorder.none,
              contentPadding: EdgeInsets.symmetric(horizontal: 16.w, vertical: 16.h),
            ),
          ),
        ));
  }

  Widget _buildRememberMe() {
    return Obx(() => Row(
          children: [
            SizedBox(
              width: 24.w,
              height: 24.h,
              child: Checkbox(
                value: controller.rememberMe.value,
                onChanged: controller.toggleRememberMe,
                activeColor: AppTheme.primaryColor,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(4.r),
                ),
              ),
            ),
            SizedBox(width: 8.w),
            GestureDetector(
              onTap: () => controller.toggleRememberMe(!controller.rememberMe.value),
              child: Text(
                '记住密码',
                style: TextStyle(
                  fontSize: 14.sp,
                  color: AppTheme.textSecondary,
                ),
              ),
            ),
          ],
        ));
  }

  Widget _buildLoginButton() {
    return Obx(() => Container(
          height: 52.h,
          decoration: BoxDecoration(
            gradient: LinearGradient(
              colors: [
                AppTheme.primaryColor,
                AppTheme.secondaryColor,
              ],
            ),
            borderRadius: BorderRadius.circular(12.r),
            boxShadow: [
              BoxShadow(
                color: AppTheme.primaryColor.withOpacity(0.3),
                blurRadius: 12,
                offset: const Offset(0, 6),
              ),
            ],
          ),
          child: ElevatedButton(
            onPressed: controller.isLoading.value ? null : controller.login,
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.transparent,
              shadowColor: Colors.transparent,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12.r),
              ),
            ),
            child: controller.isLoading.value
                ? SizedBox(
                    width: 24.w,
                    height: 24.h,
                    child: const CircularProgressIndicator(
                      strokeWidth: 2,
                      valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                    ),
                  )
                : Text(
                    '登 录',
                    style: TextStyle(
                      fontSize: 16.sp,
                      fontWeight: FontWeight.w600,
                      color: Colors.white,
                    ),
                  ),
          ),
        ));
  }

  Widget _buildFooter() {
    return Column(
      children: [
        Text(
          '版本 1.0.0',
          style: TextStyle(
            fontSize: 12.sp,
            color: AppTheme.textHint,
          ),
        ),
        SizedBox(height: 8.h),
        Text(
          '© 2024 食品检测LIMS',
          style: TextStyle(
            fontSize: 12.sp,
            color: AppTheme.textHint,
          ),
        ),
      ],
    );
  }
}
