import 'package:json_annotation/json_annotation.dart';

part 'detect_result_model.g.dart';

@JsonSerializable()
class DetectResultModel {
  int? id;
  String? offlineId;
  int? taskId;
  int? sampleId;
  String? sampleCode;
  int? detectItemId;
  String? detectItemName;
  String? instrument;
  String? detectTime;
  String? resultType;
  double? resultValue;
  String? resultUnit;
  String? qualitativeResult;
  int? limitStandardId;
  String? calculateFormula;
  String? remark;
  String? attachFiles;
  List<RawDataModel>? rawDataList;
  String? syncStatus;
  String? createTime;
  String? deviceId;

  DetectResultModel({
    this.id,
    this.offlineId,
    this.taskId,
    this.sampleId,
    this.sampleCode,
    this.detectItemId,
    this.detectItemName,
    this.instrument,
    this.detectTime,
    this.resultType,
    this.resultValue,
    this.resultUnit,
    this.qualitativeResult,
    this.limitStandardId,
    this.calculateFormula,
    this.remark,
    this.attachFiles,
    this.rawDataList,
    this.syncStatus,
    this.createTime,
    this.deviceId,
  });

  factory DetectResultModel.fromJson(Map<String, dynamic> json) => _$DetectResultModelFromJson(json);

  Map<String, dynamic> toJson() => _$DetectResultModelToJson(this);

  Map<String, dynamic> toSubmitJson() {
    return {
      'taskId': taskId,
      'sampleId': sampleId,
      'sampleCode': sampleCode,
      'detectItemId': detectItemId,
      'instrument': instrument,
      'detectTime': detectTime,
      'resultType': resultType,
      'resultValue': resultValue,
      'resultUnit': resultUnit,
      'qualitativeResult': qualitativeResult,
      'limitStandardId': limitStandardId,
      'calculateFormula': calculateFormula,
      'remark': remark,
      'attachFiles': attachFiles,
      'rawDataList': rawDataList?.map((e) => e.toJson()).toList(),
    };
  }
}

@JsonSerializable()
class RawDataModel {
  String? dataKey;
  String? dataValue;
  String? dataType;
  int? sort;
  String? remark;

  RawDataModel({
    this.dataKey,
    this.dataValue,
    this.dataType,
    this.sort,
    this.remark,
  });

  factory RawDataModel.fromJson(Map<String, dynamic> json) => _$RawDataModelFromJson(json);

  Map<String, dynamic> toJson() => _$RawDataModelToJson(this);
}
