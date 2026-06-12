import 'package:json_annotation/json_annotation.dart';

part 'user_model.g.dart';

@JsonSerializable()
class UserModel {
  int? id;
  String? username;
  String? realName;
  String? userType;
  String? email;
  String? phone;
  String? gender;
  String? avatar;
  int? deptId;
  String? deptName;
  String? token;

  UserModel({
    this.id,
    this.username,
    this.realName,
    this.userType,
    this.email,
    this.phone,
    this.gender,
    this.avatar,
    this.deptId,
    this.deptName,
    this.token,
  });

  factory UserModel.fromJson(Map<String, dynamic> json) => _$UserModelFromJson(json);

  Map<String, dynamic> toJson() => _$UserModelToJson(this);
}
