import request from '@/utils/request'

export function getSampleList(params) {
  return request({
    url: '/sample/list',
    method: 'get',
    params
  })
}

export function getSampleDetail(id) {
  return request({
    url: `/sample/${id}`,
    method: 'get'
  })
}

export function createSample(data) {
  return request({
    url: '/sample',
    method: 'post',
    data
  })
}

export function updateSample(data) {
  return request({
    url: '/sample',
    method: 'put',
    data
  })
}

export function deleteSample(id) {
  return request({
    url: `/sample/${id}`,
    method: 'delete'
  })
}

export function batchImportSample(data) {
  return request({
    url: '/sample/batchImport',
    method: 'post',
    data
  })
}
