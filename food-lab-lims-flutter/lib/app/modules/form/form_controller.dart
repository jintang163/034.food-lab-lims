import 'dart:async';
import 'dart:convert';
import 'package:get/get.dart';
import 'package:logger/logger.dart';
import 'package:fluttertoast/fluttertoast.dart';

import '../../services/form_service.dart';
import '../../services/form_sync_service.dart';
import '../../services/connectivity_service.dart';
import '../../services/storage_service.dart';
import '../../models/form_template_model.dart';
import '../../models/form_data_model.dart';
import '../../models/form_field_model.dart';

class FormController extends GetxController {
  final FormService _formService = Get.find<FormService>();
  final FormSyncService _formSyncService = Get.find<FormSyncService>();
  final ConnectivityService _connectivityService = Get.find<ConnectivityService>();
  final StorageService _storageService = Get.find<StorageService>();
  final Logger _logger = Logger();

  final Rx<FormTemplateModel?> template = Rx<FormTemplateModel?>(null);
  final Rx<FormDataModel?> formData = Rx<FormDataModel?>(null);
  final RxList<FormFieldModel> fields = <FormFieldModel>[].obs;
  final RxBool isLoading = false.obs;
  final RxBool isSubmitting = false.obs;
  final RxBool isSavingDraft = false.obs;
  final RxString renderEngine = 'form_builder'.obs;
  final RxString draftKey = ''.obs;

  Timer? _autoSaveTimer;
  int? _autoSaveInterval;

  int? templateId;
  String? templateCode;
  int? taskId;
  int? sampleId;
  String? sampleCode;
  int? detectItemId;
  String? detectItemName;

  @override
  void onInit() {
    super.onInit();
    _initFromArguments();
  }

  @override
  void onClose() {
    _autoSaveTimer?.cancel();
    super.onClose();
  }

  void _initFromArguments() {
    final args = Get.arguments;
    if (args is Map<String, dynamic>) {
      templateId = args['templateId'] as int?;
      templateCode = args['templateCode'] as String?;
      taskId = args['taskId'] as int?;
      sampleId = args['sampleId'] as int?;
      sampleCode = args['sampleCode'] as String?;
      detectItemId = args['detectItemId'] as int?;
      detectItemName = args['detectItemName'] as String?;

      if (templateId != null) {
        loadFormTemplate(templateId: templateId!);
      } else if (templateCode != null) {
        loadFormTemplate(templateCode: templateCode!);
      }
    }
  }

  Future<void> loadFormTemplate({int? templateId, String? templateCode}) async {
    isLoading.value = true;
    try {
      FormTemplateModel? loadedTemplate;

      if (templateId != null) {
        loadedTemplate = await _formService.getFormTemplateById(templateId);
      } else if (templateCode != null) {
        loadedTemplate = await _formService.getFormTemplateByCode(templateCode);
      }

      if (loadedTemplate != null) {
        template.value = loadedTemplate;
        fields.value = loadedTemplate.fields ?? [];
        renderEngine.value = loadedTemplate.renderEngine ?? 'form_builder';
        _autoSaveInterval = loadedTemplate.autoSaveInterval;

        _initDraftKey();
        await _loadDraft();
        _initFormData();
        _startAutoSave();
      }
    } catch (e) {
      _logger.e('加载表单模板失败: $e');
      Fluttertoast.showToast(msg: '加载表单失败，请重试');
    } finally {
      isLoading.value = false;
    }
  }

  void _initDraftKey() {
    final keyParts = <String>[];
    if (templateId != null) keyParts.add('tpl_$templateId');
    if (taskId != null) keyParts.add('task_$taskId');
    if (sampleId != null) keyParts.add('sample_$sampleId');
    if (detectItemId != null) keyParts.add('item_$detectItemId');

    draftKey.value = keyParts.isNotEmpty ? keyParts.join('_') : 'form_${template.value?.id}';
  }

  Future<void> _loadDraft() async {
    if (!template.value!.isDraftEnabled) return;

    try {
      final draft = await _formService.getDraft(draftKey.value);
      if (draft != null) {
        formData.value = draft;
        _logger.i('已恢复草稿: ${draftKey.value}');
        Fluttertoast.showToast(msg: '已恢复上次未提交的草稿');
      }
    } catch (e) {
      _logger.w('加载草稿失败: $e');
    }
  }

  void _initFormData() {
    if (formData.value == null) {
      final initialData = <String, dynamic>{};
      final judgeStatus = <String, dynamic>{};

      for (var field in fields) {
        if (field.key != null) {
          initialData[field.key!] = field.defaultValue;
        }
      }

      formData.value = FormDataModel(
        templateId: template.value?.id,
        templateCode: template.value?.templateCode,
        taskId: taskId,
        sampleId: sampleId,
        sampleCode: sampleCode,
        detectItemId: detectItemId,
        detectItemName: detectItemName,
        formData: initialData,
        fieldJudgeStatus: judgeStatus,
        formStatus: 'draft',
        syncStatus: 'pending',
        createTime: DateTime.now().toIso8601String(),
        draftKey: draftKey.value,
      );
    }
  }

