import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:easy_refresh/easy_refresh.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';

import '../../theme/app_theme.dart';
import 'sample_controller.dart';

class SampleView extends GetView<SampleController> {
  const SampleView({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppTheme.bgColor,
      body: Obx(() => EasyRefresh(
            onRefresh: () async {
              await controller.loadSamples(isRefresh: true);
            },
            onLoad: () async {
              await controller.loadSamples();
            },
            child: controller.sampleList.isEmpty
                ? _buildEmptyView()
                : ListView.builder(
                    padding: EdgeInsets.all(12.w),
                    itemCount: controller.sampleList.length,
                    itemBuilder: (context, index) {
                      final sample = controller.sampleList[index];
                      return _buildSampleItem(sample);
                    },
                  ),
          )),
      floatingActionButton: FloatingActionButton(
        onPressed: controller.goToRegister,
        backgroundColor: AppTheme.primaryColor,
        child: const Icon(Icons.add),
      ),
    );
  }

  Widget _buildEmptyView() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.inbox_outlined,
            size: 80.sp,
            color: Colors.grey[400],
          ),
          SizedBox(height: 16.h),
          Text(
            '暂无样品数据',
            style: TextStyle(
              fontSize: 16.sp,
              color: Colors.grey[500],
            ),
          ),
          SizedBox(height: 8.h),
          Text(
            '点击右下角按钮登记样品',
            style: TextStyle(
              fontSize: 14.sp,
              color: Colors.grey[400],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSampleItem(SampleModel sample) {
    return Card(
      margin: EdgeInsets.only(bottom: 12.h),
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(8.r),
      ),
      child: InkWell(
        borderRadius: BorderRadius.circular(8.r),
        onTap: () {
        },
        child: Padding(
          padding: EdgeInsets.all(12.w),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Expanded(
                    child: Text(
                      sample.sampleName ?? '未知样品',
                      style: TextStyle(
                        fontSize: 16.sp,
                        fontWeight: FontWeight.bold,
                        color: AppTheme.textPrimary,
                      ),
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  _buildSyncStatusTag(sample.syncStatus),
                ],
              ),
              SizedBox(height: 8.h),
              _buildInfoRow(Icons.qr_code, '样品编号', sample.sampleCode),
              SizedBox(height: 4.h),
              _buildInfoRow(Icons.label, '批号', sample.batchNo),
              SizedBox(height: 4.h),
              _buildInfoRow(Icons.factory, '生产单位', sample.manufacturer),
              SizedBox(height: 4.h),
              _buildInfoRow(Icons.location_on, '采样地点', sample.sampleLocation),
              SizedBox(height: 8.h),
              Row(
                children: [
                  Icon(
                    Icons.science,
                    size: 14.sp,
                    color: Colors.grey[500],
                  ),
                  SizedBox(width: 4.w),
                  Expanded(
                    child: Text(
                      '检测项目: ${sample.detectItemIds?.length ?? 0}项',
                      style: TextStyle(
                        fontSize: 13.sp,
                        color: Colors.grey[600],
                      ),
                    ),
                  ),
                  if (sample.createTime != null)
                    Text(
                      _formatDate(sample.createTime!),
                      style: TextStyle(
                        fontSize: 12.sp,
                        color: Colors.grey[400],
                      ),
                    ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildInfoRow(IconData icon, String label, String? value) {
    return Row(
      children: [
        Icon(
          icon,
          size: 14.sp,
          color: Colors.grey[500],
        ),
        SizedBox(width: 4.w),
        Text(
          '$label: ',
          style: TextStyle(
            fontSize: 13.sp,
            color: Colors.grey[600],
          ),
        ),
        Expanded(
          child: Text(
            value ?? '-',
            style: TextStyle(
              fontSize: 13.sp,
              color: AppTheme.textPrimary,
            ),
            overflow: TextOverflow.ellipsis,
          ),
        ),
      ],
    );
  }

  Widget _buildSyncStatusTag(String? status) {
    Color bgColor;
    String text;
    Color textColor;

    switch (status) {
      case 'synced':
        bgColor = Colors.green.withOpacity(0.1);
        textColor = Colors.green;
        text = '已同步';
        break;
      case 'pending':
        bgColor = Colors.orange.withOpacity(0.1);
        textColor = Colors.orange;
        text = '待同步';
        break;
      default:
        bgColor = Colors.blue.withOpacity(0.1);
        textColor = Colors.blue;
        text = '正常';
    }

    return Container(
      padding: EdgeInsets.symmetric(horizontal: 8.w, vertical: 2.h),
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: BorderRadius.circular(4.r),
      ),
      child: Text(
        text,
        style: TextStyle(
          fontSize: 11.sp,
          color: textColor,
        ),
      ),
    );
  }

  String _formatDate(String dateStr) {
    try {
      final date = DateTime.parse(dateStr);
      return '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
    } catch (e) {
      return dateStr;
    }
  }
}
