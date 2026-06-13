class ApiConfig {
  static const String baseUrl = 'http://192.168.1.100:8080/api';

  static const String login = '/auth/login';
  static const String logout = '/auth/logout';

  static const String sampleList = '/sample/page';
  static const String sampleDetail = '/sample';
  static const String sampleRegister = '/sample/register';
  static const String sampleUpdate = '/sample';
  static const String sampleDelete = '/sample';
  static const String sampleSync = '/sample/sync';
  static const String sampleBarcode = '/sample/barcode';
  static const String sampleQrcode = '/sample/qrcode';
  static const String sampleExport = '/sample/export';
  static const String sampleImport = '/sample/import';

  static const String taskList = '/task/page';
  static const String taskDetail = '/task';
  static const String taskAssign = '/task/assign';
  static const String taskStart = '/task/start';
  static const String taskComplete = '/task/complete';
  static const String taskSubmit = '/task/submit';
  static const String taskMyTasks = '/task/my-tasks';

  static const String detectItemList = '/detect/item/all';
  static const String detectItemFormSchema = '/detect/item/form-schema';
  static const String detectResultSubmit = '/detect/result/submit';
  static const String detectResultBatchSubmit = '/detect/result/batch-submit';
  static const String detectResultList = '/detect/result/task';
  static const String detectResultSync = '/detect/result/sync';
  static const String detectResultAutoJudge = '/detect/result/auto-judge';

  static const String auditSubmit = '/audit/submit';
  static const String auditStart = '/audit/start';
  static const String auditHistory = '/audit/history';
  static const String auditMyPending = '/audit/my-pending';

  static const String reportGenerate = '/report/generate';
  static const String reportDetail = '/report';
  static const String reportPreview = '/report/preview';
  static const String reportExport = '/report/export';
  static const String reportIssue = '/report/issue';

  static const String formTemplateList = '/form/template/page';
  static const String formTemplateDetail = '/form/template';
  static const String formTemplateByCode = '/form/template/code';
  static const String formTemplateSchema = '/form/template/schema';

  static const String formDataSave = '/form/data/save';
  static const String formDataSubmit = '/form/data/submit';
  static const String formDataDetail = '/form/data';
  static const String formDataList = '/form/data/page';
  static const String formDataSync = '/form/data/sync';
  static const String formDataBatchSync = '/form/data/batch-sync';

  static const String formDraftSave = '/form/draft/save';
  static const String formDraftGet = '/form/draft';
  static const String formDraftDelete = '/form/draft';
  static const String formDraftList = '/form/draft/page';

  static const String formFileUpload = '/form/file/upload';
  static const String formFileDownload = '/form/file/download';
  static const String formFileDelete = '/form/file';
}
