import 'package:json_annotation/json_annotation.dart';

part 'sample_model.g.dart';

@JsonSerializable()
class SampleModel {
  int? id;
  String? offlineId;
  String? sampleCode;
  String? sampleName;
  String? batchNo;
  String? manufacturer;
  String? productionDate;
  String? shelfLife;
  String? sampleLocation;
  String? sampleMethod;
  String? samplePerson;
  String? sampleAmount;
  String? sampleUnit;
  String? sampleStatus;
  String? syncStatus;
  String? remark;
  String? barCode;
  String? qrCode;
  List<int>? detectItemIds;
  String? createTime;
  String? deviceId;

  SampleModel({
    this.id,
    this.offlineId,
    this.sampleCode,
    this.sampleName,
    this.batchNo,
    this.manufacturer,
    this.productionDate,
    this.shelfLife,
    this.sampleLocation,
    this.sampleMethod,
    this.samplePerson,
    this.sampleAmount,
    this.sampleUnit,
    this.sampleStatus,
    this.syncStatus,
    this.remark,
    this.barCode,
    this.qrCode,
    this.detectItemIds,
    this.createTime,
    this.deviceId,
  });

  factory SampleModel.fromJson(Map<String, dynamic> json) => _$SampleModelFromJson(json);

  Map<String, dynamic> toJson() => _$SampleModelToJson(this);

  Map<String, dynamic> toRegisterJson() {
    return {
      'sampleName': sampleName,
      'batchNo': batchNo,
      'manufacturer': manufacturer,
      'productionDate': productionDate,
      'shelfLife': shelfLife,
      'sampleLocation': sampleLocation,
      'sampleMethod': sampleMethod,
      'samplePerson': samplePerson,
      'sampleAmount': sampleAmount,
      'sampleUnit': sampleUnit,
      'remark': remark,
      'detectItemIds': detectItemIds,
      'offlineId': offlineId,
      'deviceId': deviceId,
    };
  }

  Map<String, dynamic> toSyncJson() {
    return {
      'id': id,
      'offlineId': offlineId,
      'sampleCode': sampleCode,
      'sampleName': sampleName,
      'batchNo': batchNo,
      'manufacturer': manufacturer,
      'productionDate': productionDate,
      'shelfLife': shelfLife,
      'sampleLocation': sampleLocation,
      'sampleMethod': sampleMethod,
      'samplePerson': samplePerson,
      'sampleAmount': sampleAmount,
      'sampleUnit': sampleUnit,
      'sampleStatus': sampleStatus,
      'remark': remark,
      'barCode': barCode,
      'qrCode': qrCode,
      'detectItemIds': detectItemIds,
      'deviceId': deviceId,
      'createTime': createTime,
    };
  }
}
