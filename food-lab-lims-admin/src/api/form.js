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

export function getCurrentTemplate(detectItemId) {
  return request({
    url: `/form/template/current/${detectItemId}`,
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

export function updateTemplate(id, data) {
  return request({
    url: `/form/template/${id}`,
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

export function publishTemplate(id, data) {
  return request({
    url: `/form/template/${id}/publish`,
    method: 'post',
    data
  })
}

export function createNewVersion(id, data) {
  return request({
    url: `/form/template/${id}/new-version`,
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

export function rollbackVersion(templateId, versionId) {
  return request({
    url: `/form/template/${templateId}/rollback/${versionId}`,
    method: 'post'
  })
}

export function validateSchema(formSchema) {
  return request({
    url: '/form/template/validate-schema',
    method: 'post',
    data: formSchema,
    headers: { 'Content-Type': 'application/json' }
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
    url: '/form/data/save',
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

export function getDataByTemplateVersion(templateId, version) {
  return request({
    url: `/form/data/template/${templateId}/version/${version}`,
    method: 'get'
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

export function getMigratableData(params) {
  return request({
    url: '/form/migration/migratable',
    method: 'get',
    params
  })
}
