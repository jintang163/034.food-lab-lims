<template>
  <div class="form-preview">
    <div v-if="schema && schema.fields && schema.fields.length > 0" class="preview-wrapper">
      <div class="form-header">
        <h2 class="form-title">{{ templateInfo.name || '表单预览' }}</h2>
        <p v-if="templateInfo.description" class="form-desc">{{ templateInfo.description }}</p>
      </div>
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-width="templateInfo.labelWidth || '100px'"
        :label-position="templateInfo.labelPosition || 'right'"
        class="preview-form"
      >
        <el-form-item
          v-for="field in schema.fields"
          :key="field.id"
          :prop="field.key"
          :label="field.label"
          :required="field.required"
        >
          <component
            :is="getFieldComponent(field.type)"
            :schema="field"
            v-model="formData[field.key]"
            :disabled="disabled"
            :readonly="readonly"
            hide-label
          />
        </el-form-item>
        <el-form-item v-if="templateInfo.showSubmit !== false || templateInfo.showReset !== false" class="form-actions">
          <el-button
            v-if="templateInfo.showSubmit !== false"
            type="primary"
            @click="handleSubmit"
            :disabled="disabled || readonly"
          >
            {{ templateInfo.submitText || '提交' }}
          </el-button>
          <el-button
            v-if="templateInfo.showReset !== false"
            @click="handleReset"
            :disabled="disabled || readonly"
          >
            {{ templateInfo.resetText || '重置' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-empty v-else description="暂无表单字段，请先添加字段" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import TextField from './fields/TextField.vue'
import NumberField from './fields/NumberField.vue'
import DateField from './fields/DateField.vue'
import SelectField from './fields/SelectField.vue'
import TextareaField from './fields/TextareaField.vue'
import FileField from './fields/FileField.vue'
import CheckboxField from './fields/CheckboxField.vue'
import RadioField from './fields/RadioField.vue'

const props = defineProps({
  schema: {
    type: Object,
    default: () => ({ fields: [] })
  },
  templateInfo: {
    type: Object,
    default: () => ({})
  },
  disabled: {
    type: Boolean,
    default: false
  },
  readonly: {
    type: Boolean,
    default: false
  },
  modelValue: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'submit', 'reset'])

const formRef = ref(null)
const formData = reactive({})

const fieldComponents = {
  text: TextField,
  number: NumberField,
  date: DateField,
  select: SelectField,
  textarea: TextareaField,
  file: FileField,
  checkbox: CheckboxField,
  radio: RadioField
}

const getFieldComponent = (type) => {
  return fieldComponents[type] || TextField
}

const formRules = computed(() => {
  const rules = {}
  if (props.schema && props.schema.fields) {
    props.schema.fields.forEach(field => {
      const fieldRules = []
      if (field.required) {
        fieldRules.push({ required: true, message: `请${field.type === 'select' || field.type === 'checkbox' || field.type === 'radio' ? '选择' : '输入'}${field.label}`, trigger: field.type === 'select' || field.type === 'checkbox' || field.type === 'radio' ? 'change' : 'blur' })
      }
      if (field.pattern) {
        fieldRules.push({ pattern: new RegExp(field.pattern), message: field.errorMessage || '格式不正确', trigger: 'blur' })
      }
      if (field.validator) {
        try {
          const validatorFn = new Function(`return ${field.validator}`)()
          fieldRules.push({ validator: (rule, value, callback) => {
            if (validatorFn(value)) {
              callback()
            } else {
              callback(new Error(field.errorMessage || '校验失败'))
            }
          }, trigger: 'blur' })
        } catch (e) {
          console.error('自定义校验函数解析失败:', e)
        }
      }
      rules[field.key] = fieldRules
    })
  }
  return rules
})

watch(() => props.modelValue, (val) => {
  if (val) {
    Object.assign(formData, val)
  }
}, { deep: true, immediate: true })

watch(() => props.schema, (schema) => {
  if (schema && schema.fields) {
    schema.fields.forEach(field => {
      if (formData[field.key] === undefined) {
        if (field.defaultValue !== undefined && field.defaultValue !== null) {
          formData[field.key] = field.defaultValue
        } else if (field.type === 'checkbox') {
          formData[field.key] = []
        } else if (field.type === 'number') {
          formData[field.key] = null
        } else {
          formData[field.key] = ''
        }
      }
    })
  }
}, { deep: true, immediate: true })

watch(formData, (val) => {
  emit('update:modelValue', { ...val })
}, { deep: true })

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    emit('submit', { ...formData })
  } catch (e) {
    console.error('表单校验失败:', e)
  }
}

const handleReset = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  emit('reset')
}
</script>

<style lang="scss" scoped>
.form-preview {
  width: 100%;
  padding: 20px;

  .preview-wrapper {
    max-width: 800px;
    margin: 0 auto;
    background: #fff;
    border-radius: 8px;
    padding: 30px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }

  .form-header {
    text-align: center;
    margin-bottom: 30px;

    .form-title {
      font-size: 24px;
      font-weight: 700;
      color: #303133;
      margin: 0 0 10px 0;
    }

    .form-desc {
      font-size: 14px;
      color: #909399;
      margin: 0;
    }
  }

  .preview-form {
    .el-form-item {
      margin-bottom: 24px;
    }

    .form-actions {
      text-align: center;
      margin-top: 30px;

      .el-button + .el-button {
        margin-left: 20px;
      }
    }
  }
}
</style>
