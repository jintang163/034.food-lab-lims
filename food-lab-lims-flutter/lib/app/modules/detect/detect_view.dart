import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:flutter_smart_dialog/flutter_smart_dialog.dart';

import 'detect_controller.dart';
import 'dynamic_form_widget.dart';
import '../../models/detect_item_model.dart';
import '../../theme/app_theme.dart';

class DetectView extends GetView<DetectController> {
  const DetectView({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Obx(() => Text(
              controller.showItemList.value
                  ? '选择检测项目'
                  : '${controller.detectItemName ?? ''} 检测',
            )),
        centerTitle: true,
        leading: Obx(() => controller.showItemList.value
            ? const SizedBox.shrink()
            : IconButton(
                icon: const Icon(Icons.arrow_back),
                onPressed: () => controller.backToList(),
              )),
      ),
      body: Obx(() {
        if (controller.isLoading.value) {
          return const Center(
            child: CircularProgressIndicator(),
          );
        }

        if (controller.showItemList.value) {
          return _buildItemList();
        }

        return _buildFormView();
      }),
    );
  }

  Widget _buildItemList() {
    if (controller.detectItemList.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.error_outline,
              size: 64.sp,
              color: Colors.grey[400],
            ),
            SizedBox(height: 16.h),
            Text(
              '暂无检测项目',
              style: TextStyle(
                fontSize: 16.sp,
                color: Colors.grey[600],
              ),
            ),
          ],
        ),
      );
    }

    return ListView.builder(
      padding: EdgeInsets.all(12.w),
      itemCount: controller.detectItemList.length,
      itemBuilder: (context, index) {
        final item = controller.detectItemList[index];
        return _buildItemCard(item);
      },
    );
  }

  Widget _buildItemCard(DetectItemModel item) {
    return Card(
      margin: EdgeInsets.only(bottom: 12.h),
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(8.r),
      ),
      child: InkWell(
        borderRadius: BorderRadius.circular(8.r),
        onTap: () => controller.selectDetectItem(item),
        child: Padding(
          padding: EdgeInsets.all(16.w),
          child: Row(
            children: [
              Container(
                width: 48.w,
                height: 48.h,
                decoration: BoxDecoration(
                  color: AppTheme.primaryColor.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(8.r),
                ),
                child: Icon(
                  Icons.science,
                  color: AppTheme.primaryColor,
                  size: 24.sp,
                ),
              ),
              SizedBox(width: 12.w),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      item.itemName ?? '未知项目',
                      style: TextStyle(
                        fontSize: 16.sp,
                        fontWeight: FontWeight.bold,
                        color: AppTheme.textPrimary,
                      ),
                    ),
                    if (item.itemCode != null)
                      SizedBox(height: 4.h),
                    if (item.itemCode != null)
                      Text(
                        '编号: ${item.itemCode}',
                        style: TextStyle(
                          fontSize: 12.sp,
                          color: Colors.grey[500],
                        ),
                      ),
                    if (item.categoryName != null)
                      SizedBox(height: 4.h),
                    if (item.categoryName != null)
                      Text(
                        '分类: ${item.categoryName}',
                        style: TextStyle(
                          fontSize: 12.sp,
                          color: Colors.grey[500],
                        ),
                      ),
                    if (item.unit != null)
                      SizedBox(height: 4.h),
                    if (item.unit != null)
                      Text(
                        '单位: ${item.unit}',
                        style: TextStyle(
                          fontSize: 12.sp,
                          color: Colors.grey[500],
                        ),
                      ),
                  ],
                ),
              ),
              Icon(
                Icons.arrow_forward_ios,
                size: 16.sp,
                color: Colors.grey[400],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildFormView() {
    if (controller.formSchema.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.error_outline,
              size: 64.sp,
              color: Colors.grey[400],
            ),
            SizedBox(height: 16.h),
            Text(
              '表单加载失败',
              style: TextStyle(
                fontSize: 16.sp,
                color: Colors.grey[600],
              ),
            ),
            SizedBox(height: 16.h),
            ElevatedButton(
              onPressed: () {
                if (controller.detectItemId != null) {
                  controller.loadFormSchema(controller.detectItemId!);
                }
              },
              child: const Text('重新加载'),
            ),
          ],
        ),
      );
    }

    return SingleChildScrollView(
      padding: EdgeInsets.all(16.w),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildSampleInfo(),
          SizedBox(height: 16.h),
          _buildLimitStandard(),
          SizedBox(height: 16.h),
          _buildJudgeResult(),
          SizedBox(height: 16.h),
          DynamicFormWidget(
            schema: controller.formSchema,
            formData: controller.formData,
            judgeStatus: controller.judgeStatus.value,
            onChanged: controller.updateFormField,
          ),
          SizedBox(height: 24.h),
          _buildSubmitButton(),
          SizedBox(height: 32.h),
        ],
      ),
    );
  }

  Widget _buildSampleInfo() {
    return Container(
      padding: EdgeInsets.all(12.w),
      decoration: BoxDecoration(
        color: Colors.blue[50],
        borderRadius: BorderRadius.circular(8.r),
        border: Border.all(
          color: Colors.blue[200]!,
          width: 1.w,
        ),
      ),
      child: Row(
        children: [
          Icon(
            Icons.info_outline,
            color: Colors.blue[600],
            size: 20.sp,
          ),
          SizedBox(width: 8.w),
          Expanded(
            child: Text(
              '样品编号: ${controller.sampleCode ?? ''}',
              style: TextStyle(
                fontSize: 14.sp,
                color: Colors.blue[800],
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLimitStandard() {
    if (controller.detectItem.value?.limitStandards == null ||
        controller.detectItem.value!.limitStandards!.isEmpty) {
      return const SizedBox.shrink();
    }

    final standard = controller.detectItem.value!.limitStandards!.first;
    String limitText = '';

    switch (standard.limitType) {
      case 'max':
        limitText = '≤ ${standard.limitValueMax} ${standard.limitUnit ?? ''}';
        break;
      case 'min':
        limitText = '≥ ${standard.limitValueMin} ${standard.limitUnit ?? ''}';
        break;
      case 'range':
        limitText = '${standard.limitValueMin} ~ ${standard.limitValueMax} ${standard.limitUnit ?? ''}';
        break;
      case 'qualitative':
        limitText = standard.qualitativeResult ?? '';
        break;
    }

    return Container(
      padding: EdgeInsets.all(12.w),
      decoration: BoxDecoration(
        color: Colors.orange[50],
        borderRadius: BorderRadius.circular(8.r),
        border: Border.all(
          color: Colors.orange[200]!,
          width: 1.w,
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '限量标准: ${standard.standardName ?? ''}',
            style: TextStyle(
              fontSize: 14.sp,
              fontWeight: FontWeight.w600,
              color: Colors.orange[800],
            ),
          ),
          SizedBox(height: 4.h),
          if (standard.standardNo != null)
            Text(
              '标准号: ${standard.standardNo}',
              style: TextStyle(
                fontSize: 12.sp,
                color: Colors.orange[700],
              ),
            ),
          SizedBox(height: 4.h),
          Text(
            '限量要求: $limitText',
            style: TextStyle(
              fontSize: 13.sp,
              fontWeight: FontWeight.w500,
              color: Colors.orange[800],
            ),
          ),
          if (standard.description != null)
            Padding(
              padding: EdgeInsets.only(top: 4.h),
              child: Text(
                '说明: ${standard.description}',
                style: TextStyle(
                  fontSize: 12.sp,
                  color: Colors.orange[600],
                ),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildJudgeResult() {
    if (controller.judgeResult.value.isEmpty) {
      return const SizedBox.shrink();
    }

    final isQualified = controller.judgeStatus.value == 'qualified';
    final bgColor = isQualified ? Colors.green[50] : Colors.red[50];
    final borderColor = isQualified ? Colors.green[200] : Colors.red[200];
    final textColor = isQualified ? Colors.green[800] : Colors.red[800];
    final icon = isQualified ? Icons.check_circle : Icons.error;

    return Container(
      padding: EdgeInsets.all(16.w),
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: BorderRadius.circular(8.r),
        border: Border.all(
          color: borderColor!,
          width: 2.w,
        ),
      ),
      child: Row(
        children: [
          Icon(
            icon,
            color: isQualified ? Colors.green : Colors.red,
            size: 32.sp,
          ),
          SizedBox(width: 12.w),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '判定结果',
                  style: TextStyle(
                    fontSize: 14.sp,
                    color: textColor,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                SizedBox(height: 4.h),
                Text(
                  controller.judgeResult.value,
                  style: TextStyle(
                    fontSize: 24.sp,
                    color: isQualified ? Colors.green : Colors.red,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSubmitButton() {
    return SizedBox(
      width: double.infinity,
      height: 48.h,
      child: Obx(() => ElevatedButton(
            onPressed: controller.isSubmitting.value
                ? null
                : () async {
                    SmartDialog.showLoading(msg: '提交中...');
                    final success = await controller.submitResult(
                      taskId: controller.taskId ?? 0,
                      sampleId: controller.sampleId ?? 0,
                      sampleCode: controller.sampleCode ?? '',
                      detectItemId: controller.detectItemId ?? 0,
                      detectItemName: controller.detectItemName ?? '',
                    );
                    SmartDialog.dismiss();
                    if (success) {
                      Get.back(result: true);
                    }
                  },
            style: ElevatedButton.styleFrom(
              backgroundColor: Theme.of(Get.context!).primaryColor,
              foregroundColor: Colors.white,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8.r),
              ),
            ),
            child: controller.isSubmitting.value
                ? const CircularProgressIndicator(
                    color: Colors.white,
                    strokeWidth: 2,
                  )
                : Text(
                    '提交检测结果',
                    style: TextStyle(
                      fontSize: 16.sp,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
          )),
    );
  }
}
