import 'package:get/get.dart';

import 'detect_controller.dart';

class DetectBinding extends Bindings {
  @override
  void dependencies() {
    Get.lazyPut<DetectController>(() => DetectController(), fenix: true);
  }
}
