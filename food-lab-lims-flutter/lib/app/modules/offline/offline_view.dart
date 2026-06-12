import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';

import 'offline_controller.dart';

class OfflineView extends GetView<OfflineController> {
  const OfflineView({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('离线数据同步'),
        centerTitle: true,
      ),
      body: Obx(() {
        if (controller.isLoading.value) {
          return const Center(
            child: CircularProgressIndicator(),
          );
        }

        return RefreshIndicator(
          onRefresh: () => controller.loadPendingData(),
          child: SingleChildScrollView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: EdgeInsets.all(16.w),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildStatsCard(),
                SizedBox(height: 16.h),
                _buildActionButtons(),
                SizedBox(height: 16.h),
                if (controller.isSyncing.value || controller.syncStatus.value.isNotEmpty)
                  _buildSyncProgress(),
                if (controller.syncErrors.isNotEmpty) ...[
                  SizedBox(height: 16.h),
                  _buildSyncErrors(),
                ],
                SizedBox(height: 16.h),
                _buildDataList(),
              ],
            ),
          ),
        );
      }),
    );
  }

  Widget _buildStatsCard() {
    return Container(
      padding: EdgeInsets.all(16.w),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12.r),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            blurRadius: 8.r,
            offset: Offset(0, 2.h),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '待同步数据统计',
            style: TextStyle(
              fontSize: 16.sp,
              fontWeight: FontWeight.w600,
              color: Colors.grey[800],
            ),
          ),
          SizedBox(height: 16.h),
          Row(
            children: [
              Expanded(
                child: _buildStatItem(
                  icon: Icons.inventory_2_outlined,
                  label: '待同步样品',
                  count: controller.pendingSamples.length,
                  color: Colors.blue,
                ),
              ),
              SizedBox(width: 12.w),
              Expanded(
                child: _buildStatItem(
                  icon: Icons.assignment_outlined,
                  label: '待同步检测结果',
                  count: controller.pendingDetectResults.length,
                  color: Colors.orange,
                ),
              ),
            ],
          ),
          SizedBox(height: 12.h),
          Container(
            padding: EdgeInsets.all(12.w),
            decoration: BoxDecoration(
              color: (controller.pendingSamples.length + controller.pendingDetectResults.length) > 0
                  ? Colors.orange[50]
                  : Colors.green[50],
              borderRadius: BorderRadius.circular(8.r),
            ),
            child: Row(
              children: [
                Icon(
                  (controller.pendingSamples.length + controller.pendingDetectResults.length) > 0
                      ? Icons.info_outline
                      : Icons.check_circle_outline,
                  color: (controller.pendingSamples.length + controller.pendingDetectResults.length) > 0
                      ? Colors.orange[600]
                      : Colors.green[600],
                  size: 20.sp,
                ),
                SizedBox(width: 8.w),
                Expanded(
                  child: Text(
                    (controller.pendingSamples.length + controller.pendingDetectResults.length) > 0
                        ? '共有 ${controller.pendingSamples.length + controller.pendingDetectResults.length} 条数据待同步'
                        : '所有数据已同步完成',
                    style: TextStyle(
                      fontSize: 13.sp,
                      color: (controller.pendingSamples.length + controller.pendingDetectResults.length) > 0
                          ? Colors.orange[800]
                          : Colors.green[800],
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatItem({
    required IconData icon,
    required String label,
    required int count,
    required Color color,
  }) {
    return Container(
      padding: EdgeInsets.all(12.w),
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(8.r),
        border: Border.all(
          color: color.withOpacity(0.3),
          width: 1.w,
        ),
      ),
      child: Column(
        children: [
          Icon(
            icon,
            color: color,
            size: 32.sp,
          ),
          SizedBox(height: 8.h),
          Text(
            '$count',
            style: TextStyle(
              fontSize: 24.sp,
              fontWeight: FontWeight.bold,
              color: color,
            ),
          ),
          SizedBox(height: 4.h),
          Text(
            label,
            style: TextStyle(
              fontSize: 12.sp,
              color: Colors.grey[600],
            ),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }

  Widget _buildActionButtons() {
    final hasData = controller.pendingSamples.isNotEmpty ||
        controller.pendingDetectResults.isNotEmpty;

    return Column(
      children: [
        SizedBox(
          width: double.infinity,
          height: 48.h,
          child: ElevatedButton.icon(
            onPressed: (controller.isSyncing.value || !hasData)
                ? null
                : () => controller.syncAll(),
            style: ElevatedButton.styleFrom(
              backgroundColor: Theme.of(Get.context!).primaryColor,
              foregroundColor: Colors.white,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8.r),
              ),
            ),
            icon: const Icon(Icons.sync),
            label: Text(
              controller.isSyncing.value ? '同步中...' : '同步所有数据',
              style: TextStyle(
                fontSize: 16.sp,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
        ),
        SizedBox(height: 12.h),
        Row(
          children: [
            Expanded(
              child: SizedBox(
                height: 44.h,
                child: OutlinedButton.icon(
                  onPressed: (controller.isSyncing.value || controller.pendingSamples.isEmpty)
                      ? null
                      : () => controller.syncSamplesOnly(),
                  style: OutlinedButton.styleFrom(
                    foregroundColor: Colors.blue,
                    side: const BorderSide(color: Colors.blue),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8.r),
                    ),
                  ),
                  icon: const Icon(Icons.inventory_2_outlined, size: 18),
                  label: Text(
                    '同步样品 (${controller.pendingSamples.length})',
                    style: TextStyle(fontSize: 13.sp),
                  ),
                ),
              ),
            ),
            SizedBox(width: 12.w),
            Expanded(
              child: SizedBox(
                height: 44.h,
                child: OutlinedButton.icon(
                  onPressed: (controller.isSyncing.value || controller.pendingDetectResults.isEmpty)
                      ? null
                      : () => controller.syncResultsOnly(),
                  style: OutlinedButton.styleFrom(
                    foregroundColor: Colors.orange,
                    side: const BorderSide(color: Colors.orange),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8.r),
                    ),
                  ),
                  icon: const Icon(Icons.assignment_outlined, size: 18),
                  label: Text(
                    '同步结果 (${controller.pendingDetectResults.length})',
                    style: TextStyle(fontSize: 13.sp),
                  ),
                ),
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildSyncProgress() {
    return Container(
      padding: EdgeInsets.all(16.w),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12.r),
        border: Border.all(
          color: controller.syncStatus.value == 'failed'
              ? Colors.red[200]!
              : controller.syncStatus.value == 'completed'
                  ? Colors.green[200]!
                  : Colors.blue[200]!,
          width: 1.w,
        ),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            blurRadius: 8.r,
            offset: Offset(0, 2.h),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(
                controller.syncStatus.value == 'failed'
                    ? Icons.error_outline
                    : controller.syncStatus.value == 'completed'
                        ? Icons.check_circle_outline
                        : Icons.sync,
                color: controller.syncStatus.value == 'failed'
                    ? Colors.red
                    : controller.syncStatus.value == 'completed'
                        ? Colors.green
                        : Colors.blue,
                size: 24.sp,
              ),
              SizedBox(width: 12.w),
              Expanded(
                child: Text(
                  controller.syncStatus.value == 'failed'
                      ? '同步失败'
                      : controller.syncStatus.value == 'completed'
                          ? '同步完成'
                          : '正在同步...',
                  style: TextStyle(
                    fontSize: 16.sp,
                    fontWeight: FontWeight.w600,
                    color: controller.syncStatus.value == 'failed'
                        ? Colors.red[800]
                        : controller.syncStatus.value == 'completed'
                            ? Colors.green[800]
                            : Colors.blue[800],
                  ),
                ),
              ),
              Text(
                '${controller.syncProgress.value}/${controller.syncTotal.value}',
                style: TextStyle(
                  fontSize: 14.sp,
                  fontWeight: FontWeight.w600,
                  color: Colors.grey[600],
                ),
              ),
            ],
          ),
          SizedBox(height: 12.h),
          ClipRRect(
            borderRadius: BorderRadius.circular(4.r),
            child: LinearProgressIndicator(
              value: controller.progressPercent,
              minHeight: 8.h,
              backgroundColor: Colors.grey[200],
              valueColor: AlwaysStoppedAnimation<Color>(
                controller.syncStatus.value == 'failed'
                    ? Colors.red
                    : controller.syncStatus.value == 'completed'
                        ? Colors.green
                        : Colors.blue,
              ),
            ),
          ),
          SizedBox(height: 8.h),
          if (controller.currentSyncItem.value.isNotEmpty)
            Text(
              controller.currentSyncItem.value,
              style: TextStyle(
                fontSize: 13.sp,
                color: Colors.grey[600],
              ),
            ),
          if (controller.syncStatus.value == 'completed')
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  '成功: ${controller.syncSuccess.value} 条',
                  style: TextStyle(
                    fontSize: 13.sp,
                    color: Colors.green,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                Text(
                  '失败: ${controller.syncFailed.value} 条',
                  style: TextStyle(
                    fontSize: 13.sp,
                    color: controller.syncFailed.value > 0 ? Colors.red : Colors.grey[600],
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ],
            ),
        ],
      ),
    );
  }

  Widget _buildSyncErrors() {
    return Container(
      padding: EdgeInsets.all(16.w),
      decoration: BoxDecoration(
        color: Colors.red[50],
        borderRadius: BorderRadius.circular(12.r),
        border: Border.all(
          color: Colors.red[200]!,
          width: 1.w,
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(
                Icons.error_outline,
                color: Colors.red[600],
                size: 20.sp,
              ),
              SizedBox(width: 8.w),
              Text(
                '同步失败详情 (${controller.syncErrors.length} 条)',
                style: TextStyle(
                  fontSize: 14.sp,
                  fontWeight: FontWeight.w600,
                  color: Colors.red[800],
                ),
              ),
            ],
          ),
          SizedBox(height: 12.h),
          ...controller.syncErrors.map((error) => Padding(
                padding: EdgeInsets.only(bottom: 8.h),
                child: Container(
                  padding: EdgeInsets.all(8.w),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(6.r),
                  ),
                  child: Text(
                    error,
                    style: TextStyle(
                      fontSize: 12.sp,
                      color: Colors.red[700],
                    ),
                  ),
                ),
              )),
        ],
      ),
    );
  }

  Widget _buildDataList() {
    if (controller.pendingSamples.isEmpty && controller.pendingDetectResults.isEmpty) {
      return Center(
        child: Column(
          children: [
            Icon(
              Icons.cloud_done_outlined,
              size: 64.sp,
              color: Colors.grey[300],
            ),
            SizedBox(height: 16.h),
            Text(
              '暂无待同步数据',
              style: TextStyle(
                fontSize: 16.sp,
                color: Colors.grey[500],
              ),
            ),
            SizedBox(height: 8.h),
            Text(
              '下拉刷新数据',
              style: TextStyle(
                fontSize: 14.sp,
                color: Colors.grey[400],
              ),
            ),
          ],
        ),
      );
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (controller.pendingSamples.isNotEmpty) ...[
          Text(
            '待同步样品列表',
            style: TextStyle(
              fontSize: 16.sp,
              fontWeight: FontWeight.w600,
              color: Colors.grey[800],
            ),
          ),
          SizedBox(height: 12.h),
          ...controller.pendingSamples.map((sample) => _buildSampleItem(sample)),
          SizedBox(height: 16.h),
        ],
        if (controller.pendingDetectResults.isNotEmpty) ...[
          Text(
            '待同步检测结果列表',
            style: TextStyle(
              fontSize: 16.sp,
              fontWeight: FontWeight.w600,
              color: Colors.grey[800],
            ),
          ),
          SizedBox(height: 12.h),
          ...controller.pendingDetectResults
              .map((result) => _buildDetectResultItem(result)),
        ],
      ],
    );
  }

  Widget _buildSampleItem(SampleModel sample) {
    return Container(
      margin: EdgeInsets.only(bottom: 12.h),
      padding: EdgeInsets.all(12.w),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(8.r),
        border: Border.all(
          color: Colors.blue[200]!,
          width: 1.w,
        ),
      ),
      child: Row(
        children: [
          Container(
            width: 40.w,
            height: 40.h,
            decoration: BoxDecoration(
              color: Colors.blue[100],
              borderRadius: BorderRadius.circular(8.r),
            ),
            child: Icon(
              Icons.inventory_2_outlined,
              color: Colors.blue[600],
              size: 20.sp,
            ),
          ),
          SizedBox(width: 12.w),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  sample.sampleName ?? '未命名样品',
                  style: TextStyle(
                    fontSize: 14.sp,
                    fontWeight: FontWeight.w600,
                    color: Colors.grey[800],
                  ),
                ),
                SizedBox(height: 4.h),
                Text(
                  '编号: ${sample.sampleCode ?? '-'}',
                  style: TextStyle(
                    fontSize: 12.sp,
                    color: Colors.grey[600],
                  ),
                ),
                if (sample.createTime != null)
                  SizedBox(height: 4.h),
                if (sample.createTime != null)
                  Text(
                    '创建时间: ${sample.createTime!.split('T').first}',
                    style: TextStyle(
                      fontSize: 12.sp,
                      color: Colors.grey[500],
                    ),
                  ),
              ],
            ),
          ),
          Container(
            padding: EdgeInsets.symmetric(horizontal: 8.w, vertical: 4.h),
            decoration: BoxDecoration(
              color: Colors.orange[100],
              borderRadius: BorderRadius.circular(4.r),
            ),
            child: Text(
              '待同步',
              style: TextStyle(
                fontSize: 12.sp,
                color: Colors.orange[700],
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDetectResultItem(DetectResultModel result) {
    return Container(
      margin: EdgeInsets.only(bottom: 12.h),
      padding: EdgeInsets.all(12.w),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(8.r),
        border: Border.all(
          color: Colors.orange[200]!,
          width: 1.w,
        ),
      ),
      child: Row(
        children: [
          Container(
            width: 40.w,
            height: 40.h,
            decoration: BoxDecoration(
              color: Colors.orange[100],
              borderRadius: BorderRadius.circular(8.r),
            ),
            child: Icon(
              Icons.assignment_outlined,
              color: Colors.orange[600],
              size: 20.sp,
            ),
          ),
          SizedBox(width: 12.w),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  result.detectItemName ?? '未命名检测项目',
                  style: TextStyle(
                    fontSize: 14.sp,
                    fontWeight: FontWeight.w600,
                    color: Colors.grey[800],
                  ),
                ),
                SizedBox(height: 4.h),
                Text(
                  '样品: ${result.sampleCode ?? '-'}',
                  style: TextStyle(
                    fontSize: 12.sp,
                    color: Colors.grey[600],
                  ),
                ),
                SizedBox(height: 4.h),
                if (result.resultType == 'quantitative')
                  Text(
                    '结果: ${result.resultValue ?? '-'} ${result.resultUnit ?? ''}',
                    style: TextStyle(
                      fontSize: 12.sp,
                      color: Colors.grey[500],
                    ),
                  )
                else
                  Text(
                    '结果: ${result.qualitativeResult ?? '-'}',
                    style: TextStyle(
                      fontSize: 12.sp,
                      color: Colors.grey[500],
                    ),
                  ),
              ],
            ),
          ),
          Container(
            padding: EdgeInsets.symmetric(horizontal: 8.w, vertical: 4.h),
            decoration: BoxDecoration(
              color: Colors.orange[100],
              borderRadius: BorderRadius.circular(4.r),
            ),
            child: Text(
              '待同步',
              style: TextStyle(
                fontSize: 12.sp,
                color: Colors.orange[700],
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
