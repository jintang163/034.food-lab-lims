import 'package:json_annotation/json_annotation.dart';

part 'form_field_model.g.dart';

@JsonSerializable()
class FormFieldModel {
  String? key;
  String? type;
  String? label;
  String? placeholder;
  String? description;
  String? unit;
  bool? required;
  bool? readOnly;
  bool? hidden;
  int? maxLines;
  int? maxLength;
  int? precision;
  num? min;
  num? max;
  String? resultType;
  String? judgeStatus;
  dynamic defaultValue;
  List<FormFieldOptionModel>? options;
  List<String>? allowedFileTypes;
  int? maxFileCount;
  String? validationPattern;
  String? validationMessage;
  Map<String, dynamic>? widgetConfig;

  FormFieldModel({
    this.key,
    this.type,
    this.label,
    this.placeholder,
    this.description,
    this.unit,
    this.required,
    this.readOnly,
    this.hidden,
    this.maxLines,
    this.maxLength,
    this.precision,
    this.min,
    this.max,
    this.resultType,
    this.judgeStatus,
    this.defaultValue,
    this.options,
    this.allowedFileTypes,
    this.maxFileCount,
    this.validationPattern,
    this.validationMessage,
    this.widgetConfig,
  });

  factory FormFieldModel.fromJson(Map<String, dynamic> json) => _$FormFieldModelFromJson(json);

  Map<String, dynamic> toJson() => _$FormFieldModelToJson(this);

  bool get isRequired => required == true;
  bool get isReadOnly => readOnly == true;
  bool get isHidden => hidden == true;
  bool get isResultField => resultType != null;
  bool get isQualified => judgeStatus == 'qualified';
  bool get isUnqualified => judgeStatus == 'unqualified';
}

@JsonSerializable()
class FormFieldOptionModel {
  dynamic value;
  String? label;
  String? description;
  bool? disabled;

  FormFieldOptionModel({
    this.value,
    this.label,
    this.description,
    this.disabled,
  });

  factory FormFieldOptionModel.fromJson(Map<String, dynamic> json) => _$FormFieldOptionModelFromJson(json);

  Map<String, dynamic> toJson() => _$FormFieldOptionModelToJson(this);
}
