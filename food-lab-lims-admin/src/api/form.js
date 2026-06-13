import request from '@/utils/request'

export function getTemplatePage(params) {
  return request({
    url: '/form/template/page',
    method: 'get',
    params
  })
}

export function getTemplate(id) {
  return request({
    url: `/form/template/${id}`,
    method: 'get'
  })
}

export function getCurrentTemplate(templateCode) {
  return request({
    url: `/form/template/current/${templateCode}`,
    method: 'get'
  })
}

export function getTemplateList(params) {
  return request({
    url: '/form/template/list',
    method: 'get',
    params
  })
}

export function addTemplate(data) {
  return request({
    url: '/form/template',
    method: 'post',
    data
  })
}

export function updateTemplate(data) {
  return request({
    url: '/form/template',
    method: 'put',
    data
  })
}

export function deleteTemplate(id) {
  return request({
    url: `/form/template/${id}`,
    method: 'delete'
  })
}

export function publishTemplate(id) {
  return request({
    url: `/form/template/${id}/publish`,
    method: 'post'
  })
}

export function createNewVersion(id, data) {
  return request({
    url: `/form/template/${id}/version`,
    method: 'post',
    data
  })
}

export function getVersionHistory(templateId) {
  return request({
    url: `/form/template/${templateId}/versions`,
    method: 'get'
  })
}

export function rollbackVersion(versionId) {
  return request({
    url: `/form/template/version/${versionId}/rollback`,
    method: 'post'
  })
}

export function validateSchema(data) {
  return request({
    url: '/form/template/validate',
    method: 'post',
    data
  })
}

export function getDataPage(params) {
  return request({
    url: '/form/data/page',
    method: 'get',
    params
  })
}

export function getData(id) {
  return request({
    url: `/form/data/${id}`,
    method: 'get'
  })
}

export function saveFormData(data) {
  return request({
    url: '/form/data',
    method: 'post',
    data
  })
}

export function submitFormData(data) {
  return request({
    url: '/form/data/submit',
    method: 'post',
    data
  })
}

export function syncData(data) {
  return request({
    url: '/form/data/sync',
    method: 'post',
    data
  })
}

export function startMigration(data) {
  return request({
    url: '/form/migration/start',
    method: 'post',
    data
  })
}

export function getMigrationStatus(migrationId) {
  return request({
    url: `/form/migration/${migrationId}/status`,
    method: 'get'
  })
}
