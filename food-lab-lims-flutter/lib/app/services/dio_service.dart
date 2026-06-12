import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:logger/logger.dart';
import 'package:get/get.dart' as getx;

import '../services/storage_service.dart';
import '../config/api_config.dart';

class DioService {
  late Dio _dio;
  final Logger _logger = Logger();

  DioService() {
    _initDio();
  }

  Dio get dio => _dio;

  void _initDio() {
    BaseOptions options = BaseOptions(
      baseUrl: ApiConfig.baseUrl,
      connectTimeout: const Duration(seconds: 30),
      receiveTimeout: const Duration(seconds: 30),
      headers: {
        'Content-Type': 'application/json',
      },
    );

    _dio = Dio(options);

    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        final storage = getx.Get.find<StorageService>();
        final token = storage.getToken();
        final userId = storage.getUserId();
        final userName = storage.getUserName();

        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        if (userId != null) {
          options.headers['userId'] = userId.toString();
        }
        if (userName != null) {
          options.headers['userName'] = userName;
        }

        if (kDebugMode) {
          _logger.i('请求: ${options.method} ${options.uri}');
          _logger.i('请求头: ${options.headers}');
          _logger.i('请求参数: ${options.data}');
        }

        return handler.next(options);
      },
      onResponse: (response, handler) {
        if (kDebugMode) {
          _logger.i('响应: ${response.statusCode} ${response.requestOptions.uri}');
          _logger.i('响应数据: ${response.data}');
        }
        return handler.next(response);
      },
      onError: (error, handler) {
        if (kDebugMode) {
          _logger.e('请求错误: ${error.message}');
          if (error.response != null) {
            _logger.e('错误响应: ${error.response?.data}');
          }
        }

        if (error.response?.statusCode == 401) {
          final storage = getx.Get.find<StorageService>();
          storage.clearUserInfo();
          getx.Get.offAllNamed('/login');
        }

        return handler.next(error);
      },
    ));
  }

  Future<Response> get(String path, {Map<String, dynamic>? params, Options? options}) async {
    try {
      return await _dio.get(path, queryParameters: params, options: options);
    } catch (e) {
      rethrow;
    }
  }

  Future<Response> post(String path, {dynamic data, Map<String, dynamic>? params, Options? options}) async {
    try {
      return await _dio.post(path, data: data, queryParameters: params, options: options);
    } catch (e) {
      rethrow;
    }
  }

  Future<Response> put(String path, {dynamic data, Map<String, dynamic>? params, Options? options}) async {
    try {
      return await _dio.put(path, data: data, queryParameters: params, options: options);
    } catch (e) {
      rethrow;
    }
  }

  Future<Response> delete(String path, {Map<String, dynamic>? params, Options? options}) async {
    try {
      return await _dio.delete(path, queryParameters: params, options: options);
    } catch (e) {
      rethrow;
    }
  }

  Future<Response> upload(String path, FormData data) async {
    try {
      return await _dio.post(
        path,
        data: data,
        options: Options(
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        ),
      );
    } catch (e) {
      rethrow;
    }
  }
}
