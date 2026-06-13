import 'package:json_annotation/json_annotation.dart';

part 'form_data_model.g.dart';

@JsonSerializable()
class FormDataModel {
  int? id;
  String? offlineId;
  int? templateId;
  String? templateCode;
  int? taskId;
  int? sampleId;
  String? sampleCode;
  int? detectItemId;
  String? detectItemName;
  Map<String, dynamic>? formData;
  Map<String, dynamic>? fieldJudgeStatus;
  String? judgeResult;
  String? judgeStatus;
  String? remark;
  List<String>? attachFiles;
  String? formStatus;
  String? syncStatus;
  String? createTime;
  String? updateTime;
  String? submitTime;
  int? createUser;
  int? updateUser;
  String? deviceId;
  String? draftKey;
  int? version;

  FormDataModel({
    this.id,
    this.offlineId,
    this.templateId,
    this.templateCode,
    this.taskId,
    this.sampleId,
    this.sampleCode,
    this.detectItemId,
    this.detectItemName,
    this.formData,
    this.fieldJudgeStatus,
    this.judgeResult,
    this.judgeStatus,
    this.remark,
    this.attachFiles,
    this.formStatus,
    this.syncStatus,
    this.createTime,
    this.updateTime,
    this.submitTime,
    this.createUser,
    this.updateUser,
    this.deviceId,
    this.draftKey,
    this.version,
  });

  factory FormDataModel.fromJson(Map<String, dynamic> json) => _$FormDataModelFromJson(json);

  Map<String, dynamic> toJson() => _$FormDataModelToJson(this);

  Map<String, dynamic> toSubmitJson() {
    return {
      'id': id,
      'templateId': templateId,
      'templateCode': templateCode,
      'taskId': taskId,
      'sampleId': sampleId,
      'sampleCode': sampleCode,
      'detectItemId': detectItemId,
      'detectItemName': detectItemName,
      'formData': formData,
      'fieldJudgeStatus': fieldJudgeStatus,
      'judgeResult': judgeResult,
      'judgeStatus': judgeStatus,
      'remark': remark,
      'attachFiles': attachFiles,
      'formStatus': formStatus,
    };
  }

  bool get isDraft => formStatus == 'draft';
  bool get isSubmitted => formStatus == 'submitted';
  bool get isSynced => syncStatus == 'synced';
  bool get isPendingSync => syncStatus == 'pending';
  bool get isFailedSync => syncStatus == 'failed';
  bool get isQualified => judgeStatus == 'qualified';
  bool get isUnqualified => judgeStatus == 'unqualified';

  void updateFieldValue(String key, dynamic value) {
    formData ??= {};
    formData![key] = value;
  }

  dynamic getFieldValue(String key) {
    return formData?[key];
  }

  void updateFieldJudgeStatus(String key, String status) {
    fieldJudgeStatus ??= {};
    fieldJudgeStatus![key] = status;
  }

  String? getFieldJudgeStatus(String key) {
    return fieldJudgeStatus?[key] as String?;
  }
}
