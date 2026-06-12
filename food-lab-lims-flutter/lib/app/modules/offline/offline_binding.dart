import 'package:get/get.dart';

import 'offline_controller.dart';

class OfflineBinding extends Bindings {
  @override
  void dependencies() {
    Get.lazyPut<OfflineController>(() => OfflineController(), fenix: true);
  }
}
