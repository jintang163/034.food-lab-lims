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
