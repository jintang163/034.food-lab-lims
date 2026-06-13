<template>
  <div class="form-field-wrapper">
    <label v-if="!hideLabel" class="field-label">
      {{ schema.label }}
      <span v-if="schema.required" class="required">*</span>
    </label>
    <el-date-picker
      v-model="inputValue"
      :type="schema.dateType || 'date'"
      :placeholder="schema.placeholder || '请选择' + schema.label"
      :disabled="disabled || readonly"
      :format="schema.format || 'YYYY-MM-DD'"
      :value-format="schema.valueFormat || 'YYYY-MM-DD'"
      :start-placeholder="schema.startPlaceholder || '开始日期'"
      :end-placeholder="schema.endPlaceholder || '结束日期'"
      :range-separator="schema.rangeSeparator || '至'"
      :clearable="schema.clearable !== false"
      @change="handleChange"
      style="width: 100%"
    />
    <div v-if="schema.description" class="field-desc">{{ schema.description }}</div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

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

const inputValue = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  inputValue.value = val
})

const handleChange = (val) => {
  emit('update:modelValue', val)
  emit('change', val)
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
