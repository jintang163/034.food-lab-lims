import 'dart:convert';
import 'dart:io';
import 'package:get/get.dart';
import 'package:logger/logger.dart';
import 'package:dio/dio.dart';
import 'package:fluttertoast/fluttertoast.dart';

import 'dio_service.dart';
import 'database_service.dart';
import 'connectivity_service.dart';
import 'storage_service.dart';
import '../config/api_config.dart';
import '../models/form_template_model.dart';
import '../models/form_data_model.dart';
import '../models/form_field_model.dart';

class FormService extends GetxService {
  final DioService _dioService = Get.find<DioService>();
  final DatabaseService _databaseService = Get.find<DatabaseService>();
  final ConnectivityService _connectivityService = Get.find<ConnectivityService>();
  final StorageService _storageService = Get.find<StorageService>();
  final Logger _logger = Logger();

  Future<FormTemplateModel?> getFormTemplateById(int id) async {
    try {
      final isConnected = await _connectivityService.checkConnection();
      if (!isConnected) {
        Fluttertoast.showToast(msg: '网络不可用');
        return null;
      }

      final response = await _dioService.get(
        '${ApiConfig.formTemplateDetail}/$id',
      );

      if (response.statusCode == 200 && response.data['code'] == 200) {
        return FormTemplateModel.fromJson(response.data['data']);
      }
      Fluttertoast.showToast(msg: response.data['message'] ?? '加载表单模板失败');
      return null;
    } catch (e) {
      _logger.e('获取表单模板失败: $e');
      Fluttertoast.showToast(msg: '加载表单模板失败');
      return null;
    }
  }

  Future<FormTemplateModel?> getFormTemplateByCode(String templateCode) async {
    try {
      final isConnected = await _connectivityService.checkConnection();
      if (!isConnected) {
        Fluttertoast.showToast(msg: '网络不可用');
        return null;
      }

      final response = await _dioService.get(
        '${ApiConfig.formTemplateByCode}/$templateCode',
      );

      if (response.statusCode == 200 && response.data['code'] == 200) {
        return FormTemplateModel.fromJson(response.data['data']);
      }
      Fluttertoast.showToast(msg: response.data['message'] ?? '加载表单模板失败');
      return null;
    } catch (e) {
      _logger.e('获取表单模板失败: $e');
      Fluttertoast.showToast(msg: '加载表单模板失败');
      return null;
    }
  }

  Future<List<FormFieldModel>?> getFormSchema(int templateId) async {
    try {
      final isConnected = await _connectivityService.checkConnection();
      if (!isConnected) {
        Fluttertoast.showToast(msg: '网络不可用');
        return null;
      }

      final response = await _dioService.get(
        '${ApiConfig.formTemplateSchema}/$templateId',
      );

      if (response.statusCode == 200 && response.data['code'] == 200) {
        final data = response.data['data'];
        if (data is List) {
          return data.map((e) => FormFieldModel.fromJson(e)).toList();
        }
      }
      Fluttertoast.showToast(msg: response.data['message'] ?? '加载表单Schema失败');
      return null;
    } catch (e) {
      _logger.e('获取表单Schema失败: $e');
      Fluttertoast.showToast(msg: '加载表单Schema失败');
      return null;
    }
  }

  Future<FormDataModel?> saveFormData(FormDataModel formData) async {
    try {
      final isConnected = await _connectivityService.checkConnection();
      formData.updateTime = DateTime.now().toIso8601String();

      if (isConnected) {
        final response = await _dioService.post(
          ApiConfig.formDataSave,
          data: formData.toJson(),
        );

        if (response.statusCode == 200 && response.data['code'] == 200) {
          final savedData = FormDataModel.fromJson(response.data['data']);
          savedData.syncStatus = 'synced';
          await _saveFormDataToLocal(savedData);
          return savedData;
        }
        Fluttertoast.showToast(msg: response.data['message'] ?? '保存失败');
        return null;
      } else {
        formData.syncStatus = 'pending';
        formData.offlineId ??= _generateUuid();
        formData.createTime ??= DateTime.now().toIso8601String();
        await _saveFormDataToLocal(formData);
        Fluttertoast.showToast(msg: '网络异常，已保存到本地');
        return formData;
      }
    } catch (e) {
      _logger.e('保存表单数据失败: $e');
      formData.syncStatus = 'pending';
      formData.offlineId ??= _generateUuid();
      formData.createTime ??= DateTime.now().toIso8601String();
      await _saveFormDataToLocal(formData);
      Fluttertoast.showToast(msg: '保存失败，已缓存到本地');
      return formData;
    }
  }

