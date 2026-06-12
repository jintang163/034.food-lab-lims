import request from '@/utils/request'

export function getReportList(params) {
  return request({
    url: '/report/list',
    method: 'get',
    params
  })
}

export function getReportDetail(id) {
  return request({
    url: `/report/${id}`,
    method: 'get'
  })
}

export function generateReport(data) {
  return request({
    url: '/report/generate',
    method: 'post',
    data
  })
}

export function previewReport(id) {
  return request({
    url: `/report/preview/${id}`,
    method: 'get'
  })
}

export function exportReport(id) {
  return request({
    url: `/report/export/${id}`,
    method: 'get',
    responseType: 'blob'
  })
}
