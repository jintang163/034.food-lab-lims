import 'package:get/get.dart';
import 'package:logger/logger.dart';
import 'package:fluttertoast/fluttertoast.dart';

import '../../models/task_model.dart';
import '../../services/dio_service.dart';
import '../../services/connectivity_service.dart';
import '../../config/api_config.dart';

class TaskController extends GetxController {
  final Logger _logger = Logger();
  final DioService _dioService = Get.find<DioService>();
  final ConnectivityService _connectivityService = Get.find<ConnectivityService>();

  final RxList<TaskModel> pendingTasks = <TaskModel>[].obs;
  final RxList<TaskModel> testingTasks = <TaskModel>[].obs;
  final RxList<TaskModel> completedTasks = <TaskModel>[].obs;
  final RxList<TaskModel> auditingTasks = <TaskModel>[].obs;

  final RxBool isLoading = false.obs;
  final RxInt currentIndex = 0.obs;

  final List<String> tabTitles = ['待检', '检测中', '已完成', '审核中'];

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
    testingTasks.clear();
    completedTasks.clear();
    auditingTasks.clear();

    for (var task in tasks) {
      switch (task.taskStatus) {
        case 'pending':
          pendingTasks.add(task);
          break;
        case 'testing':
          testingTasks.add(task);
          break;
        case 'completed':
          completedTasks.add(task);
          break;
        case 'auditing':
          auditingTasks.add(task);
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
    if (task.taskStatus == 'pending') {
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
    } else if (task.taskStatus == 'testing') {
      Get.toNamed('/detect', arguments: task);
    } else {
      Get.toNamed('/detect', arguments: task);
    }
  }

  Color getStatusColor(String? status) {
    switch (status) {
      case 'pending':
        return Colors.grey;
      case 'testing':
        return Colors.blue;
      case 'completed':
        return Colors.green;
      case 'auditing':
        return Colors.orange;
      default:
        return Colors.grey;
    }
  }

  String getStatusText(String? status) {
    switch (status) {
      case 'pending':
        return '待检';
      case 'testing':
        return '检测中';
      case 'completed':
        return '已完成';
      case 'auditing':
        return '审核中';
      default:
        return '未知';
    }
  }

  List<TaskModel> getCurrentTasks() {
    switch (currentIndex.value) {
      case 0:
        return pendingTasks;
      case 1:
        return testingTasks;
      case 2:
        return completedTasks;
      case 3:
        return auditingTasks;
      default:
        return pendingTasks;
    }
  }
}
