import 'package:shared_preferences/shared_preferences.dart';

class StorageService {
  static const String _keyToken = 'token';
  static const String _keyUserId = 'user_id';
  static const String _keyUserName = 'user_name';
  static const String _keyUserType = 'user_type';
  static const String _keyDeviceId = 'device_id';
  static const String _keyRememberMe = 'remember_me';
  static const String _keyUsername = 'username';

  late SharedPreferences _prefs;

  Future<void> init() async {
    _prefs = await SharedPreferences.getInstance();
  }

  Future<void> setToken(String token) async {
    await _prefs.setString(_keyToken, token);
  }

  String? getToken() {
    return _prefs.getString(_keyToken);
  }

  Future<void> setUserId(int userId) async {
    await _prefs.setInt(_keyUserId, userId);
  }

  int? getUserId() {
    return _prefs.getInt(_keyUserId);
  }

  Future<void> setUserName(String userName) async {
    await _prefs.setString(_keyUserName, userName);
  }

  String? getUserName() {
    return _prefs.getString(_keyUserName);
  }

  Future<void> setUserType(String userType) async {
    await _prefs.setString(_keyUserType, userType);
  }

  String? getUserType() {
    return _prefs.getString(_keyUserType);
  }

  Future<void> setDeviceId(String deviceId) async {
    await _prefs.setString(_keyDeviceId, deviceId);
  }

  String? getDeviceId() {
    return _prefs.getString(_keyDeviceId);
  }

  Future<void> setRememberMe(bool value) async {
    await _prefs.setBool(_keyRememberMe, value);
  }

  bool? getRememberMe() {
    return _prefs.getBool(_keyRememberMe);
  }

  Future<void> setUsername(String username) async {
    await _prefs.setString(_keyUsername, username);
  }

  String? getUsername() {
    return _prefs.getString(_keyUsername);
  }

  Future<void> saveUserInfo({
    required String token,
    required int userId,
    required String userName,
    required String userType,
  }) async {
    await setToken(token);
    await setUserId(userId);
    await setUserName(userName);
    await setUserType(userType);
  }

  Future<void> clearUserInfo() async {
    await _prefs.remove(_keyToken);
    await _prefs.remove(_keyUserId);
    await _prefs.remove(_keyUserName);
    await _prefs.remove(_keyUserType);
  }

  Future<void> clearAll() async {
    await _prefs.clear();
  }
}
