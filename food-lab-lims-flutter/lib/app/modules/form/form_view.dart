import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';

import 'form_controller.dart';
import '../../widgets/form/form_builder_dynamic.dart';
import '../../widgets/form/json_dynamic_form.dart';
import '../../widgets/form/form_field_builder.dart';
import '../../models/form_template_model.dart';
import '../../models/form_data_model.dart';

class FormView extends GetView<FormController> {
  const FormView({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Obx(() => Text(controller.template.value?.templateName ?? '动态表单')),
        actions: [
          Obx(() {
            if (controller.isSavingDraft.value) {
              return Padding(
                padding: EdgeInsets.only(right: 16.w),
                child: Center(
                  child: SizedBox(
                    width: 16.w,
                    height: 16.w,
                    child: const CircularProgressIndicator(
                      strokeWidth: 2,
                      color: Colors.white,
                    ),
                  ),
                ),
              );
            }
            return const SizedBox.shrink();
          }),
          Obx(() {
            if (controller.template.value != null &&
                controller.formData.value != null &&
                !controller.formData.value!.isSubmitted) {
              return PopupMenuButton<String>(
                onSelected: _onMenuSelected,
                itemBuilder: (context) => [
                  PopupMenuItem(
                    value: 'switch_engine',
                    child: Row(
                      children: [
                        const Icon(Icons.swap_horiz),
                        SizedBox(width: 8.w),
                        const Text('切换渲染引擎'),
                      ],
                    ),
                  ),
                  PopupMenuItem(
                    value: 'save_draft',
                    child: Row(
                      children: [
                        const Icon(Icons.save),
                        SizedBox(width: 8.w),
                        const Text('保存草稿'),
                      ],
                    ),
                  ),
                  PopupMenuItem(
                    value: 'reset',
                    child: Row(
                      children: [
                        const Icon(Icons.refresh),
                        SizedBox(width: 8.w),
                        const Text('重置表单'),
                      ],
                    ),
                  ),
                  PopupMenuItem(
                    value: 'sync',
                    child: Row(
                      children: [
                        const Icon(Icons.sync),
                        SizedBox(width: 8.w),
                        const Text('同步数据'),
                      ],
                    ),
                  ),
                ],
              );
            }
            return const SizedBox.shrink();
          }),
        ],
      ),
      body: Obx(() {
        if (controller.isLoading.value) {
          return const Center(
            child: CircularProgressIndicator(),
          );
        }

        if (controller.template.value == null) {
          return Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(
                  Icons.error_outline,
                  size: 48.w,
                  color: Colors.grey[400],
                ),
                SizedBox(height: 16.h),
                Text(
                  '表单加载失败',
                  style: TextStyle(
                    fontSize: 16.sp,
                    color: Colors.grey[500],
                  ),
                ),
                SizedBox(height: 16.h),
                ElevatedButton(
                  onPressed: () {
                    if (controller.templateId != null) {
                      controller.loadFormTemplate(templateId: controller.templateId!);
                    } else if (controller.templateCode != null) {
                      controller.loadFormTemplate(templateCode: controller.templateCode!);
                    }
                  },
                  child: const Text('重新加载'),
                ),
              ],
            ),
          );
        }

