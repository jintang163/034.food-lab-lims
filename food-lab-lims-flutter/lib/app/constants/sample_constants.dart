class SampleConstants {
  static const String statusPending = 'PENDING';
  static const String statusTesting = 'TESTING';
  static const String statusCompleted = 'COMPLETED';
  static const String statusRejected = 'REJECTED';

  static const String syncStatusPending = 'PENDING';
  static const String syncStatusSyncing = 'SYNCING';
  static const String syncStatusSynced = 'SYNCED';
  static const String syncStatusFailed = 'FAILED';

  static String getStatusText(String? status) {
    switch (status) {
      case statusPending:
        return '待检测';
      case statusTesting:
        return '检测中';
      case statusCompleted:
        return '已完成';
      case statusRejected:
        return '已驳回';
      default:
        return '未知';
    }
  }

  static String getSyncStatusText(String? status) {
    switch (status) {
      case syncStatusPending:
        return '待同步';
      case syncStatusSyncing:
        return '同步中';
      case syncStatusSynced:
        return '已同步';
      case syncStatusFailed:
        return '同步失败';
      default:
        return '未知';
    }
  }
}
