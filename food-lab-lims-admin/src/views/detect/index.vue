<template>
  <div class="detect-container">
    <el-card shadow="hover">
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="项目名称">
            <el-input v-model="searchForm.name" placeholder="请输入" clearable />
          </el-form-item>
          <el-form-item label="项目分类">
            <el-select v-model="searchForm.category" placeholder="请选择" clearable style="width: 150px">
              <el-option label="微生物" value="microbe" />
              <el-option label="重金属" value="heavy" />
              <el-option label="农药残留" value="pesticide" />
              <el-option label="食品添加剂" value="additive" />
              <el-option label="理化指标" value="physical" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增项目
        </el-button>
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="itemCode" label="项目编号" width="120" />
        <el-table-column prop="itemName" label="项目名称" min-width="150" />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.category)" size="small">
              {{ getCategoryText(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="method" label="检测方法" min-width="200" />
        <el-table-column prop="price" label="检测费用" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="duration" label="检测周期" width="100">
          <template #default="{ row }">{{ row.duration }}个工作日</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'info'" size="small">
              {{ row.status === '1' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="handleSchema(row)">表单配置</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="项目编号" prop="itemCode">
              <el-input v-model="form.itemCode" :disabled="isEdit" placeholder="自动生成" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目名称" prop="itemName">
              <el-input v-model="form.itemName" placeholder="请输入项目名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="项目分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择" style="width: 100%">
                <el-option label="微生物" value="microbe" />
                <el-option label="重金属" value="heavy" />
                <el-option label="农药残留" value="pesticide" />
                <el-option label="食品添加剂" value="additive" />
                <el-option label="理化指标" value="physical" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位">
              <el-input v-model="form.unit" placeholder="如：mg/kg" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="检测费用">
              <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检测周期">
              <el-input-number v-model="form.duration" :min="1" style="width: 100%" />
              <span style="margin-left: 8px; font-size: 12px; color: #909399">个工作日</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="检测方法">
          <el-input v-model="form.method" placeholder="请输入检测方法标准号或名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="1">启用</el-radio>
            <el-radio value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="schemaVisible" title="表单Schema配置" width="800px">
      <div class="schema-config">
        <div class="schema-list">
          <div class="list-title">字段列表</div>
          <div
            v-for="(field, index) in formSchema"
            :key="index"
            class="schema-item"
            :class="{ active: currentFieldIndex === index }"
            @click="selectField(index)"
          >
            <span class="field-label">{{ field.label }}</span>
            <span class="field-type">{{ field.type }}</span>
          </div>
          <el-button type="primary" plain size="small" @click="addField">
            <el-icon><Plus /></el-icon>
            添加字段
          </el-button>
        </div>
        <div class="schema-editor">
          <div class="editor-title">字段属性</div>
          <el-form v-if="currentField" label-width="80px" size="small">
            <el-form-item label="字段标签">
              <el-input v-model="currentField.label" />
            </el-form-item>
            <el-form-item label="字段名">
              <el-input v-model="currentField.field" />
            </el-form-item>
            <el-form-item label="字段类型">
              <el-select v-model="currentField.type" style="width: 100%">
                <el-option label="输入框" value="input" />
                <el-option label="数字输入" value="number" />
                <el-option label="下拉选择" value="select" />
                <el-option label="日期选择" value="date" />
                <el-option label="多行文本" value="textarea" />
              </el-select>
            </el-form-item>
            <el-form-item label="是否必填">
              <el-switch v-model="currentField.required" />
            </el-form-item>
            <el-form-item label="默认值">
              <el-input v-model="currentField.defaultValue" />
            </el-form-item>
            <el-form-item label="选项值">
              <el-input
                v-model="currentField.options"
                type="textarea"
                :rows="3"
                placeholder="每行一个，格式：值:标签"
              />
            </el-form-item>
            <el-button type="danger" size="small" @click="removeField">
              <el-icon><Delete /></el-icon>
              删除字段
            </el-button>
          </el-form>
          <el-empty v-else description="请选择一个字段" />
        </div>
      </div>
      <template #footer>
        <el-button @click="schemaVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSchema">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const schemaVisible = ref(false)
const formRef = ref(null)
const selectedIds = ref([])
const currentFieldIndex = ref(-1)

const searchForm = reactive({
  name: '',
  category: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: '',
  itemCode: '',
  itemName: '',
  category: '',
  unit: '',
  method: '',
  price: 0,
  duration: 1,
  status: '1',
  remark: ''
})

const rules = {
  itemName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择项目分类', trigger: 'change' }]
}

const formSchema = ref([])

const currentField = ref(null)

const tableData = ref([
  { id: 1, itemCode: 'MIC001', itemName: '菌落总数', category: 'microbe', unit: 'CFU/g', method: 'GB 4789.2-2016', price: 50, duration: 3, status: '1' },
  { id: 2, itemCode: 'MIC002', itemName: '大肠菌群', category: 'microbe', unit: 'MPN/g', method: 'GB 4789.3-2016', price: 60, duration: 3, status: '1' },
  { id: 3, itemCode: 'MIC003', itemName: '沙门氏菌', category: 'microbe', unit: '/25g', method: 'GB 4789.4-2016', price: 80, duration: 5, status: '1' },
  { id: 4, itemCode: 'HEV001', itemName: '铅', category: 'heavy', unit: 'mg/kg', method: 'GB 5009.12-2017', price: 100, duration: 5, status: '1' },
  { id: 5, itemCode: 'HEV002', itemName: '砷', category: 'heavy', unit: 'mg/kg', method: 'GB 5009.11-2014', price: 100, duration: 5, status: '1' },
  { id: 6, itemCode: 'HEV003', itemName: '镉', category: 'heavy', unit: 'mg/kg', method: 'GB 5009.15-2014', price: 100, duration: 5, status: '1' },
  { id: 7, itemCode: 'PES001', itemName: '有机磷农药', category: 'pesticide', unit: 'mg/kg', method: 'GB/T 5009.20-2003', price: 200, duration: 7, status: '1' },
  { id: 8, itemCode: 'PES002', itemName: '有机氯农药', category: 'pesticide', unit: 'mg/kg', method: 'GB/T 5009.19-2008', price: 250, duration: 7, status: '1' },
  { id: 9, itemCode: 'ADD001', itemName: '苯甲酸钠', category: 'additive', unit: 'g/kg', method: 'GB 5009.28-2016', price: 80, duration: 5, status: '1' },
  { id: 10, itemCode: 'PHY001', itemName: '酸价', category: 'physical', unit: 'mg/g', method: 'GB 5009.229-2016', price: 60, duration: 3, status: '1' }
])

pagination.total = 48

const getCategoryText = (category) => {
  const map = {
    microbe: '微生物',
    heavy: '重金属',
    pesticide: '农药残留',
    additive: '食品添加剂',
    physical: '理化指标'
  }
  return map[category] || category
}

const getCategoryType = (category) => {
  const map = {
    microbe: 'success',
    heavy: 'warning',
    pesticide: 'danger',
    additive: 'primary',
    physical: 'info'
  }
  return map[category] || 'info'
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => { loading.value = false }, 500)
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.category = ''
  handleSearch()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增检测项目'
  Object.assign(form, {
    id: '',
    itemCode: '',
    itemName: '',
    category: '',
    unit: '',
    method: '',
    price: 0,
    duration: 1,
    status: '1',
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑检测项目'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除"${row.itemName}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个项目吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('批量删除成功')
    selectedIds.value = []
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
  dialogVisible.value = false
}

const handleSchema = (row) => {
  currentFieldIndex.value = -1
  currentField.value = null
  formSchema.value = [
    { label: '检测值', field: 'value', type: 'number', required: true, defaultValue: '', options: '' },
    { label: '检测日期', field: 'detectDate', type: 'date', required: true, defaultValue: '', options: '' },
    { label: '检测人员', field: 'operator', type: 'input', required: true, defaultValue: '', options: '' },
    { label: '仪器设备', field: 'equipment', type: 'input', required: false, defaultValue: '', options: '' },
    { label: '备注', field: 'remark', type: 'textarea', required: false, defaultValue: '', options: '' }
  ]
  schemaVisible.value = true
}

const selectField = (index) => {
  currentFieldIndex.value = index
  currentField.value = formSchema.value[index]
}

const addField = () => {
  formSchema.value.push({
    label: '新字段',
    field: 'newField',
    type: 'input',
    required: false,
    defaultValue: '',
    options: ''
  })
  currentFieldIndex.value = formSchema.value.length - 1
  currentField.value = formSchema.value[formSchema.value.length - 1]
}

const removeField = () => {
  if (currentFieldIndex.value >= 0) {
    formSchema.value.splice(currentFieldIndex.value, 1)
    currentFieldIndex.value = -1
    currentField.value = null
  }
}

const saveSchema = () => {
  ElMessage.success('表单配置保存成功')
  schemaVisible.value = false
}
</script>

<style lang="scss" scoped>
.detect-container {
  .search-bar {
    margin-bottom: 16px;
  }

  .toolbar {
    margin-bottom: 16px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .schema-config {
    display: flex;
    gap: 20px;
    min-height: 400px;

    .schema-list {
      width: 200px;
      border-right: 1px solid var(--el-border-color-lighter, #ebeef5);
      padding-right: 20px;

      .list-title {
        font-weight: 600;
        margin-bottom: 12px;
      }

      .schema-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 12px;
        margin-bottom: 6px;
        border-radius: 4px;
        cursor: pointer;
        background-color: var(--el-fill-color-light, #f5f7fa);

        &:hover, &.active {
          background-color: var(--el-color-primary-light-9, #ecf5ff);
          color: var(--el-color-primary, #409eff);
        }

        .field-label {
          font-size: 13px;
        }

        .field-type {
          font-size: 11px;
          color: #909399;
        }
      }
    }

    .schema-editor {
      flex: 1;

      .editor-title {
        font-weight: 600;
        margin-bottom: 12px;
      }
    }
  }
}
</style>
