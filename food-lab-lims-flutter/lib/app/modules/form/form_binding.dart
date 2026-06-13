import 'package:get/get.dart';

import 'form_controller.dart';
import '../../services/form_service.dart';
import '../../services/form_sync_service.dart';

class FormBinding extends Bindings {
  @override
  void dependencies() {
    Get.lazyPut<FormService>(() => FormService(), fenix: true);
    Get.lazyPut<FormSyncService>(() => FormSyncService(), fenix: true);
    Get.lazyPut<FormController>(() => FormController(), fenix: true);
  }
}
