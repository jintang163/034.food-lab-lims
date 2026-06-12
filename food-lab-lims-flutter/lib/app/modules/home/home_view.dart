import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';

import 'home_controller.dart';
import '../../theme/app_theme.dart';
import '../sample/sample_view.dart';
import '../sample/sample_binding.dart';
import '../task/task_view.dart';
import '../task/task_binding.dart';

class HomeView extends GetView<HomeController> {
  const HomeView({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Obx(() => IndexedStack(
            index: controller.currentIndex.value,
            children: [
              _buildHomePage(),
              _buildSamplePage(),
              _buildTaskPage(),
              _buildProfilePage(),
            ],
          )),
      bottomNavigationBar: Obx(() => BottomNavigationBar(
            type: BottomNavigationBarType.fixed,
            currentIndex: controller.currentIndex.value,
            onTap: controller.changeIndex,
            selectedItemColor: AppTheme.primaryColor,
            unselectedItemColor: AppTheme.textSecondary,
            selectedLabelStyle: TextStyle(fontSize: 12.sp, fontWeight: FontWeight.w500),
            unselectedLabelStyle: TextStyle(fontSize: 12.sp),
            items: const [
              BottomNavigationBarItem(
                icon: Icon(Icons.home_outlined),
                activeIcon: Icon(Icons.home),
                label: '首页',
              ),
              BottomNavigationBarItem(
                icon: Icon(Icons.inventory_2_outlined),
                activeIcon: Icon(Icons.inventory_2),
                label: '样品',
              ),
              BottomNavigationBarItem(
                icon: Icon(Icons.assignment_outlined),
                activeIcon: Icon(Icons.assignment),
                label: '任务',
              ),
              BottomNavigationBarItem(
                icon: Icon(Icons.person_outline),
                activeIcon: Icon(Icons.person),
                label: '我的',
              ),
            ],
          )),
    );
  }

