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
  List<String>? accept;
  bool? multiple;
  int? maxFileCount;
  String? validationPattern;
  String? validationMessage;
  Map<String, dynamic>? widgetConfig;
  int? sortOrder;

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
    this.accept,
    this.multiple,
    this.maxFileCount,
    this.validationPattern,
    this.validationMessage,
    this.widgetConfig,
    this.sortOrder,
  });

  factory FormFieldModel.fromJson(Map<String, dynamic> json) {
    return FormFieldModel(
      key: json['key'] as String?,
      type: json['type'] as String?,
      label: json['label'] as String?,
      placeholder: json['placeholder'] as String?,
      description: json['description'] as String?,
      unit: json['unit'] as String?,
      required: json['required'] as bool?,
      readOnly: json['readOnly'] as bool?,
      hidden: json['hidden'] as bool?,
      maxLines: json['maxLines'] as int?,
      maxLength: json['maxLength'] as int?,
      precision: json['precision'] as int?,
      min: json['min'] as num?,
      max: json['max'] as num?,
      resultType: json['resultType'] as String?,
      judgeStatus: json['judgeStatus'] as String?,
      defaultValue: json['defaultValue'],
      options: json['options'] != null
          ? (json['options'] as List).map((e) => FormFieldOptionModel.fromJson(e as Map<String, dynamic>)).toList()
          : null,
      accept: json['accept'] != null ? List<String>.from(json['accept'] as List) : null,
      multiple: json['multiple'] as bool?,
      maxFileCount: json['maxFileCount'] as int?,
      validationPattern: json['validationPattern'] as String?,
      validationMessage: json['validationMessage'] as String?,
      widgetConfig: json['widgetConfig'] as Map<String, dynamic>?,
      sortOrder: json['sortOrder'] as int?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (key != null) 'key': key,
      if (type != null) 'type': type,
      if (label != null) 'label': label,
      if (placeholder != null) 'placeholder': placeholder,
      if (description != null) 'description': description,
      if (unit != null) 'unit': unit,
      if (required != null) 'required': required,
      if (readOnly != null) 'readOnly': readOnly,
      if (hidden != null) 'hidden': hidden,
      if (maxLines != null) 'maxLines': maxLines,
      if (maxLength != null) 'maxLength': maxLength,
      if (precision != null) 'precision': precision,
      if (min != null) 'min': min,
      if (max != null) 'max': max,
      if (resultType != null) 'resultType': resultType,
      if (defaultValue != null) 'defaultValue': defaultValue,
      if (options != null) 'options': options!.map((e) => e.toJson()).toList(),
      if (accept != null) 'accept': accept,
      if (multiple != null) 'multiple': multiple,
      if (maxFileCount != null) 'maxFileCount': maxFileCount,
      if (validationPattern != null) 'validationPattern': validationPattern,
      if (validationMessage != null) 'validationMessage': validationMessage,
      if (widgetConfig != null) 'widgetConfig': widgetConfig,
      if (sortOrder != null) 'sortOrder': sortOrder,
    };
  }

  bool get isRequired => required == true;
  bool get isResultField => resultType != null;
  bool get isQualified => judgeStatus == 'qualified';
  bool get isUnqualified => judgeStatus == 'unqualified';
}

class FormFieldOptionModel {
  dynamic value;
  String? label;
  String? description;
  bool? disabled;

  FormFieldOptionModel({this.value, this.label, this.description, this.disabled});

  factory FormFieldOptionModel.fromJson(Map<String, dynamic> json) {
    return FormFieldOptionModel(
      value: json['value'],
      label: json['label'] as String?,
      description: json['description'] as String?,
      disabled: json['disabled'] as bool?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'value': value,
      if (label != null) 'label': label,
      if (description != null) 'description': description,
      if (disabled != null) 'disabled': disabled,
    };
  }
}
