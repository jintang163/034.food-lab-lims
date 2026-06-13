import 'dart:convert';
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
      final response = await _dioService.get('${ApiConfig.formTemplateDetail}/$id');
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

  Future<FormTemplateModel?> getCurrentTemplateByDetectItem(int detectItemId) async {
    try {
      final isConnected = await _connectivityService.checkConnection();
      if (!isConnected) {
        Fluttertoast.showToast(msg: '网络不可用');
        return null;
      }
      final response = await _dioService.get('${ApiConfig.formTemplateCurrent}/$detectItemId');
      if (response.statusCode == 200 && response.data['code'] == 200) {
        return FormTemplateModel.fromJson(response.data['data']);
      }
      Fluttertoast.showToast(msg: response.data['message'] ?? '加载表单模板失败');
      return null;
    } catch (e) {
      _logger.e('获取当前模板失败: $e');
      Fluttertoast.showToast(msg: '加载表单模板失败');
      return null;
    }
  }

  Future<FormDataModel?> saveFormData(FormDataModel formData) async {
    try {
      final isConnected = await _connectivityService.checkConnection();
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
        await _saveFormDataToLocal(formData);
        Fluttertoast.showToast(msg: '网络异常，已保存到本地');
        return formData;
      }
    } catch (e) {
      _logger.e('保存表单数据失败: $e');
      formData.syncStatus = 'pending';
      formData.offlineId ??= _generateUuid();
      await _saveFormDataToLocal(formData);
      Fluttertoast.showToast(msg: '保存失败，已缓存到本地');
      return formData;
    }
  }

  Future<FormDataModel?> submitFormData(FormDataModel formData) async {
    try {
      final isConnected = await _connectivityService.checkConnection();
      formData.formStatus = 'submitted';
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
        await _saveFormDataToLocal(formData);
        Fluttertoast.showToast(msg: '网络异常，提交已缓存到本地');
        return formData;
      }
    } catch (e) {
      _logger.e('提交表单数据失败: $e');
      formData.syncStatus = 'pending';
      formData.offlineId ??= _generateUuid();
      await _saveFormDataToLocal(formData);
      Fluttertoast.showToast(msg: '提交失败，已缓存到本地');
      return formData;
    }
  }

  Future<bool> saveDraftToLocal(String draftKey, FormDataModel formData) async {
    try {
      formData.formStatus = 'draft';
      final draftJson = json.encode(formData.toJson());
      await _storageService.saveString('form_draft_$draftKey', draftJson);
      _logger.i('草稿已保存到本地: $draftKey');
      return true;
    } catch (e) {
      _logger.e('保存草稿失败: $e');
      return false;
    }
  }

  Future<FormDataModel?> getDraftFromLocal(String draftKey) async {
    try {
      final draftJson = _storageService.getString('form_draft_$draftKey');
      if (draftJson != null && draftJson.isNotEmpty) {
        return FormDataModel.fromJson(json.decode(draftJson));
      }
      return null;
    } catch (e) {
      _logger.e('获取草稿失败: $e');
      return null;
    }
  }

  Future<bool> deleteDraftFromLocal(String draftKey) async {
    try {
      await _storageService.remove('form_draft_$draftKey');
      _logger.i('草稿已删除: $draftKey');
      return true;
    } catch (e) {
      _logger.e('删除草稿失败: $e');
      return false;
    }
  }

  Future<bool> syncPendingData() async {
    try {
      final pendingList = await getPendingFormDataList();
      if (pendingList.isEmpty) return true;

      final isConnected = await _connectivityService.checkConnection();
      if (!isConnected) return false;

      final response = await _dioService.post(
        ApiConfig.formDataSync,
        data: {'dataList': pendingList.map((e) => e.toJson()).toList()},
      );

      if (response.statusCode == 200 && response.data['code'] == 200) {
        _logger.i('离线数据同步成功: ${pendingList.length}条');
        return true;
      }
      return false;
    } catch (e) {
      _logger.e('同步离线数据失败: $e');
      return false;
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
        final jsonMap = Map<String, dynamic>.from(map);
        if (jsonMap['formData'] is String && (jsonMap['formData'] as String).isNotEmpty) {
          try {
            jsonMap['formData'] = jsonDecode(jsonMap['formData'] as String);
          } catch (_) {}
        }
        return FormDataModel.fromJson(jsonMap);
      }).toList();
    } catch (e) {
      _logger.e('获取待同步表单数据失败: $e');
      return [];
    }
  }

  Future<int> _saveFormDataToLocal(FormDataModel formData) async {
    try {
      final db = await _databaseService.database;
      final map = formData.toJson();
      if (map['formData'] is Map) {
        map['formData'] = json.encode(map['formData']);
      }
      if (formData.id != null) {
        return await db.update('form_data', map, where: 'id = ?', whereArgs: [formData.id]);
      } else {
        final existing = await db.query('form_data',
            where: 'offline_id = ?', whereArgs: [formData.offlineId]);
        if (existing.isNotEmpty) {
          return await db.update('form_data', map,
              where: 'offline_id = ?', whereArgs: [formData.offlineId]);
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
        data_code TEXT,
        offline_id TEXT,
        template_id INTEGER,
        template_code TEXT,
        template_version INTEGER,
        task_id INTEGER,
        sample_id INTEGER,
        sample_code TEXT,
        detect_item_id INTEGER,
        form_data TEXT,
        submit_time TEXT,
        submitted_by INTEGER,
        submitted_by_name TEXT,
        status TEXT,
        sync_status TEXT DEFAULT 'pending',
        device_id TEXT,
        remark TEXT,
        create_time TEXT,
        update_time TEXT
      )
    ''');
    _logger.i('form_data 表已创建');
  }

  String _generateUuid() {
    final now = DateTime.now().millisecondsSinceEpoch;
    final random = List<int>.generate(8, (i) => DateTime.now().microsecond % 256);
    return '${now}-${random.map((e) => e.toRadixString(16).padLeft(2, '0')).join()}';
  }
}
