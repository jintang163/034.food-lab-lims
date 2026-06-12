import 'package:get/get.dart';
import 'package:logger/logger.dart';
import 'package:fluttertoast/fluttertoast.dart';

import '../../services/dio_service.dart';
import '../../services/database_service.dart';
import '../../services/connectivity_service.dart';
import '../../config/api_config.dart';
import '../../models/sample_model.dart';
import '../../models/detect_result_model.dart';

class OfflineController extends GetxController {
  final DioService _dioService = Get.find<DioService>();
  final DatabaseService _databaseService = Get.find<DatabaseService>();
  final ConnectivityService _connectivityService = Get.find<ConnectivityService>();
  final Logger _logger = Logger();

  final RxList<SampleModel> pendingSamples = <SampleModel>[].obs;
  final RxList<DetectResultModel> pendingDetectResults = <DetectResultModel>[].obs;
  final RxBool isLoading = false.obs;
  final RxBool isSyncing = false.obs;
  final RxInt syncProgress = 0.obs;
  final RxInt syncTotal = 0.obs;
  final RxInt syncSuccess = 0.obs;
  final RxInt syncFailed = 0.obs;
  final RxString syncStatus = ''.obs;
  final RxList<String> syncErrors = <String>[].obs;
  final RxString currentSyncItem = ''.obs;

  @override
  void onReady() {
    super.onReady();
    loadPendingData();
  }

  Future<void> loadPendingData() async {
    isLoading.value = true;
    try {
      final samples = await _databaseService.getPendingSamples();
      final results = await _databaseService.getPendingDetectResults();

      pendingSamples.value = samples;
      pendingDetectResults.value = results;

      _logger.i('待同步样品: ${samples.length}, 待同步检测结果: ${results.length}');
    } catch (e) {
      _logger.e('加载待同步数据失败: $e');
      Fluttertoast.showToast(msg: '加载数据失败');
    } finally {
      isLoading.value = false;
    }
  }

  Future<void> syncAll() async {
    if (isSyncing.value) return;

    final isConnected = await _connectivityService.checkConnection();
    if (!isConnected) {
      Fluttertoast.showToast(msg: '网络连接不可用，请检查网络');
      return;
    }

    isSyncing.value = true;
    syncProgress.value = 0;
    syncSuccess.value = 0;
    syncFailed.value = 0;
    syncErrors.clear();
    syncStatus.value = 'syncing';

    try {
      final allItems = [
        ...pendingSamples.map((s) => {'type': 'sample', 'data': s}),
        ...pendingDetectResults.map((r) => {'type': 'result', 'data': r}),
      ];

      syncTotal.value = allItems.length;

      if (syncTotal.value == 0) {
        syncStatus.value = 'completed';
        Fluttertoast.showToast(msg: '没有需要同步的数据');
        return;
      }

      for (var i = 0; i < allItems.length; i++) {
        final item = allItems[i];
        final type = item['type'] as String;
        final data = item['data'];

        syncProgress.value = i + 1;

        try {
          if (type == 'sample') {
            currentSyncItem.value = '正在同步样品: ${(data as SampleModel).sampleCode ?? ''}';
            await _syncSample(data);
          } else {
            currentSyncItem.value = '正在同步检测结果: ${(data as DetectResultModel).detectItemName ?? ''}';
            await _syncDetectResult(data);
          }
          syncSuccess.value++;
        } catch (e) {
          _logger.e('同步失败: $e');
          syncFailed.value++;
          String errorMsg = e.toString();
          if (errorMsg.contains('DioError')) {
            errorMsg = '网络请求失败';
          }
          syncErrors.add('${type == 'sample' ? '样品' : '检测结果'} ${type == 'sample' ? (data as SampleModel).sampleCode : (data as DetectResultModel).detectItemName}: $errorMsg');
        }

        await Future.delayed(const Duration(milliseconds: 300));
      }

      syncStatus.value = 'completed';

      if (syncFailed.value == 0) {
        Fluttertoast.showToast(msg: '同步完成，成功 ${syncSuccess.value} 条');
      } else {
        Fluttertoast.showToast(msg: '同步完成，成功 ${syncSuccess.value} 条，失败 ${syncFailed.value} 条');
      }

      await loadPendingData();
    } catch (e) {
      _logger.e('同步过程出错: $e');
      syncStatus.value = 'failed';
      Fluttertoast.showToast(msg: '同步失败: $e');
    } finally {
      isSyncing.value = false;
      currentSyncItem.value = '';
    }
  }

