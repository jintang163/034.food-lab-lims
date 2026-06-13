<template>
  <div class="form-property-panel">
    <div class="panel-header">
      <el-icon><Setting /></el-icon>
      <span>属性配置</span>
    </div>
    <div class="panel-content">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本属性" name="basic">
          <div v-if="selectedField" class="property-form">
            <el-form label-width="80px" size="small">
              <el-form-item label="字段标签">
                <el-input v-model="selectedField.label" @input="updateField" />
              </el-form-item>
              <el-form-item label="字段Key">
                <el-input v-model="selectedField.key" @input="updateField" />
              </el-form-item>
              <el-form-item label="控件类型">
                <el-select v-model="selectedField.type" disabled>
                  <el-option label="文本" value="text" />
                  <el-option label="数值" value="number" />
                  <el-option label="日期" value="date" />
                  <el-option label="下拉选择" value="select" />
                  <el-option label="多行文本" value="textarea" />
                  <el-option label="文件上传" value="file" />
                  <el-option label="复选框" value="checkbox" />
                  <el-option label="单选框" value="radio" />
                </el-select>
              </el-form-item>
              <el-form-item label="是否必填">
                <el-switch v-model="selectedField.required" @change="updateField" />
              </el-form-item>
              <el-form-item label="占位符">
                <el-input v-model="selectedField.placeholder" @input="updateField" placeholder="请输入占位符" />
              </el-form-item>
              <el-form-item label="默认值">
                <el-input
                  v-if="selectedField.type !== 'checkbox'"
                  v-model="selectedField.defaultValue"
                  @input="updateField"
                  placeholder="请输入默认值"
                />
                <el-select
                  v-else
                  v-model="selectedField.defaultValue"
                  multiple
                  @change="updateField"
                  placeholder="请选择默认值"
                  style="width: 100%"
                >
                  <el-option
                    v-for="opt in selectedField.options"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="描述">
                <el-input
                  v-model="selectedField.description"
                  type="textarea"
                  :rows="2"
                  @input="updateField"
                  placeholder="请输入字段描述"
                />
              </el-form-item>
            </el-form>
          </div>
          <el-empty v-else description="请选择一个字段" />
        </el-tab-pane>

        <el-tab-pane label="高级配置" name="advanced">
          <div v-if="selectedField" class="property-form">
            <el-form label-width="80px" size="small">
              <template v-if="selectedField.type === 'text' || selectedField.type === 'textarea'">
                <el-form-item label="最大长度">
                  <el-input-number
                    v-model="selectedField.maxLength"
                    @change="updateField"
                    :min="0"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="显示字数">
                  <el-switch v-model="selectedField.showWordLimit" @change="updateField" />
                </el-form-item>
              </template>

              <template v-if="selectedField.type === 'textarea'">
                <el-form-item label="行数">
                  <el-input-number
                    v-model="selectedField.rows"
                    @change="updateField"
                    :min="1"
                    :max="20"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="自动高度">
                  <el-switch v-model="selectedField.autosize" @change="updateField" />
                </el-form-item>
              </template>

              <template v-if="selectedField.type === 'number'">
                <el-form-item label="最小值">
                  <el-input-number
                    v-model="selectedField.min"
                    @change="updateField"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="最大值">
                  <el-input-number
                    v-model="selectedField.max"
                    @change="updateField"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="步长">
                  <el-input-number
                    v-model="selectedField.step"
                    @change="updateField"
                    :min="0.01"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="精度">
                  <el-input-number
                    v-model="selectedField.precision"
                    @change="updateField"
                    :min="0"
                    :max="10"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="显示控件">
                  <el-switch v-model="selectedField.controls" @change="updateField" />
                </el-form-item>
              </template>

              <template v-if="selectedField.type === 'date'">
                <el-form-item label="日期类型">
                  <el-select v-model="selectedField.dateType" @change="updateField" style="width: 100%">
                    <el-option label="日期" value="date" />
                    <el-option label="日期范围" value="daterange" />
                    <el-option label="日期时间" value="datetime" />
                    <el-option label="月份" value="month" />
                    <el-option label="年份" value="year" />
                  </el-select>
                </el-form-item>
                <el-form-item label="显示格式">
                  <el-input v-model="selectedField.format" @input="updateField" placeholder="YYYY-MM-DD" />
                </el-form-item>
                <el-form-item label="值格式">
                  <el-input v-model="selectedField.valueFormat" @input="updateField" placeholder="YYYY-MM-DD" />
                </el-form-item>
              </template>

              <template v-if="selectedField.type === 'select' || selectedField.type === 'checkbox' || selectedField.type === 'radio'">
                <el-form-item label="可多选">
                  <el-switch
                    v-if="selectedField.type === 'select'"
                    v-model="selectedField.multiple"
                    @change="updateField"
                  />
                  <span v-else>-</span>
                </el-form-item>
                <el-form-item label="可搜索">
                  <el-switch
                    v-if="selectedField.type === 'select'"
                    v-model="selectedField.filterable"
                    @change="updateField"
                  />
                  <span v-else>-</span>
                </el-form-item>
                <el-form-item label="边框样式">
                  <el-switch v-model="selectedField.border" @change="updateField" />
                </el-form-item>
                <el-form-item label="选项配置">
                  <div class="options-editor">
                    <el-table
                      :data="selectedField.options"
                      border
                      size="small"
                      style="width: 100%"
                    >
                      <el-table-column prop="label" label="标签">
                        <template #default="{ row }">
                          <el-input
                            v-model="row.label"
                            size="small"
                            @input="updateField"
                          />
                        </template>
                      </el-table-column>
                      <el-table-column prop="value" label="值">
                        <template #default="{ row }">
                          <el-input
                            v-model="row.value"
                            size="small"
                            @input="updateField"
                          />
                        </template>
                      </el-table-column>
                      <el-table-column label="操作" width="60">
                        <template #default="{ $index }">
                          <el-button
                            type="danger"
                            link
                            size="small"
                            @click="removeOption($index)"
                          >
                            删除
                          </el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                    <el-button
                      size="small"
                      style="margin-top: 8px; width: 100%"
                      @click="addOption"
                    >
                      <el-icon><Plus /></el-icon>
                      添加选项
                    </el-button>
                  </div>
                </el-form-item>
              </template>

              <template v-if="selectedField.type === 'file'">
                <el-form-item label="可多选">
                  <el-switch v-model="selectedField.multiple" @change="updateField" />
                </el-form-item>
                <el-form-item label="最大数量">
                  <el-input-number
                    v-model="selectedField.limit"
                    @change="updateField"
                    :min="1"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="最大大小(MB)">
                  <el-input-number
                    v-model="selectedField.maxSize"
                    @change="updateField"
                    :min="1"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="文件类型">
                  <el-input v-model="selectedField.accept" @input="updateField" placeholder=".jpg,.png,.pdf" />
                </el-form-item>
                <el-form-item label="提示信息">
                  <el-input v-model="selectedField.tip" @input="updateField" placeholder="上传提示" />
                </el-form-item>
              </template>

              <el-form-item label="可清空">
                <el-switch v-model="selectedField.clearable" @change="updateField" />
              </el-form-item>
            </el-form>
          </div>
          <el-empty v-else description="请选择一个字段" />
        </el-tab-pane>

        <el-tab-pane label="校验规则" name="validation">
          <div v-if="selectedField" class="property-form">
            <el-form label-width="80px" size="small">
              <el-form-item label="正则表达式">
                <el-input
                  v-model="selectedField.pattern"
                  @input="updateField"
                  placeholder="如: ^[a-zA-Z0-9]+$"
                />
              </el-form-item>
              <el-form-item label="错误提示">
                <el-input
                  v-model="selectedField.errorMessage"
                  @input="updateField"
                  placeholder="校验失败时的提示信息"
                />
              </el-form-item>
              <el-form-item label="自定义校验">
                <el-input
                  v-model="selectedField.validator"
                  type="textarea"
                  :rows="4"
                  @input="updateField"
                  placeholder="function(value) { return true; }"
                />
              </el-form-item>
              <div class="validation-tips">
                <el-alert
                  title="校验规则说明"
                  type="info"
                  :closable="false"
                  size="small"
                >
                  <p>1. 必填校验已在基本属性中配置</p>
                  <p>2. 正则表达式用于格式校验</p>
                  <p>3. 自定义校验函数返回true表示校验通过</p>
                </el-alert>
              </div>
            </el-form>
          </div>
          <el-empty v-else description="请选择一个字段" />
        </el-tab-pane>

        <el-tab-pane label="表单属性" name="form">
          <div class="property-form">
            <el-form label-width="80px" size="small">
              <el-form-item label="表单名称">
                <el-input v-model="templateInfo.name" @input="updateTemplate" />
              </el-form-item>
              <el-form-item label="表单描述">
                <el-input
                  v-model="templateInfo.description"
                  type="textarea"
                  :rows="3"
                  @input="updateTemplate"
                />
              </el-form-item>
              <el-form-item label="标签宽度">
                <el-input-number
                  v-model="templateInfo.labelWidth"
                  @change="updateTemplate"
                  :min="50"
                  :max="200"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="标签位置">
                <el-select v-model="templateInfo.labelPosition" @change="updateTemplate" style="width: 100%">
                  <el-option label="左对齐" value="left" />
                  <el-option label="右对齐" value="right" />
                  <el-option label="顶部" value="top" />
                </el-select>
              </el-form-item>
              <el-form-item label="显示提交按钮">
                <el-switch v-model="templateInfo.showSubmit" @change="updateTemplate" />
              </el-form-item>
              <el-form-item label="显示重置按钮">
                <el-switch v-model="templateInfo.showReset" @change="updateTemplate" />
              </el-form-item>
              <el-form-item label="提交按钮文本">
                <el-input v-model="templateInfo.submitText" @input="updateTemplate" />
              </el-form-item>
              <el-form-item label="重置按钮文本">
                <el-input v-model="templateInfo.resetText" @input="updateTemplate" />
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Setting, Plus } from '@element-plus/icons-vue'

