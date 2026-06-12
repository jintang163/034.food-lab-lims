import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:fluttertoast/fluttertoast.dart';

import '../../theme/app_theme.dart';
import 'sample_controller.dart';

class SampleRegisterView extends GetView<SampleController> {
  const SampleRegisterView({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppTheme.bgColor,
      appBar: AppBar(
        title: const Text('样品登记'),
        backgroundColor: AppTheme.primaryColor,
        elevation: 0,
      ),
      body: SingleChildScrollView(
        padding: EdgeInsets.all(16.w),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            _buildSectionTitle('基本信息'),
            SizedBox(height: 12.h),
            _buildTextField(
              label: '样品名称',
              hint: '请输入样品名称',
              icon: Icons.science,
              required: true,
              onChanged: (value) => controller.sampleName.value = value,
            ),
            SizedBox(height: 12.h),
            _buildTextField(
              label: '批号',
              hint: '请输入批号',
              icon: Icons.label,
              onChanged: (value) => controller.batchNo.value = value,
            ),
            SizedBox(height: 12.h),
            _buildTextField(
              label: '生产单位',
              hint: '请输入生产单位',
              icon: Icons.factory,
              onChanged: (value) => controller.manufacturer.value = value,
            ),
            SizedBox(height: 12.h),
            _buildTextField(
              label: '采样地点',
              hint: '请输入采样地点',
              icon: Icons.location_on,
              onChanged: (value) => controller.sampleLocation.value = value,
            ),
            SizedBox(height: 24.h),
            _buildSectionTitle('采样信息'),
            SizedBox(height: 12.h),
            _buildTextField(
              label: '采样人',
              hint: '请输入采样人',
              icon: Icons.person,
              onChanged: (value) => controller.samplePerson.value = value,
            ),
            SizedBox(height: 12.h),
            _buildTextField(
              label: '采样方式',
              hint: '请输入采样方式',
              icon: Icons.inventory,
              onChanged: (value) => controller.sampleMethod.value = value,
            ),
            SizedBox(height: 12.h),
            Row(
              children: [
                Expanded(
                  child: _buildTextField(
                    label: '样品数量',
                    hint: '请输入数量',
                    icon: Icons.numbers,
                    keyboardType: TextInputType.number,
                    onChanged: (value) => controller.sampleAmount.value = value,
                  ),
                ),
                SizedBox(width: 12.w),
                Expanded(
                  child: _buildTextField(
                    label: '单位',
                    hint: '请输入单位',
                    icon: Icons.straighten,
                    onChanged: (value) => controller.sampleUnit.value = value,
                  ),
                ),
              ],
            ),
            SizedBox(height: 12.h),
            Row(
              children: [
                Expanded(
                  child: _buildTextField(
                    label: '生产日期',
                    hint: '请选择日期',
                    icon: Icons.calendar_today,
                    readOnly: true,
                    onTap: () => _selectDate('production'),
                    onChanged: (value) => controller.productionDate.value = value,
                  ),
                ),
                SizedBox(width: 12.w),
                Expanded(
                  child: _buildTextField(
                    label: '保质期',
                    hint: '请输入保质期',
                    icon: Icons.schedule,
                    onChanged: (value) => controller.shelfLife.value = value,
                  ),
                ),
              ],
            ),
            SizedBox(height: 24.h),
            _buildSectionTitle('检测项目'),
            SizedBox(height: 12.h),
            _buildDetectItemList(),
            SizedBox(height: 24.h),
            _buildSectionTitle('备注'),
            SizedBox(height: 12.h),
            _buildTextField(
              label: '备注',
              hint: '请输入备注信息',
              icon: Icons.note,
              maxLines: 3,
              onChanged: (value) => controller.remark.value = value,
            ),
            SizedBox(height: 24.h),
            Row(
              children: [
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: () => controller.saveOfflineSample(),
                    icon: const Icon(Icons.save_alt),
                    label: const Text('离线保存'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.grey[600],
                      foregroundColor: Colors.white,
                      padding: EdgeInsets.symmetric(vertical: 14.h),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(8.r),
                      ),
                    ),
                  ),
                ),
                SizedBox(width: 12.w),
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: () => _handleSubmit(),
                    icon: const Icon(Icons.send),
                    label: const Text('提交'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppTheme.primaryColor,
                      foregroundColor: Colors.white,
                      padding: EdgeInsets.symmetric(vertical: 14.h),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(8.r),
                      ),
                    ),
                  ),
                ),
              ],
            ),
            SizedBox(height: 24.h),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Row(
      children: [
        Container(
          width: 4.w,
          height: 18.h,
          color: AppTheme.primaryColor,
        ),
        SizedBox(width: 8.w),
        Text(
          title,
          style: TextStyle(
            fontSize: 16.sp,
            fontWeight: FontWeight.bold,
            color: AppTheme.textPrimary,
          ),
        ),
      ],
    );
  }

  Widget _buildTextField({
    required String label,
    required String hint,
    required IconData icon,
    bool required = false,
    required ValueChanged<String> onChanged,
    TextInputType? keyboardType,
    int maxLines = 1,
    bool readOnly = false,
    VoidCallback? onTap,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        RichText(
          text: TextSpan(
            text: label,
            style: TextStyle(
              fontSize: 14.sp,
              color: AppTheme.textPrimary,
              fontWeight: FontWeight.w500,
            ),
            children: required
                ? [
                    TextSpan(
                      text: ' *',
                      style: TextStyle(
                        color: Colors.red,
                        fontSize: 14.sp,
                      ),
                    ),
                  ]
                : [],
          ),
        ),
        SizedBox(height: 8.h),
        TextField(
          onChanged: onChanged,
          keyboardType: keyboardType,
          maxLines: maxLines,
          readOnly: readOnly,
          onTap: onTap,
          style: TextStyle(fontSize: 14.sp),
          decoration: InputDecoration(
            hintText: hint,
            hintStyle: TextStyle(color: Colors.grey[400]),
            prefixIcon: Icon(icon, size: 20.sp, color: AppTheme.primaryColor),
            filled: true,
            fillColor: Colors.white,
            contentPadding: EdgeInsets.symmetric(horizontal: 12.w, vertical: 12.h),
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8.r),
              borderSide: BorderSide(color: Colors.grey[300]!),
            ),
            enabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8.r),
              borderSide: BorderSide(color: Colors.grey[300]!),
            ),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8.r),
              borderSide: const BorderSide(color: AppTheme.primaryColor, width: 2),
            ),
          ),
        ),
      ],
    );
  }

  Future<void> _selectDate(String type) async {
    final date = await showDatePicker(
      context: Get.context!,
      initialDate: DateTime.now(),
      firstDate: DateTime(2000),
      lastDate: DateTime(2100),
    );

    if (date != null) {
      final dateStr = date.toIso8601String().split('T').first;
      if (type == 'production') {
        controller.productionDate.value = dateStr;
      }
    }
  }

  Widget _buildDetectItemList() {
    if (controller.detectItemList.isEmpty) {
      return Card(
        child: Padding(
          padding: EdgeInsets.all(16.w),
          child: Center(
            child: Text(
              '暂无检测项目数据',
              style: TextStyle(color: Colors.grey[500], fontSize: 14.sp),
            ),
          ),
        ),
      );
    }

    return Obx(() => ListView.separated(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          itemCount: controller.detectItemList.length,
          separatorBuilder: (context, index) => Divider(
            height: 1.h,
            color: Colors.grey[200],
          ),
          itemBuilder: (context, index) {
            final item = controller.detectItemList[index];
            final isSelected = controller.selectedDetectItemIds.contains(item.id);

            return InkWell(
              onTap: () {
                if (item.id != null) {
                  controller.toggleDetectItem(item.id!);
                }
              },
              child: Container(
                color: isSelected
                    ? AppTheme.primaryColor.withOpacity(0.05)
                    : Colors.white,
                padding: EdgeInsets.symmetric(horizontal: 12.w, vertical: 12.h),
                child: Row(
                  children: [
                    Container(
                      width: 24.w,
                      height: 24.h,
                      decoration: BoxDecoration(
                        color: isSelected ? AppTheme.primaryColor : Colors.white,
                        border: Border.all(
                          color: isSelected
                              ? AppTheme.primaryColor
                              : Colors.grey[400]!,
                          width: 2,
                        ),
                        borderRadius: BorderRadius.circular(4.r),
                      ),
                      child: isSelected
                          ? Icon(Icons.check, size: 16.sp, color: Colors.white)
                          : null,
                    ),
                    SizedBox(width: 12.w),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            item.itemName ?? '',
                            style: TextStyle(
                              fontSize: 14.sp,
                              fontWeight: FontWeight.w500,
                              color: AppTheme.textPrimary,
                            ),
                          ),
                          if (item.categoryName != null)
                            Text(
                              item.categoryName!,
                              style: TextStyle(
                                fontSize: 12.sp,
                                color: Colors.grey[500],
                              ),
                            ),
                        ],
                      ),
                    ),
                    if (item.unit != null)
                      Text(
                        item.unit!,
                        style: TextStyle(
                          fontSize: 12.sp,
                          color: Colors.grey[500],
                        ),
                      ),
                  ],
                ),
              ),
            );
          },
        ));
  }

  Future<void> _handleSubmit() async {
    if (controller.sampleName.value.isEmpty) {
      Fluttertoast.showToast(msg: '请输入样品名称');
      return;
    }
    if (controller.selectedDetectItemIds.isEmpty) {
      Fluttertoast.showToast(msg: '请选择检测项目');
      return;
    }

    Get.dialog(
      AlertDialog(
        title: const Text('确认提交'),
        content: const Text('确认提交该样品信息吗？'),
        actions: [
          TextButton(
            onPressed: () => Get.back(),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () {
              Get.back();
              controller.submitSample();
            },
            style: TextButton.styleFrom(
              foregroundColor: AppTheme.primaryColor,
            ),
            child: const Text('确认'),
          ),
        ],
      ),
    );
  }
}
