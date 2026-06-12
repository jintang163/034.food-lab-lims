import 'package:json_annotation/json_annotation.dart';

part 'detect_item_model.g.dart';

@JsonSerializable()
class DetectItemModel {
  int? id;
  String? itemCode;
  String? itemName;
  int? categoryId;
  String? categoryName;
  String? detectMethod;
  String? detectStandard;
  String? unit;
  double? precisionValue;
  String? formSchema;
  String? status;
  List<LimitStandardModel>? limitStandards;

  DetectItemModel({
    this.id,
    this.itemCode,
    this.itemName,
    this.categoryId,
    this.categoryName,
    this.detectMethod,
    this.detectStandard,
    this.unit,
    this.precisionValue,
    this.formSchema,
    this.status,
    this.limitStandards,
  });

  factory DetectItemModel.fromJson(Map<String, dynamic> json) => _$DetectItemModelFromJson(json);

  Map<String, dynamic> toJson() => _$DetectItemModelToJson(this);
}

@JsonSerializable()
class LimitStandardModel {
  int? id;
  String? standardName;
  String? standardNo;
  String? limitType;
  double? limitValueMin;
  double? limitValueMax;
  String? limitUnit;
  String? qualitativeResult;
  String? description;

  LimitStandardModel({
    this.id,
    this.standardName,
    this.standardNo,
    this.limitType,
    this.limitValueMin,
    this.limitValueMax,
    this.limitUnit,
    this.qualitativeResult,
    this.description,
  });

  factory LimitStandardModel.fromJson(Map<String, dynamic> json) => _$LimitStandardModelFromJson(json);

  Map<String, dynamic> toJson() => _$LimitStandardModelToJson(this);
}
