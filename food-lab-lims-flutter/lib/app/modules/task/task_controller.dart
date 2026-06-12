import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:logger/logger.dart';
import 'package:fluttertoast/fluttertoast.dart';

import '../../models/task_model.dart';
import '../../services/dio_service.dart';
import '../../services/connectivity_service.dart';
import '../../config/api_config.dart';
import '../../constants/task_constants.dart';

class TaskController extends GetxController {
  final Logger _logger = Logger();
  final DioService _dioService = Get.find<DioService>();
  final ConnectivityService _connectivityService = Get.find<ConnectivityService>();

  final RxList<TaskModel> pendingTasks = <TaskModel>[].obs;
  final RxList<TaskModel> detectingTasks = <TaskModel>[].obs;
  final RxList<TaskModel> auditingTasks = <TaskModel>[].obs;
  final RxList<TaskModel> completedTasks = <TaskModel>[].obs;

  final RxBool isLoading = false.obs;
  final RxInt currentIndex = 0.obs;

  final List<String> tabTitles = ['待检', '检测中', '审核中', '已完成'];

  @override
  void onInit() {
    super.onInit();
    loadMyTasks();
  }

  Future<void> loadMyTasks({bool isRefresh = false}) async {
    isLoading.value = true;

    try {
      bool isConnected = await _connectivityService.checkConnection();
      if (!isConnected) {
        Fluttertoast.showToast(msg: '网络不可用，请检查网络连接');
        isLoading.value = false;
        return;
      }

      final response = await _dioService.get(ApiConfig.taskMyTasks);

      if (response.statusCode == 200) {
        final data = response.data;
        if (data['code'] == 200) {
          final List<dynamic> records = data['data'] ?? [];
          final List<TaskModel> tasks =
              records.map((e) => TaskModel.fromJson(e)).toList();

          _categorizeTasks(tasks);
        } else {
          Fluttertoast.showToast(msg: data['message'] ?? '加载失败');
        }
      }
    } catch (e) {
      _logger.e('加载任务列表失败: $e');
      Fluttertoast.showToast(msg: '加载失败: ${e.toString()}');
    } finally {
      isLoading.value = false;
    }
  }

  void _categorizeTasks(List<TaskModel> tasks) {
    pendingTasks.clear();
    detectingTasks.clear();
    auditingTasks.clear();
    completedTasks.clear();

    for (var task in tasks) {
      switch (task.taskStatus) {
        case TaskConstants.pending:
          pendingTasks.add(task);
          break;
        case TaskConstants.detecting:
          detectingTasks.add(task);
          break;
        case TaskConstants.firstAudit:
        case TaskConstants.secondAudit:
          auditingTasks.add(task);
          break;
        case TaskConstants.approved:
        case TaskConstants.rejected:
          completedTasks.add(task);
          break;
        default:
          pendingTasks.add(task);
      }
    }
  }

  Future<void> startTask(TaskModel task) async {
    if (task.id == null) {
      Fluttertoast.showToast(msg: '任务ID无效');
      return;
    }

    try {
      bool isConnected = await _connectivityService.checkConnection();
      if (!isConnected) {
        Fluttertoast.showToast(msg: '网络不可用，无法开始任务');
        return;
      }

      final response = await _dioService.post(
        '${ApiConfig.taskStart}/${task.id}',
      );

      if (response.statusCode == 200) {
        final data = response.data;
        if (data['code'] == 200) {
          Fluttertoast.showToast(msg: '任务已开始');
          loadMyTasks();
        } else {
          Fluttertoast.showToast(msg: data['message'] ?? '操作失败');
        }
      }
    } catch (e) {
      _logger.e('开始任务失败: $e');
      Fluttertoast.showToast(msg: '操作失败: ${e.toString()}');
    }
  }

  Future<void> submitTask(TaskModel task) async {
    if (task.id == null) {
      Fluttertoast.showToast(msg: '任务ID无效');
      return;
    }

    try {
      bool isConnected = await _connectivityService.checkConnection();
      if (!isConnected) {
        Fluttertoast.showToast(msg: '网络不可用，无法提交任务');
        return;
      }

      final response = await _dioService.post(
        '${ApiConfig.taskSubmit}/${task.id}',
      );

      if (response.statusCode == 200) {
        final data = response.data;
        if (data['code'] == 200) {
          Fluttertoast.showToast(msg: '任务提交成功');
          loadMyTasks();
        } else {
          Fluttertoast.showToast(msg: data['message'] ?? '提交失败');
        }
      }
    } catch (e) {
      _logger.e('提交任务失败: $e');
      Fluttertoast.showToast(msg: '提交失败: ${e.toString()}');
    }
  }

  void goToDetect(TaskModel task) {
    if (TaskConstants.isPending(task.taskStatus)) {
      Get.dialog(
        AlertDialog(
          title: const Text('开始任务'),
          content: const Text('确定要开始该检测任务吗？'),
          actions: [
            TextButton(
              onPressed: () => Get.back(),
              child: const Text('取消'),
            ),
            TextButton(
              onPressed: () {
                Get.back();
                startTask(task);
              },
              style: TextButton.styleFrom(
                foregroundColor: Colors.blue,
              ),
              child: const Text('开始'),
            ),
          ],
        ),
      );
    } else if (TaskConstants.isDetecting(task.taskStatus)) {
      Get.toNamed('/detect', arguments: task);
    } else {
      Get.toNamed('/detect', arguments: task);
    }
  }

  Color getStatusColor(String? status) {
    switch (status) {
      case TaskConstants.pending:
        return Colors.grey;
      case TaskConstants.detecting:
        return Colors.blue;
      case TaskConstants.firstAudit:
      case TaskConstants.secondAudit:
        return Colors.orange;
      case TaskConstants.approved:
        return Colors.green;
      case TaskConstants.rejected:
        return Colors.red;
      default:
        return Colors.grey;
    }
  }

  String getStatusText(String? status) {
    return TaskConstants.getStatusText(status);
  }

  List<TaskModel> getCurrentTasks() {
    switch (currentIndex.value) {
      case 0:
        return pendingTasks;
      case 1:
        return detectingTasks;
      case 2:
        return auditingTasks;
      case 3:
        return completedTasks;
      default:
        return pendingTasks;
    }
  }
}
