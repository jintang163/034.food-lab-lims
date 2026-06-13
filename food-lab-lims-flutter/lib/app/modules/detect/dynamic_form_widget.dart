import 'dart:async';
import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:flutter_form_builder/flutter_form_builder.dart';

import '../../widgets/form/form_field_builder.dart';
import '../../widgets/form/form_builder_dynamic.dart';
import '../../widgets/form/json_dynamic_form.dart';
import '../../models/form_template_model.dart';
import '../../models/form_data_model.dart';
import '../../models/form_field_model.dart';
import '../../services/form_service.dart';

class DynamicFormWidget extends StatefulWidget {
  final List<Map<String, dynamic>> schema;
  final Map<String, dynamic> formData;
  final String judgeStatus;
  final Function(String key, dynamic value) onChanged;
  final Function(Map<String, dynamic> data)? onSubmitted;
  final bool autoSaveDraft;
  final String? draftKey;
  final bool readOnly;
  final String renderEngine;
  final Map<String, dynamic>? fieldJudgeStatus;
  final String? formTemplateCode;

  const DynamicFormWidget({
    super.key,
    required this.schema,
    required this.formData,
    required this.judgeStatus,
    required this.onChanged,
    this.onSubmitted,
    this.autoSaveDraft = true,
    this.draftKey,
    this.readOnly = false,
    this.renderEngine = 'form_builder',
    this.fieldJudgeStatus,
    this.formTemplateCode,
  });

  @override
  State<DynamicFormWidget> createState() => _DynamicFormWidgetState();
}

class _DynamicFormWidgetState extends State<DynamicFormWidget> {
  final _formKey = GlobalKey<FormBuilderState>();
  final Map<String, dynamic> _errors = {};
  Timer? _autoSaveTimer;
  FormTemplateModel? _template;
  FormDataModel? _formDataModel;

  @override
  void initState() {
    super.initState();
    _initTemplate();
    _initFormDataModel();
    if (widget.autoSaveDraft && widget.draftKey != null) {
      _startAutoSave();
    }
  }

  @override
  void dispose() {
    _autoSaveTimer?.cancel();
    super.dispose();
  }

  void _initTemplate() {
    final fields = widget.schema
        .map((e) => FormFieldModel.fromJson(Map<String, dynamic>.from(e)))
        .toList();

    _template = FormTemplateModel(
      templateCode: widget.formTemplateCode ?? 'detect_form',
      templateName: '检测表单',
      fields: fields,
      renderEngine: widget.renderEngine,
      enableAutoSave: widget.autoSaveDraft,
      autoSaveInterval: 30,
      enableDraft: widget.autoSaveDraft,
      enableOffline: true,
    );
  }

  void _initFormDataModel() {
    _formDataModel = FormDataModel(
      templateCode: widget.formTemplateCode ?? 'detect_form',
      formData: Map<String, dynamic>.from(widget.formData),
      fieldJudgeStatus: widget.fieldJudgeStatus != null
          ? Map<String, dynamic>.from(widget.fieldJudgeStatus!)
          : {},
      judgeStatus: widget.judgeStatus,
      formStatus: 'draft',
      syncStatus: 'pending',
      draftKey: widget.draftKey,
    );
  }

  void _startAutoSave() {
    _autoSaveTimer = Timer.periodic(const Duration(seconds: 30), (_) {
      _autoSaveDraft();
    });
  }

  Future<void> _autoSaveDraft() async {
    if (widget.draftKey == null || _formDataModel == null) return;

    try {
      final formService = Get.find<FormService>();
      _formDataModel!.formData = Map<String, dynamic>.from(widget.formData);
      await formService.saveDraft(widget.draftKey!, _formDataModel!);
    } catch (e) {
      // 静默失败，不影响用户
    }
  }

