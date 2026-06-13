import 'dart:convert';
import 'dart:math';
import 'package:get/get.dart';
import 'package:logger/logger.dart';
import 'package:fluttertoast/fluttertoast.dart';

import '../../services/dio_service.dart';
import '../../services/database_service.dart';
import '../../services/connectivity_service.dart';
import '../../services/storage_service.dart';
import '../../services/form_service.dart';
import '../../config/api_config.dart';
import '../../models/detect_result_model.dart';
import '../../models/detect_item_model.dart';
import '../../models/task_model.dart';

class DetectController extends GetxController {
  final DioService _dioService = Get.find<DioService>();
  final DatabaseService _databaseService = Get.find<DatabaseService>();
  final ConnectivityService _connectivityService = Get.find<ConnectivityService>();
  final StorageService _storageService = Get.find<StorageService>();
  final Logger _logger = Logger();
  final Random _random = Random();

  final Rx<DetectItemModel?> detectItem = Rx<DetectItemModel?>(null);
  final Rx<DetectResultModel> detectResult = DetectResultModel().obs;
  final RxMap<String, dynamic> formData = <String, dynamic>{}.obs;
  final RxList<Map<String, dynamic>> formSchema = <Map<String, dynamic>>[].obs;
  final RxBool isLoading = false.obs;
  final RxBool isSubmitting = false.obs;
  final RxString judgeResult = ''.obs;
  final RxString judgeStatus = ''.obs;

  final RxList<DetectItemModel> detectItemList = <DetectItemModel>[].obs;
  final RxInt currentItemIndex = 0.obs;
  final RxBool showItemList = true.obs;

  int? taskId;
  int? sampleId;
  String? sampleCode;
  int? detectItemId;
  String? detectItemName;

  @override
  void onInit() {
    super.onInit();
    ever(formData, (_) => _autoJudge());
    _initFromArguments();
  }

  void _initFromArguments() {
    final args = Get.arguments;
    if (args is Map<String, dynamic>) {
      taskId = args['taskId'] as int?;
      sampleId = args['sampleId'] as int?;
      sampleCode = args['sampleCode'] as String?;
      detectItemId = args['detectItemId'] as int?;
      detectItemName = args['detectItemName'] as String?;
      showItemList.value = detectItemId == null;
    } else if (args is TaskModel) {
      taskId = args.id;
      sampleId = args.sampleId;
      sampleCode = args.sampleCode;
      showItemList.value = true;
    }

    if (detectItemId != null) {
      loadFormSchema(detectItemId!);
    } else if (taskId != null) {
      loadDetectItemsForTask();
    }
  }

  Future<void> loadDetectItemsForTask() async {
    isLoading.value = true;
    try {
      bool isConnected = await _connectivityService.checkConnection();
      if (!isConnected) {
        Fluttertoast.showToast(msg: '网络不可用，无法加载检测项目');
        isLoading.value = false;
        return;
      }

      final response = await _dioService.get(
        ApiConfig.detectItemList,
      );

      if (response.statusCode == 200) {
        final result = response.data;
        if (result['code'] == 200) {
          final List<dynamic> records = result['data'] ?? [];
          detectItemList.value =
              records.map((e) => DetectItemModel.fromJson(e)).toList();
        } else {
          Fluttertoast.showToast(msg: result['message'] ?? '加载失败');
        }
      }
    } catch (e) {
      _logger.e('加载检测项目列表失败: $e');
      Fluttertoast.showToast(msg: '加载失败: ${e.toString()}');
    } finally {
      isLoading.value = false;
    }
  }

  void selectDetectItem(DetectItemModel item) {
    detectItemId = item.id;
    detectItemName = item.itemName;
    detectItem.value = item;
    showItemList.value = false;
    loadFormSchema(item.id!);
  }

  void backToList() {
    showItemList.value = true;
    formSchema.clear();
    formData.clear();
    detectItem.value = null;
    judgeResult.value = '';
    judgeStatus.value = '';
  }

  Future<void> loadFormSchema(int detectItemId) async {
    isLoading.value = true;
    try {
      final FormService formService = Get.find<FormService>();
      final formTemplate = await formService.getCurrentTemplateByDetectItem(detectItemId);
      if (formTemplate != null && formTemplate.formSchema != null) {
        final fields = formTemplate.parseFields();
        formSchema.value = fields.map((f) => f.toJson()).toList();
        _initFormData();
        return;
      }

      final isConnected = await _connectivityService.checkConnection();
      if (!isConnected) {
        Fluttertoast.showToast(msg: '网络不可用');
        return;
      }

      final response = await _dioService.get(
        '${ApiConfig.detectItemFormSchema}/$detectItemId',
      );

      if (response.statusCode == 200 && response.data['code'] == 200) {
        final data = response.data['data'];
        if (data['formSchema'] != null) {
          formSchema.value = List<Map<String, dynamic>>.from(
            json.decode(data['formSchema']),
          );
        }
        if (data['limitStandards'] != null) {
          detectItem.value = DetectItemModel.fromJson(data);
        }
        _initFormData();
      }
    } catch (e) {
      _logger.e('加载表单Schema失败: $e');
      Fluttertoast.showToast(msg: '加载表单失败，请重试');
    } finally {
      isLoading.value = false;
    }
  }

