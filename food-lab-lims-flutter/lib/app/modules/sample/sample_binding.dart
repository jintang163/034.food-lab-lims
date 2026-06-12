import 'package:get/get.dart';

import 'sample_controller.dart';

class SampleBinding extends Bindings {
  @override
  void dependencies() {
    Get.lazyPut<SampleController>(() => SampleController(), fenix: true);
  }
}