  bool validateForm() {
    _errors.clear();
    bool isValid = true;

    for (var fieldJson in widget.schema) {
      final field = FormFieldModel.fromJson(Map<String, dynamic>.from(fieldJson));
      if (field.key != null && !field.isHidden) {
        final value = widget.formData[field.key!];
        final error = FormFieldBuilder.validateField(field, value);
        if (error != null) {
          _errors[field.key!] = error;
          isValid = false;
        }
      }
    }

    if (mounted) {
      setState(() {});
    }

    return isValid;
  }

  void submitForm() {
    if (!validateForm()) {
      Get.snackbar('验证失败', '请检查表单填写是否正确');
      return;
    }

    widget.onSubmitted?.call(Map<String, dynamic>.from(widget.formData));
  }

  @override
  Widget build(BuildContext context) {
    if (widget.renderEngine == 'json_dynamic' && _template != null) {
      return JsonDynamicForm(
        template: _template!,
        initialData: _formDataModel,
        onChanged: (data) {
          widget.onChanged('formData', data.formData);
          _formDataModel = data;
        },
        onSubmitted: (data) {
          widget.onSubmitted?.call(data.formData ?? {});
        },
        readOnly: widget.readOnly,
        showSubmitButton: false,
      );
    }

    if (widget.renderEngine == 'form_builder' && _template != null) {
      return FormBuilderDynamic(
        template: _template!,
        initialData: _formDataModel,
        onChanged: (data) {
          if (data.formData != null) {
            for (var entry in data.formData!.entries) {
              widget.onChanged(entry.key, entry.value);
            }
          }
          _formDataModel = data;
        },
        onSubmitted: (data) {
          widget.onSubmitted?.call(data.formData ?? {});
        },
        readOnly: widget.readOnly,
        showSubmitButton: false,
      );
    }

    return _buildLegacyForm();
  }

  Widget _buildLegacyForm() {
    return FormBuilder(
      key: _formKey,
      child: ListView.builder(
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        itemCount: widget.schema.length,
        itemBuilder: (context, index) {
          final fieldJson = widget.schema[index];
          final field = FormFieldModel.fromJson(Map<String, dynamic>.from(fieldJson));

          if (field.isHidden) return const SizedBox.shrink();

          final fieldWithStatus = FormFieldModel.fromJson(field.toJson());
          if (widget.fieldJudgeStatus != null) {
            fieldWithStatus.judgeStatus =
                widget.fieldJudgeStatus![field.key] as String?;
          } else if (field.isResultField) {
            fieldWithStatus.judgeStatus = widget.judgeStatus;
          }

          return FormFieldBuilder.buildField(
            field: fieldWithStatus,
            formData: widget.formData,
            onChanged: widget.onChanged,
            errors: _errors,
            readOnly: widget.readOnly,
          );
        },
      ),
    );
  }

  Widget buildSubmitButton({String? text, VoidCallback? onPressed}) {
    return SizedBox(
      height: 48.h,
      width: double.infinity,
      child: ElevatedButton(
        onPressed: onPressed ?? submitForm,
        style: ElevatedButton.styleFrom(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8.r),
          ),
        ),
        child: Text(
          text ?? '提交',
          style: TextStyle(
            fontSize: 16.sp,
            fontWeight: FontWeight.w500,
          ),
        ),
      ),
    );
  }

  Widget buildJudgeResultBanner() {
    if (widget.judgeStatus.isEmpty) return const SizedBox.shrink();

    final isQualified = widget.judgeStatus == 'qualified';
    final bgColor = isQualified ? Colors.green : Colors.red;
    final icon = isQualified ? Icons.check_circle : Icons.error;
    final text = isQualified ? '合格' : '不合格';

    return Container(
      width: double.infinity,
      padding: EdgeInsets.symmetric(horizontal: 16.w, vertical: 12.h),
      margin: EdgeInsets.only(bottom: 16.h),
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: BorderRadius.circular(8.r),
      ),
      child: Row(
        children: [
          Icon(
            icon,
            color: Colors.white,
            size: 20.sp,
          ),
          SizedBox(width: 8.w),
          Text(
            '判定结果：$text',
            style: TextStyle(
              color: Colors.white,
              fontSize: 14.sp,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
  }
}
