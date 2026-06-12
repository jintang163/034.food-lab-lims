import request from '@/utils/request'

export function getDetectItemList(params) {
  return request({
    url: '/detect/item/list',
    method: 'get',
    params
  })
}

export function getDetectItem(id) {
  return request({
    url: `/detect/item/${id}`,
    method: 'get'
  })
}

export function createDetectItem(data) {
  return request({
    url: '/detect/item',
    method: 'post',
    data
  })
}

export function updateDetectItem(data) {
  return request({
    url: '/detect/item',
    method: 'put',
    data
  })
}

export function deleteDetectItem(id) {
  return request({
    url: `/detect/item/${id}`,
    method: 'delete'
  })
}

export function getLimitStandardList(params) {
  return request({
    url: '/detect/standard/list',
    method: 'get',
    params
  })
}

export function createLimitStandard(data) {
  return request({
    url: '/detect/standard',
    method: 'post',
    data
  })
}

export function updateLimitStandard(data) {
  return request({
    url: '/detect/standard',
    method: 'put',
    data
  })
}

export function deleteLimitStandard(id) {
  return request({
    url: `/detect/standard/${id}`,
    method: 'delete'
  })
}
