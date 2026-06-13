<template>
  <div class="form-field-panel">
    <div class="panel-header">
      <el-icon><Grid /></el-icon>
      <span>控件面板</span>
    </div>
    <div class="panel-content">
      <draggable
        v-model="fieldGroups"
        group="fields"
        :sort="false"
        :clone="cloneField"
        item-key="type"
        class="field-list"
      >
        <template #item="{ element }">
          <div class="field-group">
            <div class="group-title">{{ element.group }}</div>
            <draggable
              v-model="element.fields"
              group="fields"
              :sort="false"
              :clone="cloneField"
              item-key="type"
              class="field-items"
            >
              <template #item="{ element: field }">
                <div class="field-item" :class="field.type" draggable="true">
                  <el-icon class="field-icon">
                    <component :is="field.icon" />
                  </el-icon>
                  <span class="field-name">{{ field.label }}</span>
                </div>
              </template>
            </draggable>
          </div>
        </template>
      </draggable>
    </div>
  </div>
</template>

<script setup>
import { ref, markRaw } from 'vue'
import draggable from 'vuedraggable'
import {
  Grid, Edit, Number, Calendar, Select,
  Document, Upload, Check, RadioButton
} from '@element-plus/icons-vue'

const emit = defineEmits(['add-field'])

const fieldGroups = ref([
  {
    group: '基础控件',
    fields: [
      { type: 'text', label: '文本', icon: markRaw(Edit), component: 'TextField' },
      { type: 'number', label: '数值', icon: markRaw(Number), component: 'NumberField' },
      { type: 'date', label: '日期', icon: markRaw(Calendar), component: 'DateField' },
      { type: 'select', label: '下拉选择', icon: markRaw(Select), component: 'SelectField' }
    ]
  },
  {
    group: '高级控件',
    fields: [
      { type: 'textarea', label: '多行文本', icon: markRaw(Document), component: 'TextareaField' },
      { type: 'file', label: '文件上传', icon: markRaw(Upload), component: 'FileField' },
      { type: 'checkbox', label: '复选框', icon: markRaw(Check), component: 'CheckboxField' },
      { type: 'radio', label: '单选框', icon: markRaw(RadioButton), component: 'RadioField' }
    ]
  }
])

const cloneField = (field) => {
  const fieldId = `field_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  return {
    ...field,
    id: fieldId,
    label: field.label,
    key: field.type + '_' + Date.now(),
    required: false,
    placeholder: '',
    defaultValue: '',
    description: '',
    options: field.type === 'select' || field.type === 'checkbox' || field.type === 'radio'
      ? [
          { label: '选项1', value: 'option1' },
          { label: '选项2', value: 'option2' },
          { label: '选项3', value: 'option3' }
        ]
      : undefined
  }
}
</script>

<style lang="scss" scoped>
.form-field-panel {
  width: 260px;
  height: 100%;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;

  .panel-header {
    padding: 16px;
    font-weight: 600;
    font-size: 15px;
    color: #303133;
    border-bottom: 1px solid #e4e7ed;
    display: flex;
    align-items: center;
    gap: 8px;

    .el-icon {
      color: #409eff;
    }
  }

  .panel-content {
    flex: 1;
    overflow-y: auto;
    padding: 12px;
  }

  .field-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .field-group {
    .group-title {
      font-size: 13px;
      font-weight: 600;
      color: #606266;
      margin-bottom: 10px;
      padding-left: 4px;
      border-left: 3px solid #409eff;
    }

    .field-items {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 10px;
    }
  }

  .field-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 14px 8px;
    background: #f5f7fa;
    border: 1px dashed #dcdfe6;
    border-radius: 6px;
    cursor: grab;
    transition: all 0.2s;

    &:hover {
      background: #ecf5ff;
      border-color: #409eff;
      color: #409eff;

      .field-icon {
        color: #409eff;
      }
    }

    &:active {
      cursor: grabbing;
    }

    .field-icon {
      font-size: 20px;
      color: #606266;
      margin-bottom: 6px;
    }

    .field-name {
      font-size: 12px;
      color: #606266;
    }

    &.text .field-icon { color: #409eff; }
    &.number .field-icon { color: #67c23a; }
    &.date .field-icon { color: #e6a23c; }
    &.select .field-icon { color: #f56c6c; }
    &.textarea .field-icon { color: #909399; }
    &.file .field-icon { color: #409eff; }
    &.checkbox .field-icon { color: #67c23a; }
    &.radio .field-icon { color: #e6a23c; }
  }
}
</style>
