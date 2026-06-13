import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter_form_builder/flutter_form_builder.dart';
import 'package:form_builder_validators/form_builder_validators.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:get/get.dart';
import 'package:file_picker/file_picker.dart';
import 'package:intl/intl.dart';

import '../../models/form_field_model.dart';
import '../../services/form_service.dart';

class FormFieldBuilder {
  static Widget buildField({
    required FormFieldModel field,
    required Map<String, dynamic> formData,
    required Function(String key, dynamic value) onChanged,
    Map<String, dynamic>? errors,
    bool readOnly = false,
  }) {
    if (field.isHidden) {
      return const SizedBox.shrink();
    }

    final isResultField = field.isResultField;
    final judgeStatus = field.judgeStatus;
    final isUnqualified = isResultField && judgeStatus == 'unqualified';
    final isQualified = isResultField && judgeStatus == 'qualified';

    Color? borderColor;
    Color? fillColor;
    if (isUnqualified) {
      borderColor = Colors.red;
      fillColor = Colors.red.withOpacity(0.1);
    } else if (isQualified) {
      borderColor = Colors.green;
      fillColor = Colors.green.withOpacity(0.1);
    }

    final errorText = errors?[field.key] as String?;
    final hasError = errorText != null && errorText.isNotEmpty;
    if (hasError) {
      borderColor = Colors.red;
      fillColor = Colors.red.withOpacity(0.05);
    }

    return Container(
      margin: EdgeInsets.only(bottom: 16.h),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Text(
                field.label ?? '',
                style: TextStyle(
                  fontSize: 14.sp,
                  fontWeight: FontWeight.w500,
                  color: Colors.grey[800],
                ),
              ),
              if (field.isRequired)
                Text(
                  ' *',
                  style: TextStyle(
                    fontSize: 14.sp,
                    color: Colors.red,
                  ),
                ),
              if (isResultField)
                Container(
                  margin: EdgeInsets.only(left: 8.w),
                  padding: EdgeInsets.symmetric(horizontal: 8.w, vertical: 2.h),
                  decoration: BoxDecoration(
                    color: isQualified
                        ? Colors.green.withOpacity(0.2)
                        : isUnqualified
                            ? Colors.red.withOpacity(0.2)
                            : Colors.grey.withOpacity(0.2),
                    borderRadius: BorderRadius.circular(4.r),
                  ),
                  child: Text(
                    isQualified
                        ? '合格'
                        : isUnqualified
                            ? '不合格'
                            : '结果项',
                    style: TextStyle(
                      fontSize: 10.sp,
                      color: isQualified
                          ? Colors.green
                          : isUnqualified
                              ? Colors.red
                              : Colors.grey[600],
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
            ],
          ),
          SizedBox(height: 8.h),
          Container(
            decoration: BoxDecoration(
              color: fillColor,
              borderRadius: BorderRadius.circular(8.r),
              border: Border.all(
                color: borderColor ?? Colors.grey[300]!,
                width: 1.w,
              ),
            ),
            child: _buildInputWidget(
              field,
              formData,
              onChanged,
              readOnly || field.isReadOnly,
            ),
          ),
          if (errorText != null && errorText.isNotEmpty)
            Padding(
              padding: EdgeInsets.only(top: 4.h),
              child: Text(
                errorText,
                style: TextStyle(
                  fontSize: 12.sp,
                  color: Colors.red,
                ),
              ),
            ),
          if (field.description != null && errorText == null)
            Padding(
              padding: EdgeInsets.only(top: 4.h),
              child: Text(
                field.description!,
                style: TextStyle(
                  fontSize: 12.sp,
                  color: Colors.grey[500],
                ),
              ),
            ),
          if (isResultField && field.unit != null)
            Padding(
              padding: EdgeInsets.only(top: 4.h),
              child: Text(
                '单位: ${field.unit}',
                style: TextStyle(
                  fontSize: 12.sp,
                  color: Colors.grey[500],
                ),
              ),
            ),
        ],
      ),
    );
  }

  static Widget _buildInputWidget(
    FormFieldModel field,
    Map<String, dynamic> formData,
    Function(String key, dynamic value) onChanged,
    bool readOnly,
  ) {
    final type = field.type ?? 'text';
    final key = field.key!;
    final initialValue = formData[key] ?? field.defaultValue;

    switch (type) {
      case 'text':
        return _buildTextField(field, initialValue, onChanged, readOnly);
      case 'number':
        return _buildNumberField(field, initialValue, onChanged, readOnly);
      case 'date':
        return _buildDateField(field, initialValue, onChanged, readOnly);
      case 'datetime':
        return _buildDateTimeField(field, initialValue, onChanged, readOnly);
      case 'select':
        return _buildSelectField(field, initialValue, onChanged, readOnly);
      case 'textarea':
        return _buildTextareaField(field, initialValue, onChanged, readOnly);
      case 'file':
        return _buildFileField(field, initialValue, onChanged, readOnly);
      case 'checkbox':
        return _buildCheckboxField(field, initialValue, onChanged, readOnly);
      case 'radio':
        return _buildRadioField(field, initialValue, onChanged, readOnly);
      case 'switch':
        return _buildSwitchField(field, initialValue, onChanged, readOnly);
      default:
        return _buildTextField(field, initialValue, onChanged, readOnly);
    }
  }

  static Widget _buildTextField(
    FormFieldModel field,
    dynamic initialValue,
    Function(String key, dynamic value) onChanged,
    bool readOnly,
  ) {
    return TextFormField(
      initialValue: initialValue?.toString(),
      readOnly: readOnly,
      maxLength: field.maxLength,
      keyboardType: field.widgetConfig?['keyboardType'] as TextInputType? ?? TextInputType.text,
      decoration: InputDecoration(
        hintText: field.placeholder ?? '请输入',
        border: InputBorder.none,
        counterText: '',
        contentPadding: EdgeInsets.symmetric(
          horizontal: 12.w,
          vertical: 12.h,
        ),
      ),
      onChanged: (value) => onChanged(field.key!, value),
      validator: (value) => validateField(field, value),
    );
  }

  static Widget _buildNumberField(
    FormFieldModel field,
    dynamic initialValue,
    Function(String key, dynamic value) onChanged,
    bool readOnly,
  ) {
    final precision = field.precision ?? 0;
    return TextFormField(
      initialValue: initialValue?.toString(),
      readOnly: readOnly,
      keyboardType: TextInputType.numberWithOptions(
        decimal: precision > 0,
        signed: false,
      ),
      decoration: InputDecoration(
        hintText: field.placeholder ?? '请输入数值',
        suffixText: field.unit,
        suffixStyle: TextStyle(
          fontSize: 14.sp,
          color: Colors.grey[500],
        ),
        border: InputBorder.none,
        contentPadding: EdgeInsets.symmetric(
          horizontal: 12.w,
          vertical: 12.h,
        ),
      ),
      onChanged: (value) {
        final numValue = double.tryParse(value);
        onChanged(field.key!, numValue);
      },
      validator: (value) => validateField(field, value),
    );
  }

  static Widget _buildDateField(
    FormFieldModel field,
    dynamic initialValue,
    Function(String key, dynamic value) onChanged,
    bool readOnly,
  ) {
    return InkWell(
      onTap: readOnly
          ? null
          : () async {
              final initialDate = initialValue != null
                  ? DateTime.tryParse(initialValue.toString()) ?? DateTime.now()
                  : DateTime.now();
              final date = await showDatePicker(
                context: Get.context!,
                initialDate: initialDate,
                firstDate: DateTime(2000),
                lastDate: DateTime(2100),
              );
              if (date != null) {
                final dateStr = DateFormat('yyyy-MM-dd').format(date);
                onChanged(field.key!, dateStr);
              }
            },
      child: Container(
        padding: EdgeInsets.symmetric(
          horizontal: 12.w,
          vertical: 12.h,
        ),
        child: Row(
          children: [
            Expanded(
              child: Text(
                initialValue?.toString() ?? field.placeholder ?? '请选择日期',
                style: TextStyle(
                  fontSize: 14.sp,
                  color: initialValue != null ? Colors.grey[800] : Colors.grey[400],
                ),
              ),
            ),
            Icon(
              Icons.calendar_today,
              size: 20.sp,
              color: Colors.grey[500],
            ),
          ],
        ),
      ),
    );
  }

  static Widget _buildDateTimeField(
    FormFieldModel field,
    dynamic initialValue,
    Function(String key, dynamic value) onChanged,
    bool readOnly,
  ) {
    return InkWell(
      onTap: readOnly
          ? null
          : () async {
              final initialDate = initialValue != null
                  ? DateTime.tryParse(initialValue.toString()) ?? DateTime.now()
                  : DateTime.now();
              final date = await showDatePicker(
                context: Get.context!,
                initialDate: initialDate,
                firstDate: DateTime(2000),
                lastDate: DateTime(2100),
              );
              if (date != null) {
                final time = await showTimePicker(
                  context: Get.context!,
                  initialTime: TimeOfDay.fromDateTime(initialDate),
                );
                if (time != null) {
                  final dateTime = DateTime(
                    date.year,
                    date.month,
                    date.day,
                    time.hour,
                    time.minute,
                  );
                  final dateStr = DateFormat('yyyy-MM-dd HH:mm:ss').format(dateTime);
                  onChanged(field.key!, dateStr);
                }
              }
            },
      child: Container(
        padding: EdgeInsets.symmetric(
          horizontal: 12.w,
          vertical: 12.h,
        ),
        child: Row(
          children: [
            Expanded(
              child: Text(
                initialValue?.toString() ?? field.placeholder ?? '请选择日期时间',
                style: TextStyle(
                  fontSize: 14.sp,
                  color: initialValue != null ? Colors.grey[800] : Colors.grey[400],
                ),
              ),
            ),
            Icon(
              Icons.access_time,
              size: 20.sp,
              color: Colors.grey[500],
            ),
          ],
        ),
      ),
    );
  }

  static Widget _buildSelectField(
    FormFieldModel field,
    dynamic initialValue,
    Function(String key, dynamic value) onChanged,
    bool readOnly,
  ) {
    final options = field.options ?? [];
    return DropdownButtonFormField<dynamic>(
      value: initialValue,
      disabledHint: initialValue != null
          ? Text(
              options.firstWhere((o) => o.value == initialValue, orElse: () => FormFieldOptionModel(label: initialValue.toString())).label ?? '',
            )
          : null,
      decoration: InputDecoration(
        hintText: field.placeholder ?? '请选择',
        border: InputBorder.none,
        contentPadding: EdgeInsets.symmetric(
          horizontal: 12.w,
          vertical: 12.h,
        ),
      ),
      items: options.where((o) => o.disabled != true).map((option) {
        return DropdownMenuItem<dynamic>(
          value: option.value,
          child: Text(
            option.label ?? '',
            style: TextStyle(
              fontSize: 14.sp,
              color: Colors.grey[800],
            ),
          ),
        );
      }).toList(),
      onChanged: readOnly
          ? null
          : (value) => onChanged(field.key!, value),
      validator: (value) => validateField(field, value),
    );
  }

  static Widget _buildTextareaField(
    FormFieldModel field,
    dynamic initialValue,
    Function(String key, dynamic value) onChanged,
    bool readOnly,
  ) {
    return TextFormField(
      initialValue: initialValue?.toString(),
      readOnly: readOnly,
      maxLines: field.maxLines ?? 4,
      maxLength: field.maxLength,
      keyboardType: TextInputType.multiline,
      decoration: InputDecoration(
        hintText: field.placeholder ?? '请输入',
        border: InputBorder.none,
        counterText: '',
        contentPadding: EdgeInsets.symmetric(
          horizontal: 12.w,
          vertical: 12.h,
        ),
      ),
      onChanged: (value) => onChanged(field.key!, value),
      validator: (value) => validateField(field, value),
    );
  }

  static Widget _buildFileField(
    FormFieldModel field,
    dynamic initialValue,
    Function(String key, dynamic value) onChanged,
    bool readOnly,
  ) {
    final List<String> files = initialValue is List
        ? List<String>.from(initialValue)
        : initialValue != null
            ? [initialValue.toString()]
            : [];
    final maxCount = field.maxFileCount ?? 1;

    return Container(
      padding: EdgeInsets.symmetric(
        horizontal: 12.w,
        vertical: 8.h,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Wrap(
            spacing: 8.w,
            runSpacing: 8.h,
            children: [
              ...files.asMap().entries.map((entry) {
                final index = entry.key;
                final file = entry.value;
                return _buildFileItem(
                  file,
                  readOnly,
                  () {
                    final newFiles = List<String>.from(files);
                    newFiles.removeAt(index);
                    onChanged(field.key!, newFiles);
                  },
                );
              }),
              if (files.length < maxCount && !readOnly)
                _buildAddFileButton(field, files, onChanged),
            ],
          ),
          if (field.allowedFileTypes != null && field.allowedFileTypes!.isNotEmpty)
            Padding(
              padding: EdgeInsets.only(top: 8.h),
              child: Text(
                '支持格式: ${field.allowedFileTypes!.join(', ')}',
                style: TextStyle(
                  fontSize: 11.sp,
                  color: Colors.grey[500],
                ),
              ),
            ),
        ],
      ),
    );
  }

  static Widget _buildFileItem(String file, bool readOnly, VoidCallback onDelete) {
    final isUrl = file.startsWith('http');
    final fileName = isUrl ? file.split('/').last : file.split('\\').last;

    return Container(
      padding: EdgeInsets.symmetric(horizontal: 8.w, vertical: 6.h),
      decoration: BoxDecoration(
        color: Colors.grey[100],
        borderRadius: BorderRadius.circular(4.r),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            Icons.attach_file,
            size: 16.sp,
            color: Colors.grey[600],
          ),
          SizedBox(width: 4.w),
          Text(
            fileName,
            style: TextStyle(
              fontSize: 12.sp,
              color: Colors.grey[700],
            ),
          ),
          if (!readOnly)
            SizedBox(width: 4.w),
          if (!readOnly)
            InkWell(
              onTap: onDelete,
              child: Icon(
                Icons.close,
                size: 16.sp,
                color: Colors.grey[500],
              ),
            ),
        ],
      ),
    );
  }

  static Widget _buildAddFileButton(
    FormFieldModel field,
    List<String> currentFiles,
    Function(String key, dynamic value) onChanged,
  ) {
    final formService = Get.find<FormService>();

    return InkWell(
      onTap: () async {
        try {
          final allowedExtensions = field.allowedFileTypes
              ?.map((e) => e.replaceAll('.', ''))
              .toList();

          final result = await FilePicker.platform.pickFiles(
            type: allowedExtensions != null && allowedExtensions.isNotEmpty
                ? FileType.custom
                : FileType.any,
            allowedExtensions: allowedExtensions,
            allowMultiple: (field.maxFileCount ?? 1) > 1,
          );

          if (result != null && result.files.isNotEmpty) {
            for (var file in result.files) {
              if (file.path != null) {
                final uploadedUrl = await formService.uploadFile(
                  File(file.path!),
                  fieldKey: field.key,
                );
                if (uploadedUrl != null) {
                  final newFiles = List<String>.from(currentFiles);
                  newFiles.add(uploadedUrl);
                  onChanged(field.key!, newFiles);
                }
              }
            }
          }
        } catch (e) {
          Get.snackbar('错误', '选择文件失败');
        }
      },
      child: Container(
        padding: EdgeInsets.symmetric(horizontal: 16.w, vertical: 12.h),
        decoration: BoxDecoration(
          border: Border.all(
            color: Colors.grey[300]!,
            style: BorderStyle.solid,
          ),
          borderRadius: BorderRadius.circular(4.r),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              Icons.add,
              size: 18.sp,
              color: Colors.grey[500],
            ),
            SizedBox(width: 4.w),
            Text(
              '添加文件',
              style: TextStyle(
                fontSize: 13.sp,
                color: Colors.grey[500],
              ),
            ),
          ],
        ),
      ),
    );
  }

  static Widget _buildCheckboxField(
    FormFieldModel field,
    dynamic initialValue,
    Function(String key, dynamic value) onChanged,
    bool readOnly,
  ) {
    final options = field.options ?? [];
    final List<dynamic> selectedValues = initialValue is List
        ? List<dynamic>.from(initialValue)
        : initialValue != null
            ? [initialValue]
            : [];

    return Container(
      padding: EdgeInsets.symmetric(
        horizontal: 12.w,
        vertical: 8.h,
      ),
      child: Wrap(
        spacing: 16.w,
        runSpacing: 8.h,
        children: options.map((option) {
          final isSelected = selectedValues.contains(option.value);
          return InkWell(
            onTap: readOnly || option.disabled == true
                ? null
                : () {
                    final newValues = List<dynamic>.from(selectedValues);
                    if (isSelected) {
                      newValues.remove(option.value);
                    } else {
                      newValues.add(option.value);
                    }
                    onChanged(field.key!, newValues);
                  },
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(
                  isSelected ? Icons.check_box : Icons.check_box_outline_blank,
                  color: isSelected
                      ? Theme.of(Get.context!).primaryColor
                      : Colors.grey[400],
                  size: 20.sp,
                ),
                SizedBox(width: 6.w),
                Text(
                  option.label ?? '',
                  style: TextStyle(
                    fontSize: 14.sp,
                    color: option.disabled == true ? Colors.grey[400] : Colors.grey[700],
                  ),
                ),
              ],
            ),
          );
        }).toList(),
      ),
    );
  }

  static Widget _buildRadioField(
    FormFieldModel field,
    dynamic initialValue,
    Function(String key, dynamic value) onChanged,
    bool readOnly,
  ) {
    final options = field.options ?? [];

    return Container(
      padding: EdgeInsets.symmetric(
        horizontal: 12.w,
        vertical: 8.h,
      ),
      child: Wrap(
        spacing: 16.w,
        runSpacing: 8.h,
        children: options.map((option) {
          final isSelected = initialValue == option.value;
          return InkWell(
            onTap: readOnly || option.disabled == true
                ? null
                : () => onChanged(field.key!, option.value),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(
                  isSelected ? Icons.radio_button_checked : Icons.radio_button_off,
                  color: isSelected
                      ? Theme.of(Get.context!).primaryColor
                      : Colors.grey[400],
                  size: 20.sp,
                ),
                SizedBox(width: 6.w),
                Text(
                  option.label ?? '',
                  style: TextStyle(
                    fontSize: 14.sp,
                    color: option.disabled == true ? Colors.grey[400] : Colors.grey[700],
                  ),
                ),
              ],
            ),
          );
        }).toList(),
      ),
    );
  }

  static Widget _buildSwitchField(
    FormFieldModel field,
    dynamic initialValue,
    Function(String key, dynamic value) onChanged,
    bool readOnly,
  ) {
    final value = initialValue == true;
    return Container(
      padding: EdgeInsets.symmetric(
        horizontal: 12.w,
        vertical: 8.h,
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            value ? '是' : '否',
            style: TextStyle(
              fontSize: 14.sp,
              color: Colors.grey[700],
            ),
          ),
          Switch(
            value: value,
            onChanged: readOnly
                ? null
                : (newValue) => onChanged(field.key!, newValue),
          ),
        ],
      ),
    );
  }

  static String? validateField(FormFieldModel field, dynamic value) {
    if (field.isRequired) {
      if (value == null || (value is String && value.isEmpty)) {
        return field.validationMessage ?? '请输入${field.label}';
      }
    }

    if (field.validationPattern != null && value is String && value.isNotEmpty) {
      final regExp = RegExp(field.validationPattern!);
      if (!regExp.hasMatch(value)) {
        return field.validationMessage ?? '${field.label}格式不正确';
      }
    }

    if (field.type == 'number' && value != null && value.toString().isNotEmpty) {
      final numValue = num.tryParse(value.toString());
      if (numValue == null) {
        return '请输入有效的数值';
      }
      if (field.min != null && numValue < field.min!) {
        return '数值不能小于${field.min}';
      }
      if (field.max != null && numValue > field.max!) {
        return '数值不能大于${field.max}';
      }
    }

    return null;
  }
}
