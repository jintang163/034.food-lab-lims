import 'package:json_annotation/json_annotation.dart';

import 'form_field_model.dart';

part 'form_template_model.g.dart';

@JsonSerializable()
class FormTemplateModel {
  int? id;
  String? templateCode;
  String? templateName;
  String? description;
  String? category;
  int? version;
  String? status;
  List<FormFieldModel>? fields;
  Map<String, dynamic>? validationRules;
  Map<String, dynamic>? submitConfig;
  Map<String, dynamic>? layoutConfig;
  String? renderEngine;
  bool? enableAutoSave;
  int? autoSaveInterval;
  bool? enableDraft;
  bool? enableOffline;
  String? createTime;
  String? updateTime;
  int? createUser;
  int? updateUser;

  FormTemplateModel({
    this.id,
    this.templateCode,
    this.templateName,
    this.description,
    this.category,
    this.version,
    this.status,
    this.fields,
    this.validationRules,
    this.submitConfig,
    this.layoutConfig,
    this.renderEngine,
    this.enableAutoSave,
    this.autoSaveInterval,
    this.enableDraft,
    this.enableOffline,
    this.createTime,
    this.updateTime,
    this.createUser,
    this.updateUser,
  });

  factory FormTemplateModel.fromJson(Map<String, dynamic> json) => _$FormTemplateModelFromJson(json);

  Map<String, dynamic> toJson() => _$FormTemplateModelToJson(this);

  bool get useFormBuilder => renderEngine == 'form_builder' || renderEngine == null;
  bool get useJsonDynamic => renderEngine == 'json_dynamic';
  bool get isAutoSaveEnabled => enableAutoSave == true;
  bool get isDraftEnabled => enableDraft == true;
  bool get isOfflineEnabled => enableOffline == true;
}
