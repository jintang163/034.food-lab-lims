import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import 'package:logger/logger.dart';

import '../models/sample_model.dart';
import '../models/detect_result_model.dart';

class DatabaseService {
  static Database? _database;
  final Logger _logger = Logger();

  String _camelToSnake(String camel) {
    final buffer = StringBuffer();
    for (var i = 0; i < camel.length; i++) {
      final char = camel[i];
      if (char.toUpperCase() == char && i > 0) {
        buffer.write('_');
      }
      buffer.write(char.toLowerCase());
    }
    return buffer.toString();
  }

  Map<String, dynamic> _toDbMap(Map<String, dynamic> json) {
    final Map<String, dynamic> dbMap = {};
    json.forEach((key, value) {
      final snakeKey = _camelToSnake(key);
      if (value is List) {
        dbMap[snakeKey] = value.join(',');
      } else {
        dbMap[snakeKey] = value;
      }
    });
    return dbMap;
  }

  Map<String, dynamic> _fromDbMap(Map<String, dynamic> dbMap) {
    final Map<String, dynamic> json = {};
    dbMap.forEach((key, value) {
      final camelKey = _snakeToCamel(key);
      if (key == 'detect_item_ids' && value is String) {
        json[camelKey] = value.split(',').map((e) => int.tryParse(e)).where((e) => e != null).cast<int>().toList();
      } else {
        json[camelKey] = value;
      }
    });
    return json;
  }

  String _snakeToCamel(String snake) {
    final parts = snake.split('_');
    if (parts.length == 1) return snake;
    return parts.first + parts.sublist(1).map((e) => e[0].toUpperCase() + e.substring(1)).join();
  }

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  Future<Database> _initDatabase() async {
    String path = join(await getDatabasesPath(), 'food_lab_lims.db');
    _logger.i('数据库路径: $path');

    return await openDatabase(
      path,
      version: 1,
      onCreate: _onCreate,
    );
  }

  Future<void> _onCreate(Database db, int version) async {
    _logger.i('创建数据库表...');

    await db.execute('''
      CREATE TABLE IF NOT EXISTS samples (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        offline_id TEXT,
        sample_name TEXT,
        batch_no TEXT,
        manufacturer TEXT,
        production_date TEXT,
        shelf_life TEXT,
        sample_location TEXT,
        sample_method TEXT,
        sample_person TEXT,
        sample_amount TEXT,
        sample_unit TEXT,
        remark TEXT,
        detect_item_ids TEXT,
        sync_status TEXT DEFAULT 'pending',
        create_time TEXT,
        device_id TEXT
      )
    ''');

    await db.execute('''
      CREATE TABLE IF NOT EXISTS detect_results (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        offline_id TEXT,
        task_id INTEGER,
        sample_id INTEGER,
        sample_code TEXT,
        detect_item_id INTEGER,
        instrument TEXT,
        detect_time TEXT,
        result_type TEXT,
        result_value REAL,
        result_unit TEXT,
        qualitative_result TEXT,
        limit_standard_id INTEGER,
        calculate_formula TEXT,
        remark TEXT,
        attach_files TEXT,
        raw_data TEXT,
        sync_status TEXT DEFAULT 'pending',
        create_time TEXT,
        device_id TEXT
      )
    ''');

    await db.execute('''
      CREATE TABLE IF NOT EXISTS sync_records (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        sync_batch_no TEXT,
        sync_type TEXT,
        data_type TEXT,
        total_count INTEGER DEFAULT 0,
        success_count INTEGER DEFAULT 0,
        fail_count INTEGER DEFAULT 0,
        sync_status TEXT DEFAULT 'processing',
        sync_start_time TEXT,
        sync_end_time TEXT,
        fail_details TEXT
      )
    ''');
  }

  Future<int> insertSample(SampleModel sample) async {
    final db = await database;
    return await db.insert('samples', _toDbMap(sample.toJson()));
  }

  Future<List<SampleModel>> getPendingSamples() async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query(
      'samples',
      where: 'sync_status = ?',
      whereArgs: ['pending'],
    );
    return List.generate(maps.length, (i) => SampleModel.fromJson(_fromDbMap(maps[i])));
  }

  Future<List<SampleModel>> getAllSamples() async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query('samples', orderBy: 'create_time DESC');
    return List.generate(maps.length, (i) => SampleModel.fromJson(_fromDbMap(maps[i])));
  }

  Future<int> updateSampleSyncStatus(int id, String status) async {
    final db = await database;
    return await db.update(
      'samples',
      {'sync_status': status},
      where: 'id = ?',
      whereArgs: [id],
    );
  }

  Future<int> deleteSample(int id) async {
    final db = await database;
    return await db.delete('samples', where: 'id = ?', whereArgs: [id]);
  }

  Future<int> insertDetectResult(DetectResultModel result) async {
    final db = await database;
    return await db.insert('detect_results', _toDbMap(result.toJson()));
  }

  Future<List<DetectResultModel>> getPendingDetectResults() async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query(
      'detect_results',
      where: 'sync_status = ?',
      whereArgs: ['pending'],
    );
    return List.generate(maps.length, (i) => DetectResultModel.fromJson(_fromDbMap(maps[i])));
  }

  Future<int> updateDetectResultSyncStatus(int id, String status) async {
    final db = await database;
    return await db.update(
      'detect_results',
      {'sync_status': status},
      where: 'id = ?',
      whereArgs: [id],
    );
  }

  Future<void> clearAllData() async {
    final db = await database;
    await db.delete('samples');
    await db.delete('detect_results');
    _logger.i('已清空所有本地数据');
  }

  Future close() async {
    final db = await database;
    await db.close();
    _database = null;
  }
}
