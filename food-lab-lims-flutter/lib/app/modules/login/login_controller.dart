import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:flutter_smart_dialog/flutter_smart_dialog.dart';

import '../../services/dio_service.dart';
import '../../services/storage_service.dart';
import '../../config/api_config.dart';
import '../../models/user_model.dart';

class LoginController extends GetxController {
  final DioService _dioService = Get.find<DioService>();
  final StorageService _storageService = Get.find<StorageService>();

  final TextEditingController usernameController = TextEditingController();
  final TextEditingController passwordController = TextEditingController();

  final RxBool isLoading = false.obs;
  final RxBool rememberMe = false.obs;
  final RxBool obscurePassword = true.obs;

  @override
  void onInit() {
    super.onInit();
    _loadRememberedUser();
  }

  @override
  void onClose() {
    usernameController.dispose();
    passwordController.dispose();
    super.onClose();
  }

  void _loadRememberedUser() {
    final savedUsername = _storageService.getUsername();
    final savedRememberMe = _storageService.getRememberMe();

    if (savedRememberMe == true && savedUsername != null) {
      rememberMe.value = true;
      usernameController.text = savedUsername;
    }
  }

  void togglePasswordVisibility() {
    obscurePassword.value = !obscurePassword.value;
  }

  void toggleRememberMe(bool? value) {
    if (value != null) {
      rememberMe.value = value;
    }
  }

  Future<void> login() async {
    final username = usernameController.text.trim();
    final password = passwordController.text.trim();

    if (username.isEmpty) {
      Fluttertoast.showToast(msg: '请输入用户名');
      return;
    }

    if (password.isEmpty) {
      Fluttertoast.showToast(msg: '请输入密码');
      return;
    }

    isLoading.value = true;
    SmartDialog.showLoading(msg: '登录中...');

    try {
      final response = await _dioService.post(
        ApiConfig.login,
        data: {
          'username': username,
          'password': password,
        },
      );

      if (response.statusCode == 200) {
        final data = response.data['data'];
        final user = UserModel.fromJson(data);

        if (user.token != null) {
          await _storageService.saveUserInfo(
            token: user.token!,
            userId: user.id ?? 0,
            userName: user.realName ?? user.username ?? '',
            userType: user.userType ?? '',
          );

          if (rememberMe.value) {
            await _storageService.setRememberMe(true);
            await _storageService.setUsername(username);
          } else {
            await _storageService.setRememberMe(false);
            await _storageService.setUsername('');
          }

          Fluttertoast.showToast(msg: '登录成功');
          Get.offAllNamed('/home');
        } else {
          Fluttertoast.showToast(msg: '登录失败：未获取到令牌');
        }
      } else {
        Fluttertoast.showToast(msg: '登录失败：${response.statusMessage}');
      }
    } catch (e) {
      Fluttertoast.showToast(msg: '登录失败：${e.toString().replaceAll('Exception: ', '')}');
    } finally {
      isLoading.value = false;
      SmartDialog.dismiss();
    }
  }
}
