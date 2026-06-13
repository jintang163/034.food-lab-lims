import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:get/get.dart';
import 'package:json_dynamic_widget/json_dynamic_widget.dart';
import 'package:json_theme/json_theme.dart';

import '../../models/form_field_model.dart';
import '../../models/form_template_model.dart';
import '../../models/form_data_model.dart';
import 'form_field_builder.dart';

class JsonDynamicForm extends StatefulWidget {
  final FormTemplateModel template;
  final FormDataModel? initialData;
  final Function(FormDataModel data)? onChanged;
  final Function(FormDataModel data)? onSubmitted;
  final bool readOnly;
  final bool showSubmitButton;
  final String? submitButtonText;
  final Widget? submitButtonWidget;
  final Map<String, dynamic>? customRegistryFunctions;

  const JsonDynamicForm({
    super.key,
    required this.template,
    this.initialData,
    this.onChanged,
    this.onSubmitted,
    this.readOnly = false,
    this.showSubmitButton = true,
    this.submitButtonText,
    this.submitButtonWidget,
    this.customRegistryFunctions,
  });

  @override
  State<JsonDynamicForm> createState() => _JsonDynamicFormState();
}

class _JsonDynamicFormState extends State<JsonDynamicForm> {
  late final JsonWidgetRegistry _registry;
  late Map<String, dynamic> _formData;
  late Map<String, String> _fieldJudgeStatus;
  late FormDataModel _currentData;
  late List<FormFieldModel> _fields;
  final GlobalKey<FormState> _formKey = GlobalKey<FormState>();

  @override
  void initState() {
    super.initState();
    _fields = widget.template.parseFields();
    _initFormData();
    _initWidgetRegistry();
  }

  void _initFormData() {
    _formData = {};
    _fieldJudgeStatus = {};

    for (var field in _fields) {
      if (field.key != null) {
        final initialValue = widget.initialData?.formData?[field.key] ?? field.defaultValue;
        _formData[field.key!] = initialValue;
      }
    }

    _currentData = widget.initialData ?? FormDataModel(
      templateId: widget.template.id,
      templateCode: widget.template.templateCode,
      formData: _formData,
    );
  }

  void _initWidgetRegistry() {
    _registry = JsonWidgetRegistry(
      values: {
        ..._formData.map((key, value) => MapEntry(key, value)),
        'formReadOnly': widget.readOnly,
      },
    );

    _registerCustomFunctions();
  }

  void _registerCustomFunctions() {
    _registry.registerFunctions({
      'updateFormField': (args, registry) {
        if (args?.isNotEmpty == true) {
          final key = args![0] as String;
          final value = args.length > 1 ? args[1] : null;
          _onFieldChanged(key, value);
        }
        return null;
      },
      'submitForm': (args, registry) {
        _submitForm();
        return null;
      },
      'getFieldValue': (args, registry) {
        if (args?.isNotEmpty == true) {
          return _formData[args![0] as String];
        }
        return null;
      },
      'isFieldQualified': (args, registry) {
        if (args?.isNotEmpty == true) {
          return _fieldJudgeStatus[args![0] as String] == 'qualified';
        }
        return false;
      },
      'isFieldUnqualified': (args, registry) {
        if (args?.isNotEmpty == true) {
          return _fieldJudgeStatus[args![0] as String] == 'unqualified';
        }
        return false;
      },
      ...?widget.customRegistryFunctions,
    });
  }

  void _onFieldChanged(String key, dynamic value) {
    setState(() {
      _formData[key] = value;
      _currentData.formData = Map.from(_formData);
      _registry.setValue(key, value);
      _autoJudge(key, value);
    });

    widget.onChanged?.call(_currentData);
  }

  void _autoJudge(String key, dynamic value) {
    final field = _fields.firstWhereOrNull((f) => f.key == key);
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
    _fieldJudgeStatus[key] = status;
    field.judgeStatus = status;
  }

  bool _validateForm() {
    bool isValid = true;
    for (var field in _fields) {
      if (field.key != null && !field.isHidden) {
        final value = _formData[field.key!];
        final error = FormFieldBuilder.validateField(field, value);
        if (error != null) {
          isValid = false;
        }
      }
    }
    return isValid;
  }

  void _submitForm() {
    if (!_validateForm()) {
      Get.snackbar('验证失败', '请检查表单填写是否正确');
      return;
    }

    _currentData.formData = Map.from(_formData);
    widget.onSubmitted?.call(_currentData);
  }

