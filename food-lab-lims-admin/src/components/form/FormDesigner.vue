<template>
  <div class="form-designer">
    <div class="designer-toolbar">
      <div class="toolbar-left">
        <el-button @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <el-divider direction="vertical" />
        <span class="template-name">{{ templateInfo.name || '未命名表单' }}</span>
        <el-tag v-if="templateInfo.status" :type="getStatusType(templateInfo.status)" size="small">
          {{ getStatusText(templateInfo.status) }}
        </el-tag>
      </div>
      <div class="toolbar-right">
        <el-button @click="handleImport">
          <el-icon><Upload /></el-icon>
          导入Schema
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出Schema
        </el-button>
        <el-button @click="handlePreview">
          <el-icon><View /></el-icon>
          预览
        </el-button>
        <el-button @click="handleValidate">
          <el-icon><CircleCheck /></el-icon>
          校验
        </el-button>
        <el-button type="primary" @click="handleSave">
          <el-icon><Save /></el-icon>
          保存
        </el-button>
        <el-button type="success" @click="handlePublish" :disabled="templateInfo.status === 'published'">
          <el-icon><Promotion /></el-icon>
          发布
        </el-button>
        <el-button type="warning" @click="handleNewVersion">
          <el-icon><DocumentAdd /></el-icon>
          新建版本
        </el-button>
      </div>
    </div>
    <div class="designer-content">
      <FormFieldPanel />
      <FormDesignerCanvas
        :fields="fields"
        :selectedField="selectedField"
        :templateInfo="templateInfo"
        @update:fields="handleFieldsUpdate"
        @update:selectedField="handleSelectField"
        @preview="handlePreview"
      />
      <FormPropertyPanel
        :selectedField="selectedField"
        :templateInfo="templateInfo"
        @update:selectedField="handleUpdateField"
        @update:templateInfo="handleTemplateInfoUpdate"
      />
    </div>

    <el-dialog v-model="previewVisible" title="表单预览" width="900px" destroy-on-close>
      <FormPreview
        :schema="{ fields }"
        :templateInfo="templateInfo"
        @submit="handlePreviewSubmit"
      />
    </el-dialog>

    <el-dialog v-model="schemaVisible" title="JSON Schema" width="800px" destroy-on-close>
      <el-tabs v-model="schemaTab">
        <el-tab-pane label="表单Schema" name="schema">
          <el-input
            v-model="schemaJson"
            type="textarea"
            :rows="20"
            :readonly="schemaTab === 'schema'"
            placeholder="表单Schema JSON"
          />
        </el-tab-pane>
        <el-tab-pane label="导入Schema" name="import">
          <el-input
            v-model="importJson"
            type="textarea"
            :rows="20"
            placeholder="粘贴JSON Schema进行导入"
          />
          <div style="margin-top: 16px; text-align: right;">
            <el-button @click="handleImportSchema">
              <el-icon><Upload /></el-icon>
              导入
            </el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <el-dialog v-model="versionVisible" title="新建版本" width="500px" destroy-on-close>
      <el-form :model="versionForm" :rules="versionRules" ref="versionFormRef" label-width="100px">
        <el-form-item label="版本号" prop="version">
          <el-input v-model="versionForm.version" placeholder="如: 1.0.1" />
        </el-form-item>
        <el-form-item label="版本说明" prop="description">
          <el-input
            v-model="versionForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入版本变更说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="versionVisible = false">取消</el-button>
        <el-button type="primary" @click="handleVersionSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Upload, Download, View, CircleCheck,
  Save, Promotion, DocumentAdd
} from '@element-plus/icons-vue'
import FormFieldPanel from './FormFieldPanel.vue'
import FormDesignerCanvas from './FormDesignerCanvas.vue'
import FormPropertyPanel from './FormPropertyPanel.vue'
import FormPreview from './FormPreview.vue'
import {
  getTemplate, addTemplate, updateTemplate,
  publishTemplate, createNewVersion, validateSchema
} from '@/api/form'

const props = defineProps({
  templateId: {
    type: [String, Number],
    default: null
  }
})

const emit = defineEmits(['save', 'publish', 'new-version'])

const router = useRouter()