  Future<FormDataModel?> submitFormData(FormDataModel formData) async {
    try {
      final isConnected = await _connectivityService.checkConnection();
      formData.formStatus = 'submitted';
      formData.submitTime = DateTime.now().toIso8601String();
      formData.updateTime = DateTime.now().toIso8601String();

      if (isConnected) {
        final response = await _dioService.post(
          ApiConfig.formDataSubmit,
          data: formData.toSubmitJson(),
        );

        if (response.statusCode == 200 && response.data['code'] == 200) {
          final submittedData = FormDataModel.fromJson(response.data['data']);
          submittedData.syncStatus = 'synced';
          submittedData.formStatus = 'submitted';
          await _saveFormDataToLocal(submittedData);
          Fluttertoast.showToast(msg: '提交成功');
          return submittedData;
        }
        Fluttertoast.showToast(msg: response.data['message'] ?? '提交失败');
        return null;
      } else {
        formData.syncStatus = 'pending';
        formData.offlineId ??= _generateUuid();
        formData.createTime ??= DateTime.now().toIso8601String();
        await _saveFormDataToLocal(formData);
        Fluttertoast.showToast(msg: '网络异常，提交已缓存到本地');
        return formData;
      }
    } catch (e) {
      _logger.e('提交表单数据失败: $e');
      formData.syncStatus = 'pending';
      formData.offlineId ??= _generateUuid();
      formData.createTime ??= DateTime.now().toIso8601String();
      await _saveFormDataToLocal(formData);
      Fluttertoast.showToast(msg: '提交失败，已缓存到本地');
      return formData;
    }
  }

  Future<bool> saveDraft(String draftKey, FormDataModel formData) async {
    try {
      formData.draftKey = draftKey;
      formData.formStatus = 'draft';
      formData.updateTime = DateTime.now().toIso8601String();

      final draftJson = json.encode(formData.toJson());
      await _storageService.saveString('form_draft_$draftKey', draftJson);

      final isConnected = await _connectivityService.checkConnection();
      if (isConnected) {
        try {
          await _dioService.post(
            ApiConfig.formDraftSave,
            data: formData.toJson(),
          );
        } catch (e) {
          _logger.w('同步草稿到服务器失败: $e');
        }
      }

      _logger.i('草稿已保存: $draftKey');
      return true;
    } catch (e) {
      _logger.e('保存草稿失败: $e');
      return false;
    }
  }

  Future<FormDataModel?> getDraft(String draftKey) async {
    try {
      final draftJson = _storageService.getString('form_draft_$draftKey');
      if (draftJson != null && draftJson.isNotEmpty) {
        return FormDataModel.fromJson(json.decode(draftJson));
      }

      final isConnected = await _connectivityService.checkConnection();
      if (isConnected) {
        final response = await _dioService.get(
          '${ApiConfig.formDraftGet}/$draftKey',
        );
        if (response.statusCode == 200 && response.data['code'] == 200) {
          return FormDataModel.fromJson(response.data['data']);
        }
      }
      return null;
    } catch (e) {
      _logger.e('获取草稿失败: $e');
      return null;
    }
  }

  Future<bool> deleteDraft(String draftKey) async {
    try {
      await _storageService.remove('form_draft_$draftKey');

      final isConnected = await _connectivityService.checkConnection();
      if (isConnected) {
        try {
          await _dioService.delete(
            '${ApiConfig.formDraftDelete}/$draftKey',
          );
        } catch (e) {
          _logger.w('删除服务器草稿失败: $e');
        }
      }

      _logger.i('草稿已删除: $draftKey');
      return true;
    } catch (e) {
      _logger.e('删除草稿失败: $e');
      return false;
    }
  }

