<template>
  <div class="form-field-wrapper">
    <label v-if="!hideLabel" class="field-label">
      {{ schema.label }}
      <span v-if="schema.required" class="required">*</span>
    </label>
    <el-input
      v-model="inputValue"
      type="textarea"
      :placeholder="schema.placeholder || '请输入' + schema.label"
      :disabled="disabled || readonly"
      :rows="schema.rows || 3"
      :maxlength="schema.maxLength"
      :show-word-limit="schema.showWordLimit && schema.maxLength"
      :autosize="schema.autosize"
      @input="handleInput"
      @change="handleChange"
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
    type: String,
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

const handleInput = () => {
  emit('update:modelValue', inputValue.value)
}

const handleChange = () => {
  emit('change', inputValue.value)
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
