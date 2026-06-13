<template>
  <div class="form-field-wrapper">
    <label v-if="!hideLabel" class="field-label">
      {{ schema.label }}
      <span v-if="schema.required" class="required">*</span>
    </label>
    <el-upload
      v-model:file-list="fileList"
      :action="schema.uploadUrl || '/api/upload'"
      :multiple="schema.multiple"
      :limit="schema.limit"
      :accept="schema.accept"
      :disabled="disabled || readonly"
      :on-success="handleSuccess"
      :on-remove="handleRemove"
      :before-upload="beforeUpload"
      list-type="text"
    >
      <el-button type="primary" :disabled="disabled || readonly">
        <el-icon><Upload /></el-icon>
        上传文件
      </el-button>
      <template #tip>
        <div v-if="schema.tip" class="el-upload__tip">{{ schema.tip }}</div>
      </template>
    </el-upload>
    <div v-if="schema.description" class="field-desc">{{ schema.description }}</div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'

const props = defineProps({
  schema: {
    type: Object,
    required: true
  },
  modelValue: {
    type: [String, Array],
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  readonly: {
    type: Boolean,
    default: false
  },
  hideLabel: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const fileList = ref([])

watch(() => props.modelValue, (val) => {
  if (val) {
    if (Array.isArray(val)) {
      fileList.value = val.map((url, index) => ({
        name: `file_${index}`,
        url: url,
        response: { url: url }
      }))
    } else {
      fileList.value = [{
        name: 'file',
        url: val,
        response: { url: val }
      }]
    }
  }
}, { immediate: true })

const beforeUpload = (file) => {
  if (props.schema.maxSize) {
    const isLtMax = file.size / 1024 / 1024 < props.schema.maxSize
    if (!isLtMax) {
      ElMessage.error(`文件大小不能超过 ${props.schema.maxSize}MB!`)
      return false
    }
  }
  return true
}

const handleSuccess = (response, file) => {
  file.url = response.url
  updateValue()
}

const handleRemove = () => {
  updateValue()
}

const updateValue = () => {
  const urls = fileList.value.map(f => f.url || f.response?.url).filter(Boolean)
  const value = props.schema.multiple ? urls : urls[0] || ''
  emit('update:modelValue', value)
  emit('change', value)
}
</script>

<style lang="scss" scoped>
.form-field-wrapper {
  width: 100%;

  .field-label {
    display: block;
    margin-bottom: 8px;
    font-size: 14px;
    color: #303133;

    .required {
      color: #f56c6c;
      margin-right: 4px;
    }
  }

  .field-desc {
    margin-top: 6px;
    font-size: 12px;
    color: #909399;
  }
}
</style>
