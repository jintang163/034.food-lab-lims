import 'dart:async';
import 'dart:convert';
import 'package:get/get.dart';
import 'package:logger/logger.dart';
import 'package:connectivity_plus/connectivity_plus.dart';

import 'dio_service.dart';
import 'database_service.dart';
import 'connectivity_service.dart';
import 'form_service.dart';
import '../config/api_config.dart';
import '../models/form_data_model.dart';

class FormSyncService extends GetxService {
  final DioService _dioService = Get.find<DioService>();
  final DatabaseService _databaseService = Get.find<DatabaseService>();
  final ConnectivityService _connectivityService = Get.find<ConnectivityService>();
  final FormService _formService = Get.find<FormService>();
  final Logger _logger = Logger();

  final RxBool isSyncing = false.obs;
  final RxInt pendingCount = 0.obs;
  final RxInt syncSuccessCount = 0.obs;
  final RxInt syncFailCount = 0.obs;
  final RxString syncStatus = 'idle'.obs;
  final RxList<String> syncLogs = <String>[].obs;

  Timer? _autoSyncTimer;
  StreamSubscription<ConnectivityResult>? _connectivitySubscription;

  @override
  void onInit() {
    super.onInit();
    _initSyncService();
  }

  @override
  void onClose() {
    _autoSyncTimer?.cancel();
    _connectivitySubscription?.cancel();
    super.onClose();
  }

  void _initSyncService() {
    _formService.createFormDataTable();
    _loadPendingCount();
    _startAutoSync();
    _listenConnectivityChange();
  }

  void _loadPendingCount() async {
    final pendingList = await _formService.getPendingFormDataList();
    pendingCount.value = pendingList.length;
  }

  void _startAutoSync() {
    _autoSyncTimer = Timer.periodic(const Duration(minutes: 5), (_) {
      if (!isSyncing.value) {
        syncAllPendingData();
      }
    });
    _logger.i('自动同步已启动，每5分钟执行一次');
  }

  void _listenConnectivityChange() {
    _connectivitySubscription = _connectivityService.connectivityStream.listen((result) {
      if (result != ConnectivityResult.none && !isSyncing.value) {
        _addLog('网络已恢复，开始同步数据...');
        syncAllPendingData();
      }
    });
  }

  Future<void> syncAllPendingData() async {
    if (isSyncing.value) {
      _logger.w('已有同步任务在进行中');
      return;
    }

    final isConnected = await _connectivityService.checkConnection();
    if (!isConnected) {
      _addLog('网络不可用，无法同步');
      return;
    }

    isSyncing.value = true;
    syncStatus.value = 'syncing';
    syncSuccessCount.value = 0;
    syncFailCount.value = 0;

    try {
      _addLog('开始同步待提交数据...');
      final pendingList = await _formService.getPendingFormDataList();
      pendingCount.value = pendingList.length;

      if (pendingList.isEmpty) {
        _addLog('没有待同步的数据');
        syncStatus.value = 'completed';
        return;
      }

      _addLog('共 ${pendingList.length} 条待同步数据');

      for (var formData in pendingList) {
        await _syncSingleFormData(formData);
      }

      _addLog('同步完成：成功 ${syncSuccessCount.value} 条，失败 ${syncFailCount.value} 条');
      syncStatus.value = syncFailCount.value > 0 ? 'partial' : 'completed';
    } catch (e) {
      _logger.e('同步数据失败: $e');
      _addLog('同步失败: ${e.toString()}');
      syncStatus.value = 'error';
    } finally {
      isSyncing.value = false;
      await _loadPendingCount();
    }
  }

  Future<bool> _syncSingleFormData(FormDataModel formData) async {
    try {
      _addLog('正在同步: ${formData.offlineId}');

      final response = await _dioService.post(
        formData.isSubmitted ? ApiConfig.formDataSync : ApiConfig.formDataSubmit,
        data: formData.toSubmitJson(),
      );

      if (response.statusCode == 200 && response.data['code'] == 200) {
        await _updateSyncStatus(formData, 'synced');
        syncSuccessCount.value++;
        _addLog('同步成功: ${formData.offlineId}');
        return true;
      } else {
        await _updateSyncStatus(formData, 'failed');
        syncFailCount.value++;
        _addLog('同步失败: ${formData.offlineId} - ${response.data['message']}');
        return false;
      }
    } catch (e) {
      _logger.e('同步单条数据失败: $e');
      await _updateSyncStatus(formData, 'failed');
      syncFailCount.value++;
      _addLog('同步异常: ${formData.offlineId} - ${e.toString()}');
      return false;
    }
  }

