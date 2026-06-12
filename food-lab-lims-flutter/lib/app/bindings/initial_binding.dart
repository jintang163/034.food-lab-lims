import 'package:get/get.dart';

import '../services/dio_service.dart';
import '../services/database_service.dart';
import '../services/storage_service.dart';
import '../services/connectivity_service.dart';

class InitialBinding extends Bindings {
  @override
  Future<void> dependencies() async {
    final storageService = StorageService();
    await storageService.init();
    Get.put<StorageService>(storageService, permanent: true);

    final connectivityService = ConnectivityService();
    await connectivityService.onInit();
    Get.put<ConnectivityService>(connectivityService, permanent: true);

    Get.lazyPut<DioService>(() => DioService(), fenix: true);
    Get.lazyPut<DatabaseService>(() => DatabaseService(), fenix: true);
  }
}
