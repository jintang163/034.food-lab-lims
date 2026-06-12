import 'package:flutter/foundation.dart';
import 'package:get/get.dart';
import 'package:logger/logger.dart';
import 'package:fluttertoast/fluttertoast.dart';

import '../../models/sample_model.dart';
import '../../models/detect_item_model.dart';
import '../../services/dio_service.dart';
import '../../services/database_service.dart';
import '../../services/connectivity_service.dart';
import '../../services/storage_service.dart';
import '../../config/api_config.dart';
import '../../constants/sample_constants.dart';

class SampleController extends GetxController {
  final Logger _logger = Logger();
  final DioService _dioService = Get.find<DioService>();
  final DatabaseService _databaseService = Get.find<DatabaseService>();
  final ConnectivityService _connectivityService = Get.find<ConnectivityService>();
  final StorageService _storageService = Get.find<StorageService>();

  final RxList<SampleModel> sampleList = <SampleModel>[].obs;
  final RxList<DetectItemModel> detectItemList = <DetectItemModel>[].obs;
  final RxList<int> selectedDetectItemIds = <int>[].obs;
  final RxBool isLoading = false.obs;
  final RxBool isSyncing = false.obs;
  final RxInt pageNum = 1.obs;
  final RxInt pageSize = 20.obs;
  final RxBool hasMore = true.obs;

  final RxString sampleName = ''.obs;
  final RxString batchNo = ''.obs;
  final RxString manufacturer = ''.obs;
  final RxString sampleLocation = ''.obs;
  final RxString samplePerson = ''.obs;
  final RxString sampleMethod = ''.obs;
  final RxString sampleAmount = ''.obs;
  final RxString sampleUnit = ''.obs;
  final RxString productionDate = ''.obs;
  final RxString shelfLife = ''.obs;
  final RxString remark = ''.obs;

  @override
  void onInit() {
    super.onInit();
    loadDetectItems();
    loadSamples();
  }

  Future<void> loadSamples({bool isRefresh = false}) async {
    if (isRefresh) {
      pageNum.value = 1;
      hasMore.value = true;
      sampleList.clear();
    }

    if (!hasMore.value) return;

    isLoading.value = true;

    try {
      bool isConnected = await _connectivityService.checkConnection();

      if (isConnected) {
        final response = await _dioService.get(
          ApiConfig.sampleList,
          params: {
            'pageNum': pageNum.value,
            'pageSize': pageSize.value,
          },
        );

        if (response.statusCode == 200) {
          final data = response.data;
          if (data['code'] == 200) {
            final List<dynamic> records = data['data']['records'] ?? [];
            final List<SampleModel> samples =
                records.map((e) => SampleModel.fromJson(e)).toList();

            if (samples.length < pageSize.value) {
              hasMore.value = false;
            }

            sampleList.addAll(samples);
            pageNum.value++;
          } else {
            Fluttertoast.showToast(msg: data['message'] ?? '加载失败');
          }
        }
      } else {
        final localSamples = await _databaseService.getAllSamples();
        sampleList.addAll(localSamples);
        hasMore.value = false;
        Fluttertoast.showToast(msg: '当前为离线模式，显示本地数据');
      }
    } catch (e) {
      _logger.e('加载样品列表失败: $e');
      Fluttertoast.showToast(msg: '加载失败: ${e.toString()}');
    } finally {
      isLoading.value = false;
    }
  }

  Future<void> loadDetectItems() async {
    try {
      bool isConnected = await _connectivityService.checkConnection();
      if (isConnected) {
        final response = await _dioService.get(ApiConfig.detectItemList);
        if (response.statusCode == 200) {
          final data = response.data;
          if (data['code'] == 200) {
            final List<dynamic> records = data['data'] ?? [];
            detectItemList.value =
                records.map((e) => DetectItemModel.fromJson(e)).toList();
          }
        }
      }
    } catch (e) {
      _logger.e('加载检测项目失败: $e');
    }
  }

  Future<void> saveOfflineSample() async {
    if (sampleName.value.isEmpty) {
      Fluttertoast.showToast(msg: '请输入样品名称');
      return;
    }
    if (selectedDetectItemIds.isEmpty) {
      Fluttertoast.showToast(msg: '请选择检测项目');
      return;
    }

    try {
      String? deviceId = _getDeviceId();
      String offlineId = 'OFF_${DateTime.now().millisecondsSinceEpoch}';

      SampleModel sample = SampleModel(
        offlineId: offlineId,
        sampleName: sampleName.value,
        batchNo: batchNo.value,
        manufacturer: manufacturer.value,
        productionDate: productionDate.value,
        shelfLife: shelfLife.value,
        sampleLocation: sampleLocation.value,
        sampleMethod: sampleMethod.value,
        samplePerson: samplePerson.value,
        sampleAmount: sampleAmount.value,
        sampleUnit: sampleUnit.value,
        remark: remark.value,
        detectItemIds: List.from(selectedDetectItemIds),
        syncStatus: SampleConstants.syncStatusPending,
        createTime: DateTime.now().toIso8601String(),
        deviceId: deviceId,
      );

      await _databaseService.insertSample(sample);
      Fluttertoast.showToast(msg: '离线登记成功');

      clearForm();
    } catch (e) {
      _logger.e('离线登记失败: $e');
      Fluttertoast.showToast(msg: '登记失败: ${e.toString()}');
    }
  }