  Future<void> _syncSample(SampleModel sample) async {
    try {
      final response = await _dioService.post(
        ApiConfig.sampleSync,
        data: sample.toRegisterJson(),
      );

      if (response.statusCode == 200 && response.data['code'] == 200) {
        if (sample.id != null) {
          await _databaseService.updateSampleSyncStatus(sample.id!, 'synced');
        }
        _logger.i('样品同步成功: ${sample.sampleCode}');
      } else {
        throw Exception(response.data['message'] ?? '同步失败');
      }
    } catch (e) {
      rethrow;
    }
  }

  Future<void> _syncDetectResult(DetectResultModel result) async {
    try {
      final response = await _dioService.post(
        ApiConfig.detectResultSync,
        data: result.toSubmitJson(),
      );

      if (response.statusCode == 200 && response.data['code'] == 200) {
        if (result.id != null) {
          await _databaseService.updateDetectResultSyncStatus(result.id!, 'synced');
        }
        _logger.i('检测结果同步成功: ${result.detectItemName}');
      } else {
        throw Exception(response.data['message'] ?? '同步失败');
      }
    } catch (e) {
      rethrow;
    }
  }

  Future<void> syncSamplesOnly() async {
    if (isSyncing.value) return;

    final isConnected = await _connectivityService.checkConnection();
    if (!isConnected) {
      Fluttertoast.showToast(msg: '网络连接不可用，请检查网络');
      return;
    }

    if (pendingSamples.isEmpty) {
      Fluttertoast.showToast(msg: '没有需要同步的样品');
      return;
    }

    isSyncing.value = true;
    syncProgress.value = 0;
    syncSuccess.value = 0;
    syncFailed.value = 0;
    syncErrors.clear();
    syncStatus.value = 'syncing';
    syncTotal.value = pendingSamples.length;

    try {
      for (var i = 0; i < pendingSamples.length; i++) {
        final sample = pendingSamples[i];
        syncProgress.value = i + 1;
        currentSyncItem.value = '正在同步样品: ${sample.sampleCode ?? ''}';

        try {
          await _syncSample(sample);
          syncSuccess.value++;
        } catch (e) {
          syncFailed.value++;
          syncErrors.add('样品 ${sample.sampleCode}: ${e.toString().contains('DioError') ? '网络请求失败' : e.toString()}');
        }

        await Future.delayed(const Duration(milliseconds: 300));
      }

      syncStatus.value = 'completed';
      Fluttertoast.showToast(msg: '样品同步完成，成功 ${syncSuccess.value} 条，失败 ${syncFailed.value} 条');
      await loadPendingData();
    } catch (e) {
      syncStatus.value = 'failed';
      Fluttertoast.showToast(msg: '同步失败: $e');
    } finally {
      isSyncing.value = false;
      currentSyncItem.value = '';
    }
  }

  Future<void> syncResultsOnly() async {
    if (isSyncing.value) return;

    final isConnected = await _connectivityService.checkConnection();
    if (!isConnected) {
      Fluttertoast.showToast(msg: '网络连接不可用，请检查网络');
      return;
    }

    if (pendingDetectResults.isEmpty) {
      Fluttertoast.showToast(msg: '没有需要同步的检测结果');
      return;
    }

    isSyncing.value = true;
    syncProgress.value = 0;
    syncSuccess.value = 0;
    syncFailed.value = 0;
    syncErrors.clear();
    syncStatus.value = 'syncing';
    syncTotal.value = pendingDetectResults.length;

    try {
      for (var i = 0; i < pendingDetectResults.length; i++) {
        final result = pendingDetectResults[i];
        syncProgress.value = i + 1;
        currentSyncItem.value = '正在同步检测结果: ${result.detectItemName ?? ''}';

        try {
          await _syncDetectResult(result);
          syncSuccess.value++;
        } catch (e) {
          syncFailed.value++;
          syncErrors.add('检测结果 ${result.detectItemName}: ${e.toString().contains('DioError') ? '网络请求失败' : e.toString()}');
        }

        await Future.delayed(const Duration(milliseconds: 300));
      }

      syncStatus.value = 'completed';
      Fluttertoast.showToast(msg: '检测结果同步完成，成功 ${syncSuccess.value} 条，失败 ${syncFailed.value} 条');
      await loadPendingData();
    } catch (e) {
      syncStatus.value = 'failed';
      Fluttertoast.showToast(msg: '同步失败: $e');
    } finally {
      isSyncing.value = false;
      currentSyncItem.value = '';
    }
  }

  double get progressPercent {
    if (syncTotal.value == 0) return 0.0;
    return syncProgress.value / syncTotal.value;
  }
}
