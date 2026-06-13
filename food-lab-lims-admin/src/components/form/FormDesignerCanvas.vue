<template>
  <div class="form-designer-canvas">
    <div class="canvas-header">
      <el-icon><EditPen /></el-icon>
      <span>设计画布</span>
      <div class="canvas-actions">
        <el-button size="small" @click="$emit('preview')">
          <el-icon><View /></el-icon>
          预览
        </el-button>
        <el-button size="small" type="primary" @click="handleClear">
          <el-icon><Delete /></el-icon>
          清空
        </el-button>
      </div>
    </div>
    <div class="canvas-content">
      <div class="form-wrapper">
        <div class="form-title">{{ templateInfo.name || '表单标题' }}</div>
        <div class="form-desc">{{ templateInfo.description || '表单描述信息' }}</div>
        <draggable
          v-model="fields"
          group="fields"
          item-key="id"
          class="canvas-fields"
          ghost-class="ghost"
          chosen-class="chosen"
          drag-class="drag"
          @start="drag = true"
          @end="drag = false"
        >
          <template #item="{ element, index }">
            <div
              class="field-item-wrapper"
              :class="{ 'selected': selectedField?.id === element.id }"
              @click.stop="selectField(element)"
            >
              <div class="field-actions">
                <el-tooltip content="上移" placement="top">
                  <el-icon class="action-icon" @click.stop="moveUp(index)">
                    <Top />
                  </el-icon>
                </el-tooltip>
                <el-tooltip content="下移" placement="top">
                  <el-icon class="action-icon" @click.stop="moveDown(index)">
                    <Bottom />
                  </el-icon>
                </el-tooltip>
                <el-tooltip content="复制" placement="top">
                  <el-icon class="action-icon" @click.stop="copyField(index)">
                    <CopyDocument />
                  </el-icon>
                </el-tooltip>
                <el-tooltip content="删除" placement="top">
                  <el-icon class="action-icon delete" @click.stop="deleteField(index)">
                    <Delete />
                  </el-icon>
                </el-tooltip>
              </div>
              <component
                :is="getFieldComponent(element.type)"
                :schema="element"
                :modelValue="getDefaultValue(element)"
                hideLabel
                disabled
              />
            </div>
          </template>
        </draggable>
        <div v-if="fields.length === 0" class="empty-tip">
          <el-empty description="拖拽左侧控件到这里" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import draggable from 'vuedraggable'
import {
  EditPen, View, Delete, Top, Bottom, CopyDocument
} from '@element-plus/icons-vue'
import TextField from './fields/TextField.vue'
import NumberField from './fields/NumberField.vue'
import DateField from './fields/DateField.vue'
import SelectField from './fields/SelectField.vue'
import TextareaField from './fields/TextareaField.vue'
import FileField from './fields/FileField.vue'
import CheckboxField from './fields/CheckboxField.vue'
import RadioField from './fields/RadioField.vue'

const props = defineProps({
  fields: {
    type: Array,
    default: () => []
  },
  selectedField: {
    type: Object,
    default: null
  },
  templateInfo: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:fields', 'select', 'preview', 'update:selectedField'])

const drag = ref(false)

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

const getDefaultValue = (field) => {
  if (field.defaultValue !== undefined && field.defaultValue !== null) {
    return field.defaultValue
  }
  if (field.type === 'checkbox') {
    return []
  }
  if (field.type === 'number') {
    return null
  }
  return ''
}

const selectField = (field) => {
  emit('select', field)
  emit('update:selectedField', field)
}

const moveUp = (index) => {
  if (index > 0) {
    const newFields = [...props.fields]
    const temp = newFields[index]
    newFields[index] = newFields[index - 1]
    newFields[index - 1] = temp
    emit('update:fields', newFields)
  }
}

const moveDown = (index) => {
  if (index < props.fields.length - 1) {
    const newFields = [...props.fields]
    const temp = newFields[index]
    newFields[index] = newFields[index + 1]
    newFields[index + 1] = temp
    emit('update:fields', newFields)
  }
}

const copyField = (index) => {
  const field = props.fields[index]
  const newField = {
    ...field,
    id: `field_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    key: field.key + '_copy_' + Date.now()
  }
  const newFields = [...props.fields]
  newFields.splice(index + 1, 0, newField)
  emit('update:fields', newFields)
}

const deleteField = (index) => {
  const newFields = props.fields.filter((_, i) => i !== index)
  emit('update:fields', newFields)
  if (props.selectedField?.id === props.fields[index].id) {
    emit('update:selectedField', null)
  }
}

const handleClear = () => {
  emit('update:fields', [])
  emit('update:selectedField', null)
}
</script>

<style lang="scss" scoped>
.form-designer-canvas {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;

  .canvas-header {
    padding: 14px 20px;
    background: #fff;
    border-bottom: 1px solid #e4e7ed;
    display: flex;
    align-items: center;
    gap: 8px;

    .el-icon {
      color: #409eff;
    }

    span {
      font-weight: 600;
      font-size: 15px;
      color: #303133;
    }

    .canvas-actions {
      margin-left: auto;
      display: flex;
      gap: 8px;
    }
  }

  .canvas-content {
    flex: 1;
    overflow-y: auto;
    padding: 30px;
  }

  .form-wrapper {
    max-width: 800px;
    margin: 0 auto;
    background: #fff;
    border-radius: 8px;
    padding: 40px;
    min-height: 500px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

    .form-title {
      font-size: 22px;
      font-weight: 700;
      color: #303133;
      text-align: center;
      margin-bottom: 8px;
    }

    .form-desc {
      font-size: 13px;
      color: #909399;
      text-align: center;
      margin-bottom: 30px;
    }
  }

  .canvas-fields {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .field-item-wrapper {
    position: relative;
    padding: 16px;
    border: 2px solid transparent;
    border-radius: 6px;
    transition: all 0.2s;

    &:hover {
      border-color: #dcdfe6;
      background: #fafafa;

      .field-actions {
        opacity: 1;
      }
    }

    &.selected {
      border-color: #409eff;
      background: #ecf5ff;
    }

    .field-actions {
      position: absolute;
      top: -12px;
      right: 10px;
      display: flex;
      gap: 4px;
      background: #fff;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      padding: 2px;
      opacity: 0;
      transition: opacity 0.2s;
      z-index: 10;

      .action-icon {
        width: 24px;
        height: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        color: #606266;
        border-radius: 3px;

        &:hover {
          background: #f5f7fa;
          color: #409eff;
        }

        &.delete:hover {
          color: #f56c6c;
        }
      }
    }
  }

  .empty-tip {
    padding: 60px 0;
  }

  .ghost {
    opacity: 0.5;
    background: #c8ebfb;
  }

  .chosen {
    opacity: 0.8;
  }

  .drag {
    opacity: 0.8;
  }
}
</style>
