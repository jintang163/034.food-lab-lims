<template>
  <div class="form-field-wrapper">
    <label v-if="!hideLabel" class="field-label">
      {{ schema.label }}
      <span v-if="schema.required" class="required">*</span>
    </label>
    <el-select
      v-model="inputValue"
      :placeholder="schema.placeholder || '请选择' + schema.label"
      :disabled="disabled || readonly"
      :multiple="schema.multiple"
      :filterable="schema.filterable"
      :clearable="schema.clearable !== false"
      @change="handleChange"
      style="width: 100%"
    >
      <el-option
        v-for="option in schema.options || []"
        :key="option.value"
        :label="option.label"
        :value="option.value"
      />
    </el-select>
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
    type: [String, Number, Array],
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