  void _startAutoSave() {
    if (!template.value!.isAutoSaveEnabled) return;

    final interval = _autoSaveInterval ?? 30;
    _autoSaveTimer = Timer.periodic(Duration(seconds: interval), (_) {
      if (formData.value != null && !formData.value!.isSubmitted) {
        saveDraft();
      }
    });

    _logger.i('自动保存已启动，间隔 ${interval}秒');
  }

  void updateFieldValue(String key, dynamic value) {
    if (formData.value == null) return;

    formData.update((val) {
      val?.formData ??= {};
      val.formData![key] = value;
    });

    _autoJudgeField(key, value);
  }

  void _autoJudgeField(String key, dynamic value) {
    final field = fields.firstWhereOrNull((f) => f.key == key);
    if (field == null || !field.isResultField) return;

    final widgetConfig = field.widgetConfig;
    if (widgetConfig == null) return;

    final limitType = widgetConfig['limitType'] as String?;
    final limitMin = widgetConfig['limitMin'] as num?;
    final limitMax = widgetConfig['limitMax'] as num?;
    final expectedValue = widgetConfig['expectedValue'];

    bool isQualified = false;

    if (field.resultType == 'quantitative' && value is num) {
      switch (limitType) {
        case 'max':
          isQualified = limitMax != null && value <= limitMax;
          break;
        case 'min':
          isQualified = limitMin != null && value >= limitMin;
          break;
        case 'range':
          isQualified = (limitMin != null && value >= limitMin) &&
              (limitMax != null && value <= limitMax);
          break;
        case 'equal':
          isQualified = expectedValue != null && value == expectedValue;
          break;
      }
    } else if (field.resultType == 'qualitative') {
      isQualified = expectedValue != null && value == expectedValue;
    }

    final status = isQualified ? 'qualified' : 'unqualified';
    formData.update((val) {
      val?.fieldJudgeStatus ??= {};
      val.fieldJudgeStatus![key] = status;
      val.judgeStatus = status;
      val.judgeResult = isQualified ? '合格' : '不合格';
    });
  }

  Future<bool> saveDraft() async {
    if (formData.value == null || !template.value!.isDraftEnabled) return false;

    isSavingDraft.value = true;
    try {
      final success = await _formService.saveDraft(draftKey.value, formData.value!);
      if (success) {
        _logger.i('草稿已自动保存: ${draftKey.value}');
      }
      return success;
    } catch (e) {
      _logger.e('保存草稿失败: $e');
      return false;
    } finally {
      isSavingDraft.value = false;
    }
  }

  Future<bool> saveFormData() async {
    if (formData.value == null) return false;

    isSubmitting.value = true;
    try {
      formData.value!.formStatus = 'saved';
      final saved = await _formService.saveFormData(formData.value!);
      if (saved != null) {
        formData.value = saved;
        await _deleteDraft();
        Fluttertoast.showToast(msg: '保存成功');
        return true;
      }
      return false;
    } catch (e) {
      _logger.e('保存表单数据失败: $e');
      return false;
    } finally {
      isSubmitting.value = false;
    }
  }

  Future<bool> submitForm() async {
    if (!_validateForm()) {
      Fluttertoast.showToast(msg: '请检查表单填写是否正确');
      return false;
    }

    if (formData.value == null) return false;

    isSubmitting.value = true;
    try {
      final submitted = await _formService.submitFormData(formData.value!);
      if (submitted != null) {
        formData.value = submitted;
        await _deleteDraft();
        _autoSaveTimer?.cancel();
        return true;
      }
      return false;
    } catch (e) {
      _logger.e('提交表单失败: $e');
      return false;
    } finally {
      isSubmitting.value = false;
    }
  }

  bool _validateForm() {
    bool isValid = true;
    for (var field in fields) {
      if (field.key != null && !field.isHidden) {
        final value = formData.value?.formData?[field.key!];
        final error = FormFieldBuilder.validateField(field, value);
        if (error != null) {
          isValid = false;
          Fluttertoast.showToast(msg: error);
          break;
        }
      }
    }
    return isValid;
  }

  Future<void> _deleteDraft() async {
    try {
      await _formService.deleteDraft(draftKey.value);
    } catch (e) {
      _logger.w('删除草稿失败: $e');
    }
  }

  void resetForm() {
    formData.value = null;
    _initFormData();
    _deleteDraft();
  }

  void switchRenderEngine(String engine) {
    renderEngine.value = engine;
  }

  Future<void> syncPendingData() async {
    await _formSyncService.syncAllPendingData();
  }
}

extension _IterableExtension<T> on Iterable<T> {
  T? firstWhereOrNull(bool Function(T element) test) {
    for (var element in this) {
      if (test(element)) return element;
    }
    return null;
  }
}