        return _buildFormContent(context);
      }),
    );
  }

  Widget _buildFormContent(BuildContext context) {
    final template = controller.template.value!;
    final formData = controller.formData.value;

    return Column(
      children: [
        _buildJudgeResultBanner(),
        Expanded(
          child: SingleChildScrollView(
            padding: EdgeInsets.all(16.w),
            child: Obx(() {
              if (controller.renderEngine.value == 'json_dynamic') {
                return JsonDynamicForm(
                  template: template,
                  initialData: formData,
                  onChanged: _onFormChanged,
                  onSubmitted: _onFormSubmitted,
                  readOnly: formData?.isSubmitted ?? false,
                  showSubmitButton: false,
                );
              } else {
                return FormBuilderDynamic(
                  template: template,
                  initialData: formData,
                  onChanged: _onFormChanged,
                  onSubmitted: _onFormSubmitted,
                  readOnly: formData?.isSubmitted ?? false,
                  showSubmitButton: false,
                );
              }
            }),
          ),
        ),
        _buildBottomActions(),
      ],
    );
  }

  Widget _buildJudgeResultBanner() {
    return Obx(() {
      final formData = controller.formData.value;
      if (formData == null || formData.judgeStatus == null || formData.judgeStatus!.isEmpty) {
        return const SizedBox.shrink();
      }

      final isQualified = formData.judgeStatus == 'qualified';
      final bgColor = isQualified ? Colors.green : Colors.red;
      final icon = isQualified ? Icons.check_circle : Icons.error;

      return Container(
        width: double.infinity,
        padding: EdgeInsets.symmetric(horizontal: 16.w, vertical: 12.h),
        color: bgColor,
        child: Row(
          children: [
            Icon(
              icon,
              color: Colors.white,
              size: 20.sp,
            ),
            SizedBox(width: 8.w),
            Text(
              '判定结果：${formData.judgeResult ?? ''}',
              style: TextStyle(
                color: Colors.white,
                fontSize: 14.sp,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      );
    });
  }

  Widget _buildBottomActions() {
    return Obx(() {
      final formData = controller.formData.value;
      if (formData?.isSubmitted ?? false) {
        return Container(
          padding: EdgeInsets.all(16.w),
          decoration: BoxDecoration(
            color: Colors.white,
            boxShadow: [
              BoxShadow(
                color: Colors.grey.withOpacity(0.1),
                blurRadius: 4,
                offset: const Offset(0, -2),
              ),
            ],
          ),
          child: SizedBox(
            width: double.infinity,
            height: 48.h,
            child: ElevatedButton(
              onPressed: () => Get.back(),
              child: const Text('返回'),
            ),
          ),
        );
      }

      return Container(
        padding: EdgeInsets.all(16.w),
        decoration: BoxDecoration(
          color: Colors.white,
          boxShadow: [
            BoxShadow(
              color: Colors.grey.withOpacity(0.1),
              blurRadius: 4,
              offset: const Offset(0, -2),
            ),
          ],
        ),
        child: Row(
          children: [
            Expanded(
              child: SizedBox(
                height: 48.h,
                child: OutlinedButton(
                  onPressed: controller.isSubmitting.value
                      ? null
                      : () async {
                          await controller.saveFormData();
                        },
                  child: controller.isSubmitting.value
                      ? SizedBox(
                          width: 16.w,
                          height: 16.w,
                          child: const CircularProgressIndicator(
                            strokeWidth: 2,
                          ),
                        )
                      : const Text('保存'),
                ),
              ),
            ),
            SizedBox(width: 16.w),
            Expanded(
              child: SizedBox(
                height: 48.h,
                child: ElevatedButton(
                  onPressed: controller.isSubmitting.value
                      ? null
                      : () async {
                          final success = await controller.submitForm();
                          if (success) {
                            Get.back(result: controller.formData.value);
                          }
                        },
                  child: controller.isSubmitting.value
                      ? SizedBox(
                          width: 16.w,
                          height: 16.w,
                          child: const CircularProgressIndicator(
                            strokeWidth: 2,
                            color: Colors.white,
                          ),
                        )
                      : const Text('提交'),
                ),
              ),
            ),
          ],
        ),
      );
    });
  }

  void _onFormChanged(FormDataModel data) {
    controller.formData.value = data;
  }

  Future<void> _onFormSubmitted(FormDataModel data) async {
    controller.formData.value = data;
    final success = await controller.submitForm();
    if (success) {
      Get.back(result: controller.formData.value);
    }
  }

  void _onMenuSelected(String value) async {
    switch (value) {
      case 'switch_engine':
        _showEngineSwitchDialog();
        break;
      case 'save_draft':
        await controller.saveDraft();
        break;
      case 'reset':
        _showResetConfirmDialog();
        break;
      case 'sync':
        await controller.syncPendingData();
        break;
    }
  }

  void _showEngineSwitchDialog() {
    Get.dialog(
      AlertDialog(
        title: const Text('切换渲染引擎'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              title: const Text('Form Builder'),
              subtitle: const Text('基于 flutter_form_builder，功能更丰富'),
              leading: Radio<String>(
                value: 'form_builder',
                groupValue: controller.renderEngine.value,
                onChanged: (value) {
                  if (value != null) {
                    controller.switchRenderEngine(value);
                    Get.back();
                  }
                },
              ),
            ),
            ListTile(
              title: const Text('JSON Dynamic'),
              subtitle: const Text('基于 json_dynamic_widget，支持 JSON Schema'),
              leading: Radio<String>(
                value: 'json_dynamic',
                groupValue: controller.renderEngine.value,
                onChanged: (value) {
                  if (value != null) {
                    controller.switchRenderEngine(value);
                    Get.back();
                  }
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _showResetConfirmDialog() {
    Get.dialog(
      AlertDialog(
        title: const Text('确认重置'),
        content: const Text('确定要重置表单吗？所有已填写的内容将被清空。'),
        actions: [
          TextButton(
            onPressed: () => Get.back(),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () {
              controller.resetForm();
              Get.back();
            },
            child: const Text('确定'),
          ),
        ],
      ),
    );
  }
}
