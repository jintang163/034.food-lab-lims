import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:easy_refresh/easy_refresh.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';

import '../../theme/app_theme.dart';
import '../../models/task_model.dart';
import 'task_controller.dart';
import '../../constants/task_constants.dart';

class TaskView extends GetView<TaskController> {
  const TaskView({super.key});

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: controller.tabTitles.length,
      child: Scaffold(
        backgroundColor: AppTheme.bgColor,
        body: Column(
          children: [
            Container(
              color: Colors.white,
              child: Obx(() => TabBar(
                    onTap: (index) => controller.currentIndex.value = index,
                    labelColor: AppTheme.primaryColor,
                    unselectedLabelColor: Colors.grey[600],
                    labelStyle: TextStyle(
                      fontSize: 14.sp,
                      fontWeight: FontWeight.w500,
                    ),
                    unselectedLabelStyle: TextStyle(fontSize: 14.sp),
                    indicatorColor: AppTheme.primaryColor,
                    indicatorWeight: 2,
                    tabs: controller.tabTitles
                        .asMap()
                        .entries
                        .map((entry) => Tab(
                              child: Row(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  Text(entry.value),
                                  SizedBox(width: 4.w),
                                  _buildTabBadge(entry.key),
                                ],
                              ),
                            ))
                        .toList(),
                  )),
            ),
            Expanded(
              child: Obx(() => EasyRefresh(
                    onRefresh: () async {
                      await controller.loadMyTasks(isRefresh: true);
                    },
                    child: _buildTaskList(),
                  )),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTabBadge(int index) {
    int count;
    switch (index) {
      case 0:
        count = controller.pendingTasks.length;
        break;
      case 1:
        count = controller.detectingTasks.length;
        break;
      case 2:
        count = controller.auditingTasks.length;
        break;
      case 3:
        count = controller.completedTasks.length;
        break;
      default:
        count = 0;
    }

    if (count == 0) return const SizedBox.shrink();

    return Container(
      padding: EdgeInsets.symmetric(horizontal: 6.w, vertical: 1.h),
      decoration: BoxDecoration(
        color: index == 0
            ? Colors.grey
            : index == 1
                ? Colors.blue
                : index == 2
                    ? Colors.green
                    : Colors.orange,
        borderRadius: BorderRadius.circular(10.r),
      ),
      constraints: BoxConstraints(
        minWidth: 18.w,
        minHeight: 18.h,
      ),
      child: Text(
        count > 99 ? '99+' : count.toString(),
        style: TextStyle(
          color: Colors.white,
          fontSize: 10.sp,
          fontWeight: FontWeight.bold,
        ),
        textAlign: TextAlign.center,
      ),
    );
  }

  Widget _buildTaskList() {
    final tasks = controller.getCurrentTasks();

    if (controller.isLoading.value && tasks.isEmpty) {
      return const Center(
        child: CircularProgressIndicator(color: AppTheme.primaryColor),
      );
    }

    if (tasks.isEmpty) {
      return _buildEmptyView();
    }

    return ListView.builder(
      padding: EdgeInsets.all(12.w),
      itemCount: tasks.length,
      itemBuilder: (context, index) {
        final task = tasks[index];
        return _buildTaskItem(task);
      },
    );
  }

  Widget _buildEmptyView() {
    String emptyText;
    switch (controller.currentIndex.value) {
      case 0:
        emptyText = '暂无待检任务';
        break;
      case 1:
        emptyText = '暂无检测中任务';
        break;
      case 2:
        emptyText = '暂无审核中任务';
        break;
      case 3:
        emptyText = '暂无已完成任务';
        break;
      default:
        emptyText = '暂无任务数据';
    }

    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.assignment_outlined,
            size: 80.sp,
            color: Colors.grey[400],
          ),
          SizedBox(height: 16.h),
          Text(
            emptyText,
            style: TextStyle(
              fontSize: 16.sp,
              color: Colors.grey[500],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTaskItem(TaskModel task) {
    return Card(
      margin: EdgeInsets.only(bottom: 12.h),
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(8.r),
      ),
      child: InkWell(
        borderRadius: BorderRadius.circular(8.r),
        onTap: () => controller.goToDetect(task),
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
                      task.taskName ?? '未知任务',
                      style: TextStyle(
                        fontSize: 16.sp,
                        fontWeight: FontWeight.bold,
                        color: AppTheme.textPrimary,
                      ),
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  _buildStatusTag(task.taskStatus),
                ],
              ),
              SizedBox(height: 8.h),
              _buildInfoRow(Icons.confirmation_number, '任务编号', task.taskCode),
              SizedBox(height: 4.h),
              _buildInfoRow(Icons.qr_code, '样品编号', task.sampleCode),
              SizedBox(height: 4.h),
              _buildInfoRow(
                  Icons.person, '分配人', task.assignByName ?? '系统'),
              SizedBox(height: 4.h),
              if (task.detectPersonName != null)
                _buildInfoRow(Icons.science, '检测人', task.detectPersonName!),
              SizedBox(height: 8.h),
              if (task.detectItemCount != null)
                Row(
                  children: [
                    Icon(
                      Icons.list_alt,
                      size: 14.sp,
                      color: Colors.grey[500],
                    ),
                    SizedBox(width: 4.w),
                    Text(
                      '检测项目: ${task.completedItemCount ?? 0}/${task.detectItemCount}',
                      style: TextStyle(
                        fontSize: 13.sp,
                        color: Colors.grey[600],
                      ),
                    ),
                    SizedBox(width: 16.w),
                    if (task.deadline != null)
                      Expanded(
                        child: Row(
                          children: [
                            Icon(
                              Icons.schedule,
                              size: 14.sp,
                              color: Colors.grey[500],
                            ),
                            SizedBox(width: 4.w),
                            Expanded(
                              child: Text(
                                '截止: ${_formatDate(task.deadline!)}',
                                style: TextStyle(
                                  fontSize: 13.sp,
                                  color: _isOverdue(task.deadline!)
                                      ? Colors.red
                                      : Colors.grey[600],
                                ),
                                overflow: TextOverflow.ellipsis,
                              ),
                            ),
                          ],
                        ),
                      ),
                  ],
                ),
              SizedBox(height: 8.h),
              if (task.detectItemCount != null &&
                  task.detectItemCount! > 0 &&
                  task.taskStatus == TaskConstants.detecting)
                LinearProgressIndicator(
                  value: (task.completedItemCount ?? 0) / task.detectItemCount!,
                  backgroundColor: Colors.grey[200],
                  valueColor:
                      const AlwaysStoppedAnimation<Color>(AppTheme.primaryColor),
                  minHeight: 6.h,
                  borderRadius: BorderRadius.circular(3.r),
                ),
              SizedBox(height: 8.h),
              if (task.taskStatus == TaskConstants.pending)
                Align(
                  alignment: Alignment.centerRight,
                  child: ElevatedButton.icon(
                    onPressed: () => controller.goToDetect(task),
                    icon: const Icon(Icons.play_arrow, size: 18),
                    label: const Text('开始检测'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppTheme.primaryColor,
                      foregroundColor: Colors.white,
                      padding: EdgeInsets.symmetric(
                          horizontal: 16.w, vertical: 8.h),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(6.r),
                      ),
                    ),
                  ),
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

  Widget _buildStatusTag(String? status) {
    final bgColor = controller.getStatusColor(status).withOpacity(0.1);
    final textColor = controller.getStatusColor(status);
    final text = controller.getStatusText(status);

    return Container(
      padding: EdgeInsets.symmetric(horizontal: 10.w, vertical: 4.h),
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: BorderRadius.circular(4.r),
      ),
      child: Text(
        text,
        style: TextStyle(
          fontSize: 12.sp,
          color: textColor,
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }

  bool _isOverdue(String deadline) {
    try {
      final deadlineDate = DateTime.parse(deadline);
      final now = DateTime.now();
      return now.isAfter(deadlineDate);
    } catch (e) {
      return false;
    }
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
