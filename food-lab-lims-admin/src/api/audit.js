import request from '@/utils/request'

export function getAuditPendingList(params) {
  return request({
    url: '/audit/pending',
    method: 'get',
    params
  })
}

export function getAuditHistoryList(params) {
  return request({
    url: '/audit/history',
    method: 'get',
    params
  })
}

export function auditApprove(data) {
  return request({
    url: '/audit/approve',
    method: 'post',
    data
  })
}

export function auditReject(data) {
  return request({
    url: '/audit/reject',
    method: 'post',
    data
  })
}

export function getAuditRecordList(businessId) {
  return request({
    url: `/audit/record/${businessId}`,
    method: 'get'
  })
}

export function getAuditFlow(processInstanceId) {
  return request({
    url: `/audit/flow/${processInstanceId}`,
    method: 'get'
  })
}

export function rejectToSubmitter(flowTaskId, reason) {
  return request({
    url: '/audit/reject-to-submitter',
    method: 'post',
    params: { flowTaskId, reason }
  })
}

export function triggerRetest(flowTaskId, retesterId, reason) {
  return request({
    url: '/audit/trigger-retest',
    method: 'post',
    params: { flowTaskId, retesterId, reason }
  })
}

export function startRetest(data) {
  return request({
    url: '/audit/retest/start',
    method: 'post',
    data
  })
}

export function submitRetestResult(data) {
  return request({
    url: '/audit/retest/submit-result',
    method: 'post',
    data
  })
}

export function compareRetestResult(retestId) {
  return request({
    url: `/audit/retest/compare/${retestId}`,
    method: 'get'
  })
}

export function adoptRetestResult(data) {
  return request({
    url: '/audit/retest/adopt',
    method: 'post',
    data
  })
}

export function getRetestByTaskId(taskId) {
  return request({
    url: `/audit/retest/task/${taskId}`,
    method: 'get'
  })
}

export function getRetestDetail(retestId) {
  return request({
    url: `/audit/retest/${retestId}`,
    method: 'get'
  })
}

export function createSamplingReview(data) {
  return request({
    url: '/audit/sampling-review/create',
    method: 'post',
    data
  })
}

export function getPendingSamplingReviews() {
  return request({
    url: '/audit/sampling-review/pending',
    method: 'get'
  })
}

export function completeSamplingReview(reviewId, result, opinion) {
  return request({
    url: '/audit/sampling-review/complete',
    method: 'post',
    params: { reviewId, result, opinion }
  })
}

export function getProcessHistory(processInstanceId) {
  return request({
    url: `/audit/process/history/${processInstanceId}`,
    method: 'get'
  })
}

export function getMyAuditTasks() {
  return request({
    url: '/audit/my-tasks',
    method: 'get'
  })
}