const props = defineProps({
  selectedField: {
    type: Object,
    default: null
  },
  templateInfo: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:selectedField', 'update:templateInfo'])

const activeTab = ref('basic')

const updateField = () => {
  emit('update:selectedField', { ...props.selectedField })
}

const updateTemplate = () => {
  emit('update:templateInfo', { ...props.templateInfo })
}

const addOption = () => {
  if (!props.selectedField.options) {
    props.selectedField.options = []
  }
  const index = props.selectedField.options.length + 1
  props.selectedField.options.push({
    label: `选项${index}`,
    value: `option${index}`
  })
  updateField()
}

const removeOption = (index) => {
  props.selectedField.options.splice(index, 1)
  updateField()
}
</script>

<style lang="scss" scoped>
.form-property-panel {
  width: 320px;
  height: 100%;
  background: #fff;
  border-left: 1px solid #e4e7ed;
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
  }

  .property-form {
    padding: 16px;
  }

  .options-editor {
    width: 100%;
  }

  .validation-tips {
    margin-top: 16px;
  }

  :deep(.el-tabs__header) {
    margin: 0;
  }

  :deep(.el-tabs__nav) {
    padding: 0 16px;
  }

  :deep(.el-tabs__item) {
    padding: 0 12px;
  }
}
</style>