  Map<String, dynamic> _convertFieldToJsonWidget(FormFieldModel field) {
    final fieldWithStatus = FormFieldModel.fromJson(field.toJson());
    fieldWithStatus.judgeStatus = _fieldJudgeStatus[field.key];

    final isResultField = fieldWithStatus.isResultField;
    final isUnqualified = fieldWithStatus.isUnqualified;
    final isQualified = fieldWithStatus.isQualified;

    Color? borderColor;
    Color? fillColor;
    if (isUnqualified) {
      borderColor = Colors.red;
      fillColor = Colors.red.withOpacity(0.1);
    } else if (isQualified) {
      borderColor = Colors.green;
      fillColor = Colors.green.withOpacity(0.1);
    }

    return {
      'type': 'container',
      'args': {
        'margin': EdgeInsets.only(bottom: 16.h),
        'child': {
          'type': 'column',
          'args': {
            'crossAxisAlignment': 'start',
            'children': [
              {
                'type': 'row',
                'args': {
                  'children': [
                    {
                      'type': 'text',
                      'args': {
                        'text': field.label ?? '',
                        'style': ThemeDecoder.decodeTextStyle({
                          'fontSize': 14,
                          'fontWeight': 'w500',
                          'color': '#FF424242',
                        }),
                      },
                    },
                    if (field.isRequired)
                      {
                        'type': 'text',
                        'args': {
                          'text': ' *',
                          'style': ThemeDecoder.decodeTextStyle({
                            'fontSize': 14,
                            'color': '#FFF44336',
                          }),
                        },
                      },
                  ],
                },
              },
              {
                'type': 'sized_box',
                'args': {'height': 8},
              },
              {
                'type': 'container',
                'args': {
                  'decoration': ThemeDecoder.decodeBoxDecoration({
                    'color': fillColor?.value.toRadixString(16).padLeft(8, '0'),
                    'borderRadius': 8,
                    'border': {
                      'color': (borderColor ?? Colors.grey).value.toRadixString(16).padLeft(8, '0'),
                      'width': 1,
                    },
                  }),
                  'child': _getInputWidgetJson(field),
                },
              },
            ],
          },
        },
      },
    };
  }

  Map<String, dynamic> _getInputWidgetJson(FormFieldModel field) {
    final key = field.key!;
    final type = field.type ?? 'text';

    switch (type) {
      case 'text':
        return {
          'type': 'text_form_field',
          'args': {
            'key': 'form_field_$key',
            'initialValue': '\${$key}',
            'decoration': ThemeDecoder.decodeInputDecoration({
              'hintText': field.placeholder ?? '请输入',
              'border': 'none',
              'contentPadding': {'left': 12, 'top': 12, 'right': 12, 'bottom': 12},
            }),
            'onChanged': 'updateFormField("$key", #text_editing_value)',
            'readOnly': widget.readOnly || field.isReadOnly,
          },
        };
      case 'number':
        return {
          'type': 'text_form_field',
          'args': {
            'key': 'form_field_$key',
            'initialValue': '\${$key}',
            'keyboardType': 'numberWithOptions(decimal: true)',
            'decoration': ThemeDecoder.decodeInputDecoration({
              'hintText': field.placeholder ?? '请输入数值',
              'suffixText': field.unit,
              'border': 'none',
              'contentPadding': {'left': 12, 'top': 12, 'right': 12, 'bottom': 12},
            }),
            'onChanged': 'updateFormField("$key", #text_editing_value)',
            'readOnly': widget.readOnly || field.isReadOnly,
          },
        };
      default:
        return {
          'type': 'text_form_field',
          'args': {
            'key': 'form_field_$key',
            'initialValue': '\${$key}',
            'decoration': ThemeDecoder.decodeInputDecoration({
              'hintText': field.placeholder ?? '请输入',
              'border': 'none',
              'contentPadding': {'left': 12, 'top': 12, 'right': 12, 'bottom': 12},
            }),
            'onChanged': 'updateFormField("$key", #text_editing_value)',
            'readOnly': widget.readOnly || field.isReadOnly,
          },
        };
    }
  }

  Map<String, dynamic> _buildFormJson() {
    final List<Map<String, dynamic>> children = [];

    for (var field in _fields) {
      if (!field.isHidden) {
        children.add(_convertFieldToJsonWidget(field));
      }
    }

    if (widget.showSubmitButton && !widget.readOnly) {
      children.add({
        'type': 'sized_box',
        'args': {'height': 24},
      });
      children.add({
        'type': 'sized_box',
        'args': {
          'height': 48,
          'child': {
            'type': 'elevated_button',
            'args': {
              'onPressed': 'submitForm()',
              'child': {
                'type': 'text',
                'args': {
                  'text': widget.submitButtonText ?? '提交',
                  'style': ThemeDecoder.decodeTextStyle({
                    'fontSize': 16,
                    'fontWeight': 'w500',
                  }),
                },
              },
            },
          },
        },
      });
    }

    return {
      'type': 'form',
      'args': {
        'key': 'dynamic_form',
        'child': {
          'type': 'column',
          'args': {
            'crossAxisAlignment': 'stretch',
            'children': children,
          },
        },
      },
    };
  }

  @override
  Widget build(BuildContext context) {
    return _buildFallbackForm();
  }

  Widget _buildFallbackForm() {
    return Form(
      key: _formKey,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          ..._fields.map((field) {
            if (field.isHidden) return const SizedBox.shrink();

            final fieldWithStatus = FormFieldModel.fromJson(field.toJson());
            fieldWithStatus.judgeStatus = _fieldJudgeStatus[field.key];

            return FormFieldBuilder.buildField(
              field: fieldWithStatus,
              formData: _formData,
              onChanged: _onFieldChanged,
              readOnly: widget.readOnly,
            );
          }),
          if (widget.showSubmitButton && !widget.readOnly)
            SizedBox(height: 24.h),
          if (widget.showSubmitButton && !widget.readOnly)
            widget.submitButtonWidget ?? _buildDefaultSubmitButton(),
        ],
      ),
    );
  }

  Widget _buildDefaultSubmitButton() {
    return SizedBox(
      height: 48.h,
      child: ElevatedButton(
        onPressed: _submitForm,
        style: ElevatedButton.styleFrom(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8.r),
          ),
        ),
        child: Text(
          widget.submitButtonText ?? '提交',
          style: TextStyle(
            fontSize: 16.sp,
            fontWeight: FontWeight.w500,
          ),
        ),
      ),
    );
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
