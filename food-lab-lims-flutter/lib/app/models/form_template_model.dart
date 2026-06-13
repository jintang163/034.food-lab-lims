import 'form_field_model.dart';

class FormTemplateModel {
  int? id;
  String? templateCode;
  String? templateName;
  String? description;
  int? detectItemId;
  String? detectItemName;
  int? version;
  bool? isCurrent;
  String? status;
  String? formSchema;
  String? renderEngine;
  bool? enableAutoSave;
  int? autoSaveInterval;
  bool? enableDraft;
  String? remark;

  FormTemplateModel({
    this.id,
    this.templateCode,
    this.templateName,
    this.description,
    this.detectItemId,
    this.detectItemName,
    this.version,
    this.isCurrent,
    this.status,
    this.formSchema,
    this.renderEngine,
    this.enableAutoSave,
    this.autoSaveInterval,
    this.enableDraft,
    this.remark,
  });

  factory FormTemplateModel.fromJson(Map<String, dynamic> json) {
    return FormTemplateModel(
      id: json['id'] as int?,
      templateCode: json['templateCode'] as String?,
      templateName: json['templateName'] as String?,
      description: json['description'] as String?,
      detectItemId: json['detectItemId'] as int?,
      detectItemName: json['detectItemName'] as String?,
      version: json['version'] as int?,
      isCurrent: json['isCurrent'] as bool?,
      status: json['status'] as String?,
      formSchema: json['formSchema'] is Map ? 
        (json['formSchema'] as Map).toString() : json['formSchema'] as String?,
      renderEngine: json['renderEngine'] as String?,
      enableAutoSave: json['enableAutoSave'] as bool?,
      autoSaveInterval: json['autoSaveInterval'] as int?,
      enableDraft: json['enableDraft'] as bool?,
      remark: json['remark'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      if (templateCode != null) 'templateCode': templateCode,
      if (templateName != null) 'templateName': templateName,
      if (description != null) 'description': description,
      if (detectItemId != null) 'detectItemId': detectItemId,
      if (detectItemName != null) 'detectItemName': detectItemName,
      if (version != null) 'version': version,
      if (isCurrent != null) 'isCurrent': isCurrent,
      if (status != null) 'status': status,
      if (formSchema != null) 'formSchema': formSchema,
      if (renderEngine != null) 'renderEngine': renderEngine,
      if (remark != null) 'remark': remark,
    };
  }

  List<FormFieldModel> parseFields() {
    if (formSchema == null || formSchema!.isEmpty) return [];
    try {
      final Map<String, dynamic> schema =
          formSchema is String ? Map<String, dynamic>.from(
            (json.decode(formSchema!) as Map)) : 
            Map<String, dynamic>.from(formSchema as Map);
      if (schema['type'] != 'object' || schema['properties'] == null) return [];
      final properties = schema['properties'] as Map<String, dynamic>;
      final requiredList = List<String>.from(schema['required'] ?? []);
      final fields = <FormFieldModel>[];
      properties.forEach((key, value) {
        final prop = Map<String, dynamic>.from(value as Map);
        fields.add(FormFieldModel.fromJson({
          ...prop,
          'key': key,
          'required': requiredList.contains(key) || prop['required'] == true,
        }));
      });
      fields.sort((a, b) => (a.sortOrder ?? 999).compareTo(b.sortOrder ?? 999));
      return fields;
    } catch (e) {
      return [];
    }
  }

  bool get useFormBuilder => renderEngine == 'form_builder' || renderEngine == null;
  bool get isPublished => status == 'published';
  bool get isDraft => status == 'draft';
}
