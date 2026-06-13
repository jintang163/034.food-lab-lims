<template>
  <div class="form-field-wrapper">
    <label v-if="!hideLabel" class="field-label">
      {{ schema.label }}
      <span v-if="schema.required" class="required">*</span>
    </label>
    <el-input-number
      v-model="inputValue"
      :placeholder="schema.placeholder || '请输入' + schema.label"
      :disabled="disabled || readonly"
      :min="schema.min"
      :max="schema.max"
      :step="schema.step || 1"
      :precision="schema.precision"
      :controls="schema.controls !== false"
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
    type: [String, Number],
    default: null
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
