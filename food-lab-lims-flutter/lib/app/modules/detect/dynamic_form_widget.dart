import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';

class DynamicFormWidget extends StatelessWidget {
  final List<Map<String, dynamic>> schema;
  final Map<String, dynamic> formData;
  final String judgeStatus;
  final Function(String key, dynamic value) onChanged;

  const DynamicFormWidget({
    super.key,
    required this.schema,
    required this.formData,
    required this.judgeStatus,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    return ListView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      itemCount: schema.length,
      itemBuilder: (context, index) {
        final field = schema[index];
        return _buildField(field);
      },
    );
  }

  Widget _buildField(Map<String, dynamic> field) {
    final type = field['type'] as String;
    final key = field['key'] as String;
    final label = field['label'] as String;
    final required = field['required'] as bool? ?? false;
    final resultType = field['resultType'] as String?;

    final isResultField = resultType != null;
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

    return Container(
      margin: EdgeInsets.only(bottom: 16.h),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Text(
                label,
                style: TextStyle(
                  fontSize: 14.sp,
                  fontWeight: FontWeight.w500,
                  color: Colors.grey[800],
                ),
              ),
              if (required)
                Text(
                  ' *',
                  style: TextStyle(
                    fontSize: 14.sp,
                    color: Colors.red,
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
            child: _buildInputWidget(field, key, borderColor),
          ),
          if (field['description'] != null)
            Padding(
              padding: EdgeInsets.only(top: 4.h),
              child: Text(
                field['description'] as String,
                style: TextStyle(
                  fontSize: 12.sp,
                  color: Colors.grey[500],
                ),
              ),
            ),
          if (isResultField && field['unit'] != null)
            Padding(
              padding: EdgeInsets.only(top: 4.h),
              child: Text(
                '单位: ${field['unit']}',
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

  Widget _buildInputWidget(
    Map<String, dynamic> field,
    String key,
    Color? borderColor,
  ) {
    final type = field['type'] as String;

    switch (type) {
      case 'number':
        return _buildNumberInput(field, key);
      case 'select':
        return _buildSelectInput(field, key);
      case 'text':
        return _buildTextInput(field, key);
      case 'textarea':
        return _buildTextareaInput(field, key);
      case 'date':
        return _buildDateInput(field, key);
      default:
        return _buildTextInput(field, key);
    }
  }

  Widget _buildNumberInput(Map<String, dynamic> field, String key) {
    final min = field['min'] as num?;
    final max = field['max'] as num?;
    final precision = field['precision'] as int?;
    final unit = field['unit'] as String?;

    return TextFormField(
      initialValue: formData[key]?.toString(),
      keyboardType: TextInputType.numberWithOptions(
        decimal: precision != null && precision > 0,
        signed: false,
      ),
      decoration: InputDecoration(
        hintText: field['placeholder'] as String? ?? '请输入数值',
        suffixText: unit,
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
        onChanged(key, numValue);
      },
      validator: (value) {
        if (field['required'] == true && (value == null || value.isEmpty)) {
          return '请输入${field['label']}';
        }
        if (value != null && value.isNotEmpty) {
          final numValue = double.tryParse(value);
          if (numValue == null) {
            return '请输入有效的数值';
          }
          if (min != null && numValue < min) {
            return '数值不能小于$min';
          }
          if (max != null && numValue > max) {
            return '数值不能大于$max';
          }
        }
        return null;
      },
    );
  }

  Widget _buildSelectInput(Map<String, dynamic> field, String key) {
    final options = List<Map<String, dynamic>>.from(field['options'] ?? []);
    final currentValue = formData[key];

    return DropdownButtonFormField<dynamic>(
      value: currentValue,
      decoration: InputDecoration(
        hintText: field['placeholder'] as String? ?? '请选择',
        border: InputBorder.none,
        contentPadding: EdgeInsets.symmetric(
          horizontal: 12.w,
          vertical: 12.h,
        ),
      ),
      items: options.map((option) {
        return DropdownMenuItem<dynamic>(
          value: option['value'],
          child: Text(
            option['label'] as String,
            style: TextStyle(
              fontSize: 14.sp,
              color: Colors.grey[800],
            ),
          ),
        );
      }).toList(),
      onChanged: (value) {
        onChanged(key, value);
      },
      validator: (value) {
        if (field['required'] == true && value == null) {
          return '请选择${field['label']}';
        }
        return null;
      },
    );
  }

  Widget _buildTextInput(Map<String, dynamic> field, String key) {
    return TextFormField(
      initialValue: formData[key]?.toString(),
      keyboardType: TextInputType.text,
      decoration: InputDecoration(
        hintText: field['placeholder'] as String? ?? '请输入',
        border: InputBorder.none,
        contentPadding: EdgeInsets.symmetric(
          horizontal: 12.w,
          vertical: 12.h,
        ),
      ),
      onChanged: (value) {
        onChanged(key, value);
      },
      validator: (value) {
        if (field['required'] == true && (value == null || value.isEmpty)) {
          return '请输入${field['label']}';
        }
        return null;
      },
    );
  }

  Widget _buildTextareaInput(Map<String, dynamic> field, String key) {
    return TextFormField(
      initialValue: formData[key]?.toString(),
      keyboardType: TextInputType.multiline,
      maxLines: field['maxLines'] as int? ?? 4,
      decoration: InputDecoration(
        hintText: field['placeholder'] as String? ?? '请输入',
        border: InputBorder.none,
        contentPadding: EdgeInsets.symmetric(
          horizontal: 12.w,
          vertical: 12.h,
        ),
      ),
      onChanged: (value) {
        onChanged(key, value);
      },
      validator: (value) {
        if (field['required'] == true && (value == null || value.isEmpty)) {
          return '请输入${field['label']}';
        }
        return null;
      },
    );
  }

  Widget _buildDateInput(Map<String, dynamic> field, String key) {
    return TextFormField(
      initialValue: formData[key]?.toString(),
      keyboardType: TextInputType.datetime,
      readOnly: true,
      decoration: InputDecoration(
        hintText: field['placeholder'] as String? ?? '请选择日期',
        suffixIcon: Icon(
          Icons.calendar_today,
          size: 20.sp,
          color: Colors.grey[500],
        ),
        border: InputBorder.none,
        contentPadding: EdgeInsets.symmetric(
          horizontal: 12.w,
          vertical: 12.h,
        ),
      ),
      onTap: () async {
        final date = await showDatePicker(
          context: Get.context!,
          initialDate: DateTime.now(),
          firstDate: DateTime(2000),
          lastDate: DateTime(2100),
        );
        if (date != null) {
          final dateStr = date.toIso8601String().split('T').first;
          onChanged(key, dateStr);
        }
      },
      validator: (value) {
        if (field['required'] == true && (value == null || value.isEmpty)) {
          return '请选择${field['label']}';
        }
        return null;
      },
    );
  }
}
