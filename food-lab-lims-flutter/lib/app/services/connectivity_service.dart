import 'dart:async';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:get/get.dart';
import 'package:logger/logger.dart';

class ConnectivityService extends GetxService {
  final Logger _logger = Logger();
  final Connectivity _connectivity = Connectivity();

  final RxBool isOnline = true.obs;
  final Rx<ConnectivityResult> connectivityType = ConnectivityResult.none.obs;

  late StreamSubscription<ConnectivityResult> _streamSubscription;

  @override
  void onInit() {
    super.onInit();
    _initConnectivity();
    _listenConnectivityChanges();
  }

  @override
  void onClose() {
    _streamSubscription.cancel();
    super.onClose();
  }

  Future<void> _initConnectivity() async {
    try {
      final result = await _connectivity.checkConnectivity();
      _updateConnectivityStatus(result);
    } catch (e) {
      _logger.e('检查网络连接失败: $e');
      isOnline.value = false;
    }
  }

  void _listenConnectivityChanges() {
    _streamSubscription = _connectivity.onConnectivityChanged.listen(
      (ConnectivityResult result) {
        _updateConnectivityStatus(result);
      },
      onError: (e) {
        _logger.e('监听网络连接变化失败: $e');
      },
    );
  }

  void _updateConnectivityStatus(ConnectivityResult result) {
    connectivityType.value = result;
    isOnline.value = result != ConnectivityResult.none;
    _logger.i('网络状态变化: $result, 在线: ${isOnline.value}');

    if (isOnline.value) {
      _triggerSyncIfNeeded();
    }
  }

  void _triggerSyncIfNeeded() {
    _logger.i('网络已恢复，准备同步离线数据...');
  }

  Future<bool> checkConnection() async {
    try {
      final result = await _connectivity.checkConnectivity();
      return result != ConnectivityResult.none;
    } catch (e) {
      _logger.e('检查网络连接失败: $e');
      return false;
    }
  }
}