  String _generateUuid() {
    const chars = '0123456789abcdefghijklmnopqrstuvwxyz';
    final now = DateTime.now().millisecondsSinceEpoch;
    final random = List<int>.generate(16, (i) => _random.nextInt(256));
    random[6] = (random[6] & 0x0f) | 0x40;
    random[8] = (random[8] & 0x3f) | 0x80;
    final buffer = StringBuffer();
    for (var i = 0; i < 16; i++) {
      buffer.write(chars[random[i] % 16]);
      if (i == 3 || i == 5 || i == 7 || i == 9) {
        buffer.write('-');
      }
    }
    return '${buffer.toString()}-$now';
  }

  void _initFormData() {
    for (var field in formSchema) {
      final key = field['key'] as String;
      final type = field['type'] as String;
      if (type == 'number') {
        formData[key] = null;
      } else if (type == 'select') {
        formData[key] = null;
      } else {
        formData[key] = '';
      }
    }
    detectResult.value = DetectResultModel(
      offlineId: _generateUuid(),
      detectTime: DateTime.now().toIso8601String(),
      syncStatus: 'pending',
      createTime: DateTime.now().toIso8601String(),
    );
  }

  void updateFormField(String key, dynamic value) {
    formData[key] = value;

    for (var field in formSchema) {
      if (field['key'] == key) {
        final resultType = field['resultType'] as String?;
        if (resultType == 'quantitative') {
          detectResult.update((val) {
            val?.resultType = 'quantitative';
            val?.resultValue = double.tryParse(value?.toString() ?? '');
            val?.resultUnit = field['unit'] as String?;
          });
        } else if (resultType == 'qualitative') {
          detectResult.update((val) {
            val?.resultType = 'qualitative';
            val?.qualitativeResult = value?.toString();
          });
        }
        break;
      }
    }
  }

  void _autoJudge() {
    if (detectItem.value?.limitStandards == null || detectItem.value!.limitStandards!.isEmpty) {
      judgeResult.value = '';
      judgeStatus.value = '';
      return;
    }

    final standard = detectItem.value!.limitStandards!.first;
    final limitType = standard.limitType;
    bool isQualified = false;

    if (detectResult.value.resultType == 'quantitative' && detectResult.value.resultValue != null) {
      final value = detectResult.value.resultValue!;
      final min = standard.limitValueMin;
      final max = standard.limitValueMax;

      switch (limitType) {
        case 'max':
          isQualified = max != null && value <= max;
          judgeResult.value = isQualified ? '合格' : '不合格';
          break;
        case 'min':
          isQualified = min != null && value >= min;
          judgeResult.value = isQualified ? '合格' : '不合格';
          break;
        case 'range':
          isQualified = (min != null && value >= min) && (max != null && value <= max);
          judgeResult.value = isQualified ? '合格' : '不合格';
          break;
        default:
          judgeResult.value = '';
      }
    } else if (detectResult.value.resultType == 'qualitative' && detectResult.value.qualitativeResult != null) {
      final expected = standard.qualitativeResult;
      final actual = detectResult.value.qualitativeResult;
      isQualified = expected == actual;
      judgeResult.value = isQualified ? '合格' : '不合格';
    } else {
      judgeResult.value = '';
      judgeStatus.value = '';
      return;
    }

    judgeStatus.value = isQualified ? 'qualified' : 'unqualified';
  }

  Future<bool> submitResult({
    required int taskId,
    required int sampleId,
    required String sampleCode,
    required int detectItemId,
    required String detectItemName,
  }) async {
    isSubmitting.value = true;
    try {
      detectResult.update((val) {
        val?.taskId = taskId;
        val?.sampleId = sampleId;
        val?.sampleCode = sampleCode;
        val?.detectItemId = detectItemId;
        val?.detectItemName = detectItemName;
        val?.limitStandardId = detectItem.value?.limitStandards?.first.id;
      });

      final isConnected = await _connectivityService.checkConnection();

      if (isConnected) {
        final response = await _dioService.post(
          ApiConfig.detectResultSubmit,
          data: detectResult.value.toSubmitJson(),
        );

        if (response.statusCode == 200 && response.data['code'] == 200) {
          detectResult.update((val) {
            val?.syncStatus = 'synced';
            val?.id = response.data['data']['id'];
          });
          await _saveToLocal();
          Fluttertoast.showToast(msg: '提交成功');
          return true;
        } else {
          Fluttertoast.showToast(msg: response.data['message'] ?? '提交失败');
          return false;
        }
      } else {
        await _saveToLocal();
        Fluttertoast.showToast(msg: '网络异常，已保存到本地');
        return true;
      }
    } catch (e) {
      _logger.e('提交检测结果失败: $e');
      await _saveToLocal();
      Fluttertoast.showToast(msg: '提交失败，已保存到本地');
      return false;
    } finally {
      isSubmitting.value = false;
    }
  }

  Future<void> _saveToLocal() async {
    try {
      await _databaseService.insertDetectResult(detectResult.value);
      _logger.i('检测结果已保存到本地: ${detectResult.value.offlineId}');
    } catch (e) {
      _logger.e('保存到本地失败: $e');
    }
  }

  void resetForm() {
    formData.clear();
    detectResult.value = DetectResultModel();
    judgeResult.value = '';
    judgeStatus.value = '';
    _initFormData();
  }
}
