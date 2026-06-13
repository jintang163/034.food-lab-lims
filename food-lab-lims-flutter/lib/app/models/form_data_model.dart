class FormDataModel {
  int? id;
  String? dataCode;
  int? templateId;
  String? templateCode;
  int? templateVersion;
  int? detectItemId;
  int? sampleId;
  String? sampleCode;
  int? taskId;
  Map<String, dynamic>? formData;
  String? submitTime;
  int? submittedBy;
  String? submittedByName;
  String? formStatus;
  String? syncStatus;
  String? offlineId;
  String? deviceId;
  String? remark;

  FormDataModel({
    this.id,
    this.dataCode,
    this.templateId,
    this.templateCode,
    this.templateVersion,
    this.detectItemId,
    this.sampleId,
    this.sampleCode,
    this.taskId,
    this.formData,
    this.submitTime,
    this.submittedBy,
    this.submittedByName,
    this.formStatus,
    this.syncStatus,
    this.offlineId,
    this.deviceId,
    this.remark,
  });

  factory FormDataModel.fromJson(Map<String, dynamic> json) {
    Map<String, dynamic>? parsedFormData;
    if (json['formData'] != null) {
      if (json['formData'] is Map) {
        parsedFormData = Map<String, dynamic>.from(json['formData'] as Map);
      } else if (json['formData'] is String && (json['formData'] as String).isNotEmpty) {
        try {
          parsedFormData = Map<String, dynamic>.from(jsonDecode(json['formData'] as String) as Map);
        } catch (_) {
          parsedFormData = null;
        }
      }
    }
    return FormDataModel(
      id: json['id'] as int?,
      dataCode: json['dataCode'] as String?,
      templateId: json['templateId'] as int?,
      templateCode: json['templateCode'] as String?,
      templateVersion: json['templateVersion'] as int?,
      detectItemId: json['detectItemId'] as int?,
      sampleId: json['sampleId'] as int?,
      sampleCode: json['sampleCode'] as String?,
      taskId: json['taskId'] as int?,
      formData: parsedFormData,
      submitTime: json['submitTime'] as String?,
      submittedBy: json['submittedBy'] as int?,
      submittedByName: json['submittedByName'] as String?,
      formStatus: json['status'] as String? ?? json['formStatus'] as String?,
      syncStatus: json['syncStatus'] as String?,
      offlineId: json['offlineId'] as String?,
      deviceId: json['deviceId'] as String?,
      remark: json['remark'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      if (dataCode != null) 'dataCode': dataCode,
      if (templateId != null) 'templateId': templateId,
      if (templateCode != null) 'templateCode': templateCode,
      if (templateVersion != null) 'templateVersion': templateVersion,
      if (detectItemId != null) 'detectItemId': detectItemId,
      if (sampleId != null) 'sampleId': sampleId,
      if (sampleCode != null) 'sampleCode': sampleCode,
      if (taskId != null) 'taskId': taskId,
      if (formData != null) 'formData': formData,
      if (submitTime != null) 'submitTime': submitTime,
      if (submittedBy != null) 'submittedBy': submittedBy,
      if (submittedByName != null) 'submittedByName': submittedByName,
      'status': formStatus ?? 'draft',
      if (syncStatus != null) 'syncStatus': syncStatus,
      if (offlineId != null) 'offlineId': offlineId,
      if (deviceId != null) 'deviceId': deviceId,
      if (remark != null) 'remark': remark,
    };
  }

  Map<String, dynamic> toSubmitJson() {
    return {
      if (id != null) 'id': id,
      'templateId': templateId,
      if (templateCode != null) 'templateCode': templateCode,
      if (templateVersion != null) 'templateVersion': templateVersion,
      if (detectItemId != null) 'detectItemId': detectItemId,
      if (sampleId != null) 'sampleId': sampleId,
      if (sampleCode != null) 'sampleCode': sampleCode,
      if (taskId != null) 'taskId': taskId,
      'formData': formData,
      if (remark != null) 'remark': remark,
    };
  }

  bool get isDraft => formStatus == 'draft';
  bool get isSubmitted => formStatus == 'submitted';
  bool get isSynced => syncStatus == 'synced';
  bool get isPendingSync => syncStatus == 'pending';

  void updateFieldValue(String key, dynamic value) {
    formData ??= {};
    formData![key] = value;
  }

  dynamic getFieldValue(String key) => formData?[key];
}
