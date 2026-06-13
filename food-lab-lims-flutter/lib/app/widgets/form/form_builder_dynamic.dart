import 'package:flutter/material.dart';
import 'package:flutter_form_builder/flutter_form_builder.dart';
import 'package:form_builder_validators/form_builder_validators.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:get/get.dart';

import '../../models/form_field_model.dart';
import '../../models/form_template_model.dart';
import '../../models/form_data_model.dart';
import 'form_field_builder.dart';

class FormBuilderDynamic extends StatefulWidget {
  final FormTemplateModel template;
  final FormDataModel? initialData;
  final Function(FormDataModel data)? onChanged;
  final Function(FormDataModel data)? onSubmitted;
  final bool readOnly;
  final bool showSubmitButton;
  final String? submitButtonText;
  final Widget? submitButtonWidget;

  const FormBuilderDynamic({
    super.key,
    required this.template,
    this.initialData,
    this.onChanged,
    this.onSubmitted,
    this.readOnly = false,
    this.showSubmitButton = true,
    this.submitButtonText,
    this.submitButtonWidget,
  });

  @override
  State<FormBuilderDynamic> createState() => _FormBuilderDynamicState();
}

class _FormBuilderDynamicState extends State<FormBuilderDynamic> {
  final _formKey = GlobalKey<FormBuilderState>();
  late Map<String, dynamic> _formData;
  late Map<String, String> _fieldJudgeStatus;
  late FormDataModel _currentData;
  late List<FormFieldModel> _fields;

  @override
  void initState() {
    super.initState();
    _fields = widget.template.parseFields();
    _initFormData();
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

  void _onFieldChanged(String key, dynamic value) {
    setState(() {
      _formData[key] = value;
      _currentData.formData = Map.from(_formData);
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

  @override
  Widget build(BuildContext context) {
    return FormBuilder(
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
