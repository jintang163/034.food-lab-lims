import request from '@/utils/request'

export function getDashboardStats() {
  return request({
    url: '/dashboard/stats',
    method: 'get'
  })
}

export function getDashboardChart(params) {
  return request({
    url: '/dashboard/chart',
    method: 'get',
    params
  })
}