  Widget _buildHomePage() {
    return Scaffold(
      appBar: AppBar(
        title: const Text('首页'),
        automaticallyImplyLeading: false,
      ),
      body: SingleChildScrollView(
        child: Padding(
          padding: EdgeInsets.all(16.w),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildWelcomeCard(),
              SizedBox(height: 16.h),
              _buildQuickActions(),
              SizedBox(height: 16.h),
              _buildStatistics(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildWelcomeCard() {
    return Container(
      width: double.infinity,
      padding: EdgeInsets.all(20.w),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [AppTheme.primaryColor, AppTheme.secondaryColor],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(16.r),
        boxShadow: [
          BoxShadow(
            color: AppTheme.primaryColor.withOpacity(0.3),
            blurRadius: 12,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Obx(() => Text(
            '您好，${controller.userName.value.isNotEmpty ? controller.userName.value : '用户'}',
            style: TextStyle(
              fontSize: 22.sp,
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          )),
          SizedBox(height: 8.h),
          Text(
            '欢迎使用食品检测LIMS系统',
            style: TextStyle(
              fontSize: 14.sp,
              color: Colors.white.withOpacity(0.9),
            ),
          ),
          SizedBox(height: 12.h),
          Row(
            children: [
              Icon(Icons.access_time, size: 16.sp, color: Colors.white.withOpacity(0.8)),
              SizedBox(width: 4.w),
              Text(
                '今天是 ${_getCurrentDate()}',
                style: TextStyle(
                  fontSize: 13.sp,
                  color: Colors.white.withOpacity(0.8),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  String _getCurrentDate() {
    final now = DateTime.now();
    return '${now.year}年${now.month}月${now.day}日';
  }

  Widget _buildQuickActions() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '快捷操作',
          style: TextStyle(
            fontSize: 18.sp,
            fontWeight: FontWeight.bold,
            color: AppTheme.textPrimary,
          ),
        ),
        SizedBox(height: 12.h),
        Row(
          children: [
            Expanded(
              child: _buildActionCard(
                icon: Icons.add_circle_outline,
                title: '样品登记',
                subtitle: '新增检测样品',
                color: AppTheme.primaryColor,
                onTap: () => Get.toNamed('/sample/register'),
              ),
            ),
            SizedBox(width: 12.w),
            Expanded(
              child: _buildActionCard(
                icon: Icons.qr_code_scanner,
                title: '扫码查询',
                subtitle: '扫描条码获取信息',
                color: AppTheme.successColor,
                onTap: () {},
              ),
            ),
          ],
        ),
        SizedBox(height: 12.h),
        Row(
          children: [
            Expanded(
              child: _buildActionCard(
                icon: Icons.cloud_sync_outlined,
                title: '数据同步',
                subtitle: '同步离线数据',
                color: AppTheme.infoColor,
                onTap: () => Get.toNamed('/offline'),
              ),
            ),
            SizedBox(width: 12.w),
            Expanded(
              child: _buildActionCard(
                icon: Icons.picture_as_pdf_outlined,
                title: '报告查询',
                subtitle: '查看检测报告',
                color: AppTheme.accentColor,
                onTap: () {},
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildActionCard({
    required IconData icon,
    required String title,
    required String subtitle,
    required Color color,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: EdgeInsets.all(16.w),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(12.r),
          boxShadow: [
            BoxShadow(
              color: Colors.grey.withOpacity(0.08),
              blurRadius: 8,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              width: 40.r,
              height: 40.r,
              decoration: BoxDecoration(
                color: color.withOpacity(0.1),
                borderRadius: BorderRadius.circular(10.r),
              ),
              child: Icon(icon, color: color, size: 24.sp),
            ),
            SizedBox(height: 12.h),
            Text(
              title,
              style: TextStyle(
                fontSize: 15.sp,
                fontWeight: FontWeight.w600,
                color: AppTheme.textPrimary,
              ),
            ),
            SizedBox(height: 4.h),
            Text(
              subtitle,
              style: TextStyle(
                fontSize: 12.sp,
                color: AppTheme.textSecondary,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatistics() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '今日统计',
          style: TextStyle(
            fontSize: 18.sp,
            fontWeight: FontWeight.bold,
            color: AppTheme.textPrimary,
          ),
        ),
        SizedBox(height: 12.h),
        Container(
          padding: EdgeInsets.all(16.w),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(12.r),
            boxShadow: [
              BoxShadow(
                color: Colors.grey.withOpacity(0.08),
                blurRadius: 8,
                offset: const Offset(0, 2),
              ),
            ],
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _buildStatItem('待检任务', '0', AppTheme.warningColor),
              Container(width: 1.w, height: 40.h, color: Colors.grey[200]),
              _buildStatItem('检测中', '0', AppTheme.primaryColor),
              Container(width: 1.w, height: 40.h, color: Colors.grey[200]),
              _buildStatItem('已完成', '0', AppTheme.successColor),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildStatItem(String label, String value, Color color) {
    return Column(
      children: [
        Text(
          value,
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
            fontSize: 13.sp,
            color: AppTheme.textSecondary,
          ),
        ),
      ],
    );
  }

  Widget _buildSamplePage() {
    SampleBinding().dependencies();
    return const SampleView();
  }

  Widget _buildTaskPage() {
    TaskBinding().dependencies();
    return const TaskView();
  }

  Widget _buildProfilePage() {
    return Scaffold(
      appBar: AppBar(
        title: const Text('我的'),
        automaticallyImplyLeading: false,
      ),
      body: SingleChildScrollView(
        child: Column(
          children: [
            _buildUserHeader(),
            _buildMenuList(),
          ],
        ),
      ),
    );
  }

  Widget _buildUserHeader() {
    return Container(
      width: double.infinity,
      padding: EdgeInsets.symmetric(horizontal: 24.w, vertical: 32.h),
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          colors: [AppTheme.primaryColor, AppTheme.secondaryColor],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
      ),
      child: Row(
        children: [
          Container(
            width: 64.r,
            height: 64.r,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(32.r),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.1),
                  blurRadius: 10,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: Icon(
              Icons.person,
              color: AppTheme.primaryColor,
              size: 40.sp,
            ),
          ),
          SizedBox(width: 16.w),
          Expanded(
            child: Obx(() => Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      controller.userName.value.isNotEmpty ? controller.userName.value : '未登录',
                      style: TextStyle(
                        fontSize: 20.sp,
                        fontWeight: FontWeight.bold,
                        color: Colors.white,
                      ),
                    ),
                    SizedBox(height: 4.h),
                    Text(
                      controller.userType.value.isNotEmpty ? controller.userType.value : '欢迎使用',
                      style: TextStyle(
                        fontSize: 14.sp,
                        color: Colors.white.withOpacity(0.8),
                      ),
                    ),
                  ],
                )),
          ),
        ],
      ),
    );
  }

  Widget _buildMenuList() {
    return Container(
      padding: EdgeInsets.all(16.w),
      child: Column(
        children: [
          _buildMenuItem(
            icon: Icons.settings_outlined,
            title: '系统设置',
            onTap: () {},
          ),
          _buildMenuItem(
            icon: Icons.lock_outlined,
            title: '修改密码',
            onTap: () {},
          ),
          _buildMenuItem(
            icon: Icons.cloud_sync_outlined,
            title: '数据同步',
            onTap: () {},
          ),
          _buildMenuItem(
            icon: Icons.info_outline,
            title: '关于我们',
            onTap: () {},
          ),
          SizedBox(height: 24.h),
          _buildLogoutButton(),
        ],
      ),
    );
  }

  Widget _buildMenuItem({
    required IconData icon,
    required String title,
    required VoidCallback onTap,
  }) {
    return Container(
      margin: EdgeInsets.only(bottom: 8.h),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12.r),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: ListTile(
        leading: Icon(icon, color: AppTheme.primaryColor, size: 22.sp),
        title: Text(
          title,
          style: TextStyle(fontSize: 15.sp, color: AppTheme.textPrimary),
        ),
        trailing: Icon(Icons.chevron_right, color: AppTheme.textHint, size: 20.sp),
        onTap: onTap,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12.r),
        ),
      ),
    );
  }

  Widget _buildLogoutButton() {
    return SizedBox(
      width: double.infinity,
      height: 52.h,
      child: ElevatedButton.icon(
        onPressed: controller.logout,
        icon: Icon(Icons.logout, size: 20.sp),
        label: Text(
          '退出登录',
          style: TextStyle(fontSize: 16.sp, fontWeight: FontWeight.w500),
        ),
        style: ElevatedButton.styleFrom(
          backgroundColor: AppTheme.dangerColor,
          foregroundColor: Colors.white,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12.r),
          ),
        ),
      ),
    );
  }
}