  Future<bool> syncFormData(FormDataModel formData) async {
    final isConnected = await _connectivityService.checkConnection();
    if (!isConnected) {
      _addLog('网络不可用，无法同步');
      return false;
    }

    return _syncSingleFormData(formData);
  }

  Future<void> batchSyncFormData(List<FormDataModel> dataList) async {
    if (dataList.isEmpty) return;

    final isConnected = await _connectivityService.checkConnection();
    if (!isConnected) {
      _addLog('网络不可用，无法同步');
      return;
    }

    isSyncing.value = true;
    syncStatus.value = 'syncing';

    try {
      _addLog('开始批量同步 ${dataList.length} 条数据...');

      for (var formData in dataList) {
        await _syncSingleFormData(formData);
      }

      _addLog('批量同步完成：成功 ${syncSuccessCount.value} 条，失败 ${syncFailCount.value} 条');
      syncStatus.value = syncFailCount.value > 0 ? 'partial' : 'completed';
    } catch (e) {
      _logger.e('批量同步失败: $e');
      _addLog('批量同步异常: ${e.toString()}');
      syncStatus.value = 'error';
    } finally {
      isSyncing.value = false;
      await _loadPendingCount();
    }
  }

  Future<void> _updateSyncStatus(FormDataModel formData, String status) async {
    try {
      final db = await _databaseService.database;
      await db.update(
        'form_data',
        {'sync_status': status},
        where: formData.id != null
            ? 'id = ?'
            : 'offline_id = ?',
        whereArgs: formData.id != null
            ? [formData.id]
            : [formData.offlineId],
      );
    } catch (e) {
      _logger.e('更新同步状态失败: $e');
    }
  }

  Future<Map<String, dynamic>> getSyncStatistics() async {
    final pendingList = await _formService.getPendingFormDataList();
    final total = pendingList.length;
    final submitted = pendingList.where((e) => e.isSubmitted).length;
    final draft = pendingList.where((e) => e.isDraft).length;

    return {
      'total': total,
      'pending': pendingList.where((e) => e.syncStatus == 'pending').length,
      'failed': pendingList.where((e) => e.syncStatus == 'failed').length,
      'synced': pendingList.where((e) => e.syncStatus == 'synced').length,
      'submitted': submitted,
      'draft': draft,
    };
  }

  Future<bool> retryFailedSync() async {
    final pendingList = await _formService.getPendingFormDataList();
    final failedList = pendingList.where((e) => e.syncStatus == 'failed').toList();

    if (failedList.isEmpty) {
      _addLog('没有同步失败的数据');
      return true;
    }

    _addLog('开始重试 ${failedList.length} 条失败数据...');

    bool allSuccess = true;
    for (var formData in failedList) {
      final success = await _syncSingleFormData(formData);
      if (!success) allSuccess = false;
    }

    return allSuccess;
  }

  Future<void> clearSyncedData() async {
    try {
      final db = await _databaseService.database;
      final count = await db.delete(
        'form_data',
        where: 'sync_status = ?',
        whereArgs: ['synced'],
      );
      _addLog('已清理 $count 条已同步的数据');
      await _loadPendingCount();
    } catch (e) {
      _logger.e('清理已同步数据失败: $e');
    }
  }

  void _addLog(String message) {
    final timestamp = DateTime.now().toString().substring(11, 19);
    syncLogs.add('[$timestamp] $message');
    if (syncLogs.length > 100) {
      syncLogs.removeAt(0);
    }
    _logger.i(message);
  }

  Map<String, dynamic> toDbMap(Map<String, dynamic> json) {
    return _databaseService.toDbMap(json);
  }

  Map<String, dynamic> fromDbMap(Map<String, dynamic> dbMap) {
    return _databaseService.fromDbMap(dbMap);
  }
}