  Future<String?> uploadFile(File file, {String? fieldKey}) async {
    try {
      final isConnected = await _connectivityService.checkConnection();
      if (!isConnected) {
        Fluttertoast.showToast(msg: '网络不可用，无法上传文件');
        return null;
      }

      final formData = FormData.fromMap({
        'file': await MultipartFile.fromFile(file.path),
        if (fieldKey != null) 'fieldKey': fieldKey,
      });

      final response = await _dioService.upload(
        ApiConfig.formFileUpload,
        formData,
      );

      if (response.statusCode == 200 && response.data['code'] == 200) {
        final fileUrl = response.data['data']['fileUrl'] as String?;
        Fluttertoast.showToast(msg: '上传成功');
        return fileUrl;
      }
      Fluttertoast.showToast(msg: response.data['message'] ?? '上传失败');
      return null;
    } catch (e) {
      _logger.e('文件上传失败: $e');
      Fluttertoast.showToast(msg: '上传失败');
      return null;
    }
  }

  Future<List<FormDataModel>> getPendingFormDataList() async {
    try {
      final db = await _databaseService.database;
      final List<Map<String, dynamic>> maps = await db.query(
        'form_data',
        where: 'sync_status = ?',
        whereArgs: ['pending'],
        orderBy: 'create_time DESC',
      );
      return maps.map((map) {
        final json = _databaseService.fromDbMap(map);
        if (json['formData'] is String) {
          json['formData'] = json.decode(json['formData'] as String);
        }
        if (json['fieldJudgeStatus'] is String) {
          json['fieldJudgeStatus'] = json.decode(json['fieldJudgeStatus'] as String);
        }
        if (json['attachFiles'] is String) {
          json['attachFiles'] = (json['attachFiles'] as String).split(',');
        }
        return FormDataModel.fromJson(json);
      }).toList();
    } catch (e) {
      _logger.e('获取待同步表单数据失败: $e');
      return [];
    }
  }

  Future<int> _saveFormDataToLocal(FormDataModel formData) async {
    try {
      final db = await _databaseService.database;
      final map = _databaseService.toDbMap(formData.toJson());
      if (map['formData'] is Map) {
        map['formData'] = json.encode(map['formData']);
      }
      if (map['fieldJudgeStatus'] is Map) {
        map['fieldJudgeStatus'] = json.encode(map['fieldJudgeStatus']);
      }
      if (map['attachFiles'] is List) {
        map['attachFiles'] = (map['attachFiles'] as List).join(',');
      }

      if (formData.id != null) {
        return await db.update(
          'form_data',
          map,
          where: 'id = ?',
          whereArgs: [formData.id],
        );
      } else {
        final existing = await db.query(
          'form_data',
          where: 'offline_id = ?',
          whereArgs: [formData.offlineId],
        );
        if (existing.isNotEmpty) {
          return await db.update(
            'form_data',
            map,
            where: 'offline_id = ?',
            whereArgs: [formData.offlineId],
          );
        } else {
          return await db.insert('form_data', map);
        }
      }
    } catch (e) {
      _logger.e('保存表单数据到本地失败: $e');
      rethrow;
    }
  }

  Future<void> createFormDataTable() async {
    final db = await _databaseService.database;
    await db.execute('''
      CREATE TABLE IF NOT EXISTS form_data (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        offline_id TEXT,
        template_id INTEGER,
        template_code TEXT,
        task_id INTEGER,
        sample_id INTEGER,
        sample_code TEXT,
        detect_item_id INTEGER,
        detect_item_name TEXT,
        form_data TEXT,
        field_judge_status TEXT,
        judge_result TEXT,
        judge_status TEXT,
        remark TEXT,
        attach_files TEXT,
        form_status TEXT,
        sync_status TEXT DEFAULT 'pending',
        create_time TEXT,
        update_time TEXT,
        submit_time TEXT,
        create_user INTEGER,
        update_user INTEGER,
        device_id TEXT,
        draft_key TEXT,
        version INTEGER
      )
    ''');
    _logger.i('form_data 表已创建');
  }

  String _generateUuid() {
    const chars = '0123456789abcdefghijklmnopqrstuvwxyz';
    final now = DateTime.now().millisecondsSinceEpoch;
    final random = List<int>.generate(16, (i) => DateTime.now().microsecond % 256);
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
}
