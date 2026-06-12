import request from '@/utils/request'

export function getTaskList(params) {
  return request({
    url: '/task/list',
    method: 'get',
    params
  })
}

export function getTaskDetail(id) {
  return request({
    url: `/task/${id}`,
    method: 'get'
  })
}

export function assignTask(data) {
  return request({
    url: '/task/assign',
    method: 'post',
    data
  })
}

export function getTaskFlowLog(taskId) {
  return request({
    url: `/task/flowLog/${taskId}`,
    method: 'get'
  })
}