const fields = ref([])
const selectedField = ref(null)
const previewVisible = ref(false)
const schemaVisible = ref(false)
const schemaTab = ref('schema')
const versionVisible = ref(false)
const versionFormRef = ref(null)

const templateInfo = reactive({
  id: null,
  name: '',
  description: '',
  status: 'draft',
  version: '1.0.0',
  labelWidth: '100px',
  labelPosition: 'right',
  showSubmit: true,
  showReset: true,
  submitText: '提交',
  resetText: '重置'
})

const versionForm = reactive({
  version: '',
  description: ''
})

const versionRules = {
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }]
}

const importJson = ref('')

const schemaJson = computed(() => {
  const properties = {}
  const requiredKeys = []
  fields.value.forEach(f => {
    const { id, icon, component, ...rest } = f
    properties[f.key] = { ...rest }
    delete properties[f.key].key
    if (f.required) {
      requiredKeys.push(f.key)
    }
  })
  const schema = {
    type: 'object',
    title: templateInfo.name || '',
    description: templateInfo.description || '',
    properties,
    required: requiredKeys
  }
  return JSON.stringify(schema, null, 2)
})

const getStatusType = (status) => {
  const map = {
    draft: 'info',
    published: 'success',
    disabled: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    draft: '草稿',
    published: '已发布',
    disabled: '已停用'
  }
  return map[status] || status
}

const handleFieldsUpdate = (newFields) => {
  fields.value = newFields
}

const handleSelectField = (field) => {
  selectedField.value = field
}

const handleUpdateField = (field) => {
  if (field) {
    const index = fields.value.findIndex(f => f.id === field.id)
    if (index > -1) {
      fields.value[index] = field
      selectedField.value = field
    }
  } else {
    selectedField.value = null
  }
}

const handleTemplateInfoUpdate = (info) => {
  Object.assign(templateInfo, info)
}

const handleBack = () => {
  router.push('/form/template/list')
}

const buildFormSchema = () => {
  const properties = {}
  const requiredKeys = []
  fields.value.forEach(f => {
    const fieldDef = {}
    fieldDef.type = f.type === 'number' ? 'number' : 'string'
    if (f.type === 'number') fieldDef.type = 'number'
    if (f.type === 'date' || f.type === 'datetime') fieldDef.format = f.type
    if (f.type === 'textarea') fieldDef.format = 'textarea'
    if (f.type === 'file') fieldDef.format = 'file'
    fieldDef.label = f.label || ''
    if (f.placeholder) fieldDef.placeholder = f.placeholder
    if (f.description) fieldDef.description = f.description
    if (f.unit) fieldDef.unit = f.unit
    if (f.required) fieldDef.required = true
    if (f.readOnly) fieldDef.readOnly = true
    if (f.hidden) fieldDef.hidden = true
    if (f.maxLines) fieldDef.maxLines = f.maxLines
    if (f.maxLength) fieldDef.maxLength = f.maxLength
    if (f.precision !== undefined && f.precision !== null) fieldDef.precision = f.precision
    if (f.min !== undefined && f.min !== null) fieldDef.min = f.min
    if (f.max !== undefined && f.max !== null) fieldDef.max = f.max
    if (f.resultType) { fieldDef.isResultField = true; fieldDef.resultType = f.resultType }
    if (f.defaultValue !== undefined && f.defaultValue !== null) fieldDef.defaultValue = f.defaultValue
    if (f.options && f.options.length > 0) fieldDef.options = f.options
    if (f.allowedFileTypes && f.allowedFileTypes.length > 0) fieldDef.accept = f.allowedFileTypes
    if (f.multiple) fieldDef.multiple = true
    if (f.widgetConfig) fieldDef.widgetConfig = f.widgetConfig
    if (f.sortOrder !== undefined) fieldDef.sortOrder = f.sortOrder
    properties[f.key] = fieldDef
    if (f.required) requiredKeys.push(f.key)
  })
  return JSON.stringify({
    type: 'object',
    title: templateInfo.name || '',
    description: templateInfo.description || '',
    properties,
    required: requiredKeys
  })
}

