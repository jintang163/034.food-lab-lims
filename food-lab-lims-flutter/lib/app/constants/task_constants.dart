class TaskConstants {
  static const String pending = 'PENDING';
  static const String detecting = 'DETECTING';
  static const String firstAudit = 'FIRST_AUDIT';
  static const String secondAudit = 'SECOND_AUDIT';
  static const String approved = 'APPROVED';
  static const String rejected = 'REJECTED';

  static const List<String> allStatuses = [
    pending,
    detecting,
    firstAudit,
    secondAudit,
    approved,
    rejected,
  ];

  static String getStatusText(String? status) {
    switch (status) {
      case pending:
        return '待检';
      case detecting:
        return '检测中';
      case firstAudit:
        return '一审中';
      case secondAudit:
        return '二审中';
      case approved:
        return '已通过';
      case rejected:
        return '已驳回';
      default:
        return '未知';
    }
  }

  static bool isPending(String? status) => status == pending;
  static bool isDetecting(String? status) => status == detecting;
  static bool isInAudit(String? status) =>
      status == firstAudit || status == secondAudit;
  static bool isApproved(String? status) => status == approved;
  static bool isRejected(String? status) => status == rejected;
  static bool isCompleted(String? status) =>
      status == approved || status == rejected;
}
