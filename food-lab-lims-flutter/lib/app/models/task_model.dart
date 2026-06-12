import 'package:json_annotation/json_annotation.dart';

part 'task_model.g.dart';

@JsonSerializable()
class TaskModel {
  int? id;
  String? taskCode;
  int? sampleId;
  String? sampleCode;
  String? taskName;
  String? taskType;
  String? priority;
  String? taskStatus;
  int? assignBy;
  String? assignByName;
  String? assignTime;
  int? detectPersonId;
  String? detectPersonName;
  String? startTime;
  String? endTime;
  String? deadline;
  String? remark;
  int? detectItemCount;
  int? completedItemCount;
  String? createTime;

  TaskModel({
    this.id,
    this.taskCode,
    this.sampleId,
    this.sampleCode,
    this.taskName,
    this.taskType,
    this.priority,
    this.taskStatus,
    this.assignBy,
    this.assignByName,
    this.assignTime,
    this.detectPersonId,
    this.detectPersonName,
    this.startTime,
    this.endTime,
    this.deadline,
    this.remark,
    this.detectItemCount,
    this.completedItemCount,
    this.createTime,
  });

  factory TaskModel.fromJson(Map<String, dynamic> json) => _$TaskModelFromJson(json);

  Map<String, dynamic> toJson() => _$TaskModelToJson(this);
}