const handleSave = async () => {
  const formSchema = buildFormSchema()
  try {
    if (templateInfo.id) {
      await updateTemplate(templateInfo.id, {
        templateName: templateInfo.name,
        description: templateInfo.description,
        formSchema,
        remark: templateInfo.remark
      })
      ElMessage.success('保存成功')
    } else {
      const res = await addTemplate({
        templateName: templateInfo.name,
        detectItemId: templateInfo.detectItemId,
        detectItemName: templateInfo.detectItemName,
        description: templateInfo.description,
        formSchema,
        remark: templateInfo.remark
      })
      templateInfo.id = res?.id || Date.now()
      ElMessage.success('创建成功')
    }
    emit('save', formSchema)
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const handlePublish = async () => {
  if (!templateInfo.id) {
    ElMessage.warning('请先保存模板')
    return
  }
  try {
    await publishTemplate(templateInfo.id, { changeSummary: '发布版本 ' + (templateInfo.version || '1') })
    templateInfo.status = 'published'
    ElMessage.success('发布成功')
    emit('publish')
  } catch (e) {
    ElMessage.error('发布失败')
  }
}

const handleNewVersion = () => {
  if (!templateInfo.id) {
    ElMessage.warning('请先保存模板')
    return
  }
  const versions = (templateInfo.version || '1.0.0').split('.')
  versions[2] = parseInt(versions[2] || '0') + 1
  versionForm.version = versions.join('.')
  versionForm.description = ''
  versionVisible.value = true
}

const handleVersionSubmit = async () => {
  if (!versionFormRef.value) return
  await versionFormRef.value.validate()
  try {
    const formSchema = buildFormSchema()
    await createNewVersion(templateInfo.id, {
      formSchema,
      changeSummary: versionForm.description || '新建版本 ' + versionForm.version
    })
    templateInfo.version = versionForm.version
    ElMessage.success('新版本创建成功')
    versionVisible.value = false
    emit('new-version', { version: versionForm.version, description: versionForm.description })
  } catch (e) {
    ElMessage.error('创建新版本失败')
  }
}

const handlePreview = () => {
  previewVisible.value = true
}

const handlePreviewSubmit = (data) => {
  ElMessage.success('预览提交成功')
  console.log('表单数据:', data)
}

const handleValidate = async () => {
  const formSchema = buildFormSchema()
  try {
    await validateSchema(formSchema)
    ElMessage.success('Schema校验通过')
  } catch (e) {
    const errors = validateLocal(fields.value)
    if (errors.length === 0) {
      ElMessage.success('本地校验通过')
    } else {
      ElMessage.error(errors.join('; '))
    }
  }
}

const validateLocal = (fieldList) => {
  const errors = []
  if (!fieldList || fieldList.length === 0) {
    errors.push('表单至少需要一个字段')
  }
  const keys = new Set()
  fieldList?.forEach((field, index) => {
    if (!field.label) {
      errors.push(`第${index + 1}个字段缺少标签`)
    }
    if (!field.key) {
      errors.push(`第${index + 1}个字段缺少Key`)
    } else if (keys.has(field.key)) {
      errors.push(`字段Key重复: ${field.key}`)
    }
    keys.add(field.key)
  })
  return errors
}

const handleExport = () => {
  schemaTab.value = 'schema'
  schemaVisible.value = true
}

const handleImport = () => {
  schemaTab.value = 'import'
  importJson.value = ''
  schemaVisible.value = true
}

const handleImportSchema = () => {
  try {
    const data = JSON.parse(importJson.value)
    if (data.type === 'object' && data.properties) {
      templateInfo.name = data.title || templateInfo.name
      templateInfo.description = data.description || templateInfo.description
      const requiredKeys = data.required || []
      const importedFields = []
      let sortOrder = 0
      for (const [key, prop] of Object.entries(data.properties)) {
        sortOrder++
        const field = {
          id: `field_${Date.now()}_${sortOrder}`,
          key,
          type: prop.format === 'date' || prop.format === 'datetime' ? prop.format
            : prop.format === 'textarea' ? 'textarea'
            : prop.format === 'file' ? 'file'
            : prop.type === 'number' ? 'number'
            : prop.options ? 'select'
            : 'text',
          label: prop.label || key,
          placeholder: prop.placeholder || '',
          description: prop.description || '',
          unit: prop.unit || '',
          required: requiredKeys.includes(key) || prop.required === true,
          readOnly: prop.readOnly || false,
          hidden: prop.hidden || false,
          maxLines: prop.maxLines,
          maxLength: prop.maxLength,
          precision: prop.precision,
          min: prop.min,
          max: prop.max,
          resultType: prop.isResultField ? (prop.resultType || 'quantitative') : undefined,
          defaultValue: prop.defaultValue,
          options: prop.options || [],
          allowedFileTypes: prop.accept || [],
          multiple: prop.multiple || false,
          widgetConfig: prop.widgetConfig,
          sortOrder
        }
        importedFields.push(field)
      }
      fields.value = importedFields.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
    } else if (data.templateInfo && data.fields) {
      Object.assign(templateInfo, data.templateInfo)
      fields.value = data.fields.map((f, i) => ({
        ...f,
        id: f.id || `field_${Date.now()}_${i}`
      }))
    } else {
      ElMessage.error('无法识别的Schema格式，请使用标准JSON Schema或模板格式')
      return
    }
    ElMessage.success('导入成功')
    schemaVisible.value = false
  } catch (e) {
    ElMessage.error('JSON格式错误')
  }
}

watch(() => props.templateId, async (id) => {
  if (id) {
    try {
      const res = await getTemplate(id)
      if (res) {
        Object.assign(templateInfo, {
          id: res.id,
          name: res.templateName,
          description: res.description,
          status: res.status,
          version: res.version,
          detectItemId: res.detectItemId,
          detectItemName: res.detectItemName,
          remark: res.remark
        })
        if (res.formSchema) {
          try {
            const schema = typeof res.formSchema === 'string'
              ? JSON.parse(res.formSchema)
              : res.formSchema
            if (schema.type === 'object' && schema.properties) {
              templateInfo.name = schema.title || templateInfo.name
              templateInfo.description = schema.description || templateInfo.description
              const requiredKeys = schema.required || []
              const importedFields = []
              let sortOrder = 0
              for (const [key, prop] of Object.entries(schema.properties)) {
                sortOrder++
                importedFields.push({
                  id: `field_${Date.now()}_${sortOrder}`,
                  key,
                  type: prop.format === 'date' || prop.format === 'datetime' ? prop.format
                    : prop.format === 'textarea' ? 'textarea'
                    : prop.format === 'file' ? 'file'
                    : prop.type === 'number' ? 'number'
                    : prop.options ? 'select'
                    : 'text',
                  label: prop.label || key,
                  placeholder: prop.placeholder || '',
                  description: prop.description || '',
                  unit: prop.unit || '',
                  required: requiredKeys.includes(key) || prop.required === true,
                  readOnly: prop.readOnly || false,
                  hidden: prop.hidden || false,
                  maxLines: prop.maxLines,
                  maxLength: prop.maxLength,
                  precision: prop.precision,
                  min: prop.min,
                  max: prop.max,
                  resultType: prop.isResultField ? (prop.resultType || 'quantitative') : undefined,
                  defaultValue: prop.defaultValue,
                  options: prop.options || [],
                  allowedFileTypes: prop.accept || [],
                  multiple: prop.multiple || false,
                  widgetConfig: prop.widgetConfig,
                  sortOrder
                })
              }
              fields.value = importedFields.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
            }
          } catch (parseErr) {
            console.error('解析formSchema失败:', parseErr)
          }
        }
      }
    } catch (e) {
      console.error('加载模板失败:', e)
    }
  }
}, { immediate: true })
</script>

<style lang="scss" scoped>
.form-designer {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .designer-toolbar {
    height: 56px;
    background: #fff;
    border-bottom: 1px solid #e4e7ed;
    padding: 0 20px;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .toolbar-left {
      display: flex;
      align-items: center;
      gap: 12px;

      .template-name {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
    }

    .toolbar-right {
      display: flex;
      gap: 8px;
    }
  }

  .designer-content {
    flex: 1;
    display: flex;
    overflow: hidden;
  }
}
</style>