  Future<void> submitSample() async {
    if (sampleName.value.isEmpty) {
      Fluttertoast.showToast(msg: '请输入样品名称');
      return;
    }
    if (selectedDetectItemIds.isEmpty) {
      Fluttertoast.showToast(msg: '请选择检测项目');
      return;
    }

    bool isConnected = await _connectivityService.checkConnection();
    if (!isConnected) {
      Fluttertoast.showToast(msg: '网络不可用，请先离线保存');
      return;
    }

    try {
      String? deviceId = _getDeviceId();
      String offlineId = 'ONL_${DateTime.now().millisecondsSinceEpoch}';

      SampleModel sample = SampleModel(
        offlineId: offlineId,
        sampleName: sampleName.value,
        batchNo: batchNo.value,
        manufacturer: manufacturer.value,
        productionDate: productionDate.value,
        shelfLife: shelfLife.value,
        sampleLocation: sampleLocation.value,
        sampleMethod: sampleMethod.value,
        samplePerson: samplePerson.value,
        sampleAmount: sampleAmount.value,
        sampleUnit: sampleUnit.value,
        remark: remark.value,
        detectItemIds: List.from(selectedDetectItemIds),
        deviceId: deviceId,
      );

      final response = await _dioService.post(
        ApiConfig.sampleRegister,
        data: sample.toRegisterJson(),
      );

      if (response.statusCode == 200) {
        final data = response.data;
        if (data['code'] == 200) {
          Fluttertoast.showToast(msg: '提交成功');
          clearForm();
          loadSamples(isRefresh: true);
        } else {
          Fluttertoast.showToast(msg: data['message'] ?? '提交失败');
        }
      }
    } catch (e) {
      _logger.e('提交样品失败: $e');
      Fluttertoast.showToast(msg: '提交失败: ${e.toString()}');
    }
  }

  Future<void> syncSamples() async {
    bool isConnected = await _connectivityService.checkConnection();
    if (!isConnected) {
      Fluttertoast.showToast(msg: '网络不可用，无法同步');
      return;
    }

    isSyncing.value = true;

    try {
      final pendingSamples = await _databaseService.getPendingSamples();
      if (pendingSamples.isEmpty) {
        Fluttertoast.showToast(msg: '没有待同步的数据');
        isSyncing.value = false;
        return;
      }

      final syncData = pendingSamples.map((e) => e.toSyncJson()).toList();

      final response = await _dioService.post(
        ApiConfig.sampleSync,
        data: {
          'samples': syncData,
          'deviceId': _storageService.getDeviceId(),
        },
      );

      if (response.statusCode == 200) {
        final result = response.data;
        if (result['code'] == 200) {
          final data = result['data'];
          final successCount = data['successCount'] ?? 0;
          final failCount = data['failCount'] ?? 0;
          final successSamples = data['successSamples'] as List? ?? [];

          for (var sampleData in successSamples) {
            final offlineId = sampleData['offlineId'];
            final serverId = sampleData['id'];
            final sampleCode = sampleData['sampleCode'];

            if (offlineId != null) {
              final localSample = pendingSamples.firstWhereOrNull(
                (s) => s.offlineId == offlineId,
              );
              if (localSample != null && localSample.id != null) {
                await _databaseService.updateSampleSyncStatus(
                  localSample.id!,
                  SampleConstants.syncStatusSynced,
                );
              }
            }
          }

          Fluttertoast.showToast(
            msg: '同步完成：成功$successCount条，失败$failCount条',
          );
          loadSamples(isRefresh: true);
        } else {
          Fluttertoast.showToast(msg: result['message'] ?? '同步失败');
        }
      }
    } catch (e) {
      _logger.e('同步失败: $e');
      Fluttertoast.showToast(msg: '同步失败: ${e.toString()}');
    } finally {
      isSyncing.value = false;
    }
  }

  String? _getDeviceId() {
    try {
      final deviceId = _storageService.getDeviceId();
      if (deviceId != null && deviceId.isNotEmpty) {
        return deviceId;
      }
      final userId = _storageService.getUserId();
      if (userId != null) {
        return 'user_$userId';
      }
      return 'device_${DateTime.now().millisecondsSinceEpoch}';
    } catch (e) {
      _logger.e('获取设备ID失败: $e');
      return null;
    }
  }

  void toggleDetectItem(int id) {
    if (selectedDetectItemIds.contains(id)) {
      selectedDetectItemIds.remove(id);
    } else {
      selectedDetectItemIds.add(id);
    }
  }

  void clearForm() {
    sampleName.value = '';
    batchNo.value = '';
    manufacturer.value = '';
    sampleLocation.value = '';
    samplePerson.value = '';
    sampleMethod.value = '';
    sampleAmount.value = '';
    sampleUnit.value = '';
    productionDate.value = '';
    shelfLife.value = '';
    remark.value = '';
    selectedDetectItemIds.clear();
  }

  void goToRegister() {
    clearForm();
    Get.toNamed('/sample/register');
  }
}
