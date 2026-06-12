import 'package:get/get.dart';
import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:flutter_smart_dialog/flutter_smart_dialog.dart';

import '../../services/storage_service.dart';
import '../../services/dio_service.dart';
import '../../config/api_config.dart';

class HomeController extends GetxController {
  final StorageService _storageService = Get.find<StorageService>();
  final DioService _dioService = Get.find<DioService>();

  final RxInt currentIndex = 0.obs;

  final RxString userName = ''.obs;
  final RxString userType = ''.obs;

  @override
  void onInit() {
    super.onInit();
    _loadUserInfo();
  }

  void _loadUserInfo() {
    userName.value = _storageService.getUserName() ?? '';
    userType.value = _storageService.getUserType() ?? '';
  }

  void changeIndex(int index) {
    currentIndex.value = index;
  }

  Future<void> logout() async {
    Get.dialog(
      AlertDialog(
        title: const Text('退出登录'),
        content: const Text('确定要退出登录吗？'),
        actions: [
          TextButton(
            onPressed: () => Get.back(),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () async {
              Get.back();
              await _performLogout();
            },
            child: const Text('确定'),
          ),
        ],
      ),
    );
  }

  Future<void> _performLogout() async {
    try {
      SmartDialog.showLoading(msg: '退出中...');

      try {
        await _dioService.post(ApiConfig.logout);
      } catch (e) {
        debugPrint('退出登录API调用失败: $e');
      }

      await _storageService.clearUserInfo();
      await _storageService.setRememberMe(false);

      Fluttertoast.showToast(msg: '已退出登录');
      Get.offAllNamed('/login');
    } catch (e) {
      Fluttertoast.showToast(msg: '退出失败：${e.toString()}');
    } finally {
      SmartDialog.dismiss();
    }
  }
}
