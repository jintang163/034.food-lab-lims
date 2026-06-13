import 'package:get/get.dart';

import '../modules/sample/sample_binding.dart';
import '../modules/sample/sample_view.dart';
import '../modules/task/task_binding.dart';
import '../modules/task/task_view.dart';
import '../modules/detect/detect_binding.dart';
import '../modules/detect/detect_view.dart';
import '../modules/home/home_binding.dart';
import '../modules/home/home_view.dart';
import '../modules/login/login_binding.dart';
import '../modules/login/login_view.dart';
import '../modules/offline/offline_binding.dart';
import '../modules/offline/offline_view.dart';
import '../modules/form/form_binding.dart';
import '../modules/form/form_view.dart';

class AppPages {
  static const initial = Routes.login;

  static final routes = [
    GetPage(
      name: Routes.login,
      page: () => const LoginView(),
      binding: LoginBinding(),
    ),
    GetPage(
      name: Routes.home,
      page: () => const HomeView(),
      binding: HomeBinding(),
    ),
    GetPage(
      name: Routes.sample,
      page: () => const SampleView(),
      binding: SampleBinding(),
    ),
    GetPage(
      name: Routes.sampleRegister,
      page: () => const SampleRegisterView(),
      binding: SampleBinding(),
    ),
    GetPage(
      name: Routes.task,
      page: () => const TaskView(),
      binding: TaskBinding(),
    ),
    GetPage(
      name: Routes.detect,
      page: () => const DetectView(),
      binding: DetectBinding(),
    ),
    GetPage(
      name: Routes.offline,
      page: () => const OfflineView(),
      binding: OfflineBinding(),
    ),
    GetPage(
      name: Routes.form,
      page: () => const FormView(),
      binding: FormBinding(),
    ),
  ];
}

class Routes {
  static const login = '/login';
  static const home = '/home';
  static const sample = '/sample';
  static const sampleRegister = '/sample/register';
  static const task = '/task';
  static const detect = '/detect';
  static const offline = '/offline';
  static const form = '/form';
}
