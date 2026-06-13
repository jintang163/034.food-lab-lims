import request from '@/utils/request'

export function generateSchedule(data) {
  return request({
    url: '/api/schedule/generate',
    method: 'post',
    data
  })
}

export function getSchedulePage(params) {
  return request({
    url: '/api/schedule/page',
    method: 'get',
    params
  })
}

export function getScheduleList(params) {
  return request({
    url: '/api/schedule/list',
    method: 'get',
    params
  })
}

export function getGanttData(startDate, endDate, groupBy = 'instrument') {
  return request({
    url: '/api/schedule/gantt',
    method: 'get',
    params: { startDate, endDate, groupBy }
  })
}

export function getGanttResources(type = 'all') {
  return request({
    url: '/api/schedule/gantt/resources',
    method: 'get',
    params: { type }
  })
}

export function checkConflicts(startDate, endDate) {
  return request({
    url: '/api/schedule/conflicts',
    method: 'get',
    params: { startDate, endDate }
  })
}

export function adjustSchedule(data) {
  return request({
    url: '/api/schedule/adjust',
    method: 'post',
    data
  })
}

export function cancelSchedule(id, reason) {
  return request({
    url: `/api/schedule/cancel/${id}`,
    method: 'post',
    params: { reason }
  })
}

export function publishSchedule(batchNo) {
  return request({
    url: `/api/schedule/publish/${batchNo}`,
    method: 'post'
  })
}

export function getScheduleByBatchNo(batchNo) {
  return request({
    url: `/api/schedule/batch/${batchNo}`,
    method: 'get'
  })
}

export function getScheduleDetail(id) {
  return request({
    url: `/api/schedule/${id}`,
    method: 'get'
  })
}

export function getInstrumentPage(params) {
  return request({
    url: '/api/instrument/page',
    method: 'get',
    params
  })
}

export function getInstrumentById(id) {
  return request({
    url: `/api/instrument/${id}`,
    method: 'get'
  })
}

export function saveInstrument(data) {
  return request({
    url: '/api/instrument',
    method: 'post',
    data
  })
}

export function updateInstrument(data) {
  return request({
    url: '/api/instrument',
    method: 'put',
    data
  })
}

export function deleteInstrument(id) {
  return request({
    url: `/api/instrument/${id}`,
    method: 'delete'
  })
}

export function getInstrumentCalendar(id, startDate, endDate) {
  return request({
    url: `/api/instrument/${id}/calendar`,
    method: 'get',
    params: { startDate, endDate }
  })
}

export function addCalendarEvent(data) {
  return request({
    url: '/api/instrument/calendar',
    method: 'post',
    data
  })
}

export function updateCalendarEvent(data) {
  return request({
    url: '/api/instrument/calendar',
    method: 'put',
    data
  })
}

export function deleteCalendarEvent(id) {
  return request({
    url: `/api/instrument/calendar/${id}`,
    method: 'delete'
  })
}

export function getStaffLeavePage(params) {
  return request({
    url: '/api/staff-leave/page',
    method: 'get',
    params
  })
}

export function applyStaffLeave(data) {
  return request({
    url: '/api/staff-leave/apply',
    method: 'post',
    data
  })
}

export function approveStaffLeave(id, remark) {
  return request({
    url: `/api/staff-leave/approve/${id}`,
    method: 'post',
    params: { remark }
  })
}

export function rejectStaffLeave(id, remark) {
  return request({
    url: `/api/staff-leave/reject/${id}`,
    method: 'post',
    params: { remark }
  })
}

export function cancelStaffLeave(id) {
  return request({
    url: `/api/staff-leave/cancel/${id}`,
    method: 'post'
  })
}
