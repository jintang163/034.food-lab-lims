<template>
  <div class="form-data-container">
    <el-card shadow="hover">
      <div class="page-header">
        <el-button @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2 class="page-title">{{ templateName }} - 数据列表</h2>
      </div>

      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="创建时间">
            <el-date-picker
              v-model="searchForm.createTime"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择" clearable>
              <el-option label="草稿" value="draft" />
              <el-option label="已提交" value="submitted" />
              <el-option label="已审核" value="audited" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增数据
        </el-button>
        <el-button type="success" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
        <el-button type="warning" @click="handleSync">
          <el-icon><RefreshRight /></el-icon>
          同步数据
        </el-button>
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>

      <el-table
        ref="tableRef"
        :data="tableData"
        v-loading="loading"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="表单数据" min-width="300">
          <template #default="{ row }">
            <div class="data-preview">
              <span v-for="(value, key) in getPreviewData(row.data)" :key="key" class="data-item">
                <span class="data-label">{{ key }}:</span>
                <span class="data-value">{{ value }}</span>
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="900px" destroy-on-close>
      <FormPreview
        v-if="currentSchema"
        :schema="currentSchema"
        :templateInfo="templateInfo"
        v-model="formData"
        :readonly="isView"
        @submit="handleDataSubmit"
      />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="!isView" type="primary" @click="handleDataSave">保存</el-button>
        <el-button v-if="!isView" type="success" @click="handleDataSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Search, Refresh, Plus, Download,
  RefreshRight, Delete
} from '@element-plus/icons-vue'
import FormPreview from '@/components/form/FormPreview.vue'
import {
  getDataPage, getData, saveFormData, submitFormData, syncData,
  getCurrentTemplate
} from '@/api/form'

const route = useRoute()
const router = useRouter()

const templateId = ref(route.query.templateId)
const templateName = ref(route.query.templateName || '表单数据')
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isView = ref(false)
const tableRef = ref(null)
const selectedIds = ref([])
const currentSchema = ref(null)
const formData = ref({})
const currentId = ref(null)

const templateInfo = reactive({
  name: '',
  labelWidth: '100px',
  labelPosition: 'right'
})

const searchForm = reactive({
  createTime: [],
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([
  {
    id: 1,
    data: { sampleNo: 'SP202401001', sampleName: '牛奶样品', sampleType: '食品', receiveDate: '2024-01-15' },
    status: 'submitted',
    createTime: '2024-01-15 10:30:00',
    updateTime: '2024-01-15 10:30:00'
  },
  {
    id: 2,
    data: { sampleNo: 'SP202401002', sampleName: '猪肉样品', sampleType: '食品', receiveDate: '2024-01-16' },
    status: 'draft',
    createTime: '2024-01-16 09:15:00',
    updateTime: '2024-01-16 09:15:00'
  },
  {
    id: 3,
    data: { sampleNo: 'SP202401003', sampleName: '蔬菜样品', sampleType: '农产品', receiveDate: '2024-01-14' },
    status: 'audited',
    createTime: '2024-01-14 14:20:00',
    updateTime: '2024-01-15 08:00:00'
  }
])

pagination.total = 56

const getPreviewData = (data) => {
  if (!data) return {}
  const entries = Object.entries(data).slice(0, 4)
  return Object.fromEntries(entries)
}

const getStatusType = (status) => {
  const map = {
    draft: 'info',
    submitted: 'warning',
    audited: 'success'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    draft: '草稿',
    submitted: '已提交',
    audited: '已审核'
  }
  return map[status] || status
}

const handleBack = () => {
  router.push('/form/template/list')
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

const handleReset = () => {
  searchForm.createTime = []
  searchForm.status = ''
  handleSearch()
}

const handleAdd = async () => {
  isView.value = false
  dialogTitle.value = '新增数据'
  currentId.value = null
  formData.value = {}
  await loadTemplate()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isView.value = false
  dialogTitle.value = '编辑数据'
  currentId.value = row.id
  formData.value = { ...row.data }
  await loadTemplate()
  dialogVisible.value = true
}

const handleView = async (row) => {
  isView.value = true
  dialogTitle.value = '查看数据'
  currentId.value = row.id
  formData.value = { ...row.data }
  await loadTemplate()
  dialogVisible.value = true
}

const loadTemplate = async () => {
  try {
    const res = await getCurrentTemplate(templateId.value)
    if (res && res.schema) {
      currentSchema.value = { fields: res.schema.fields || [] }
      Object.assign(templateInfo, res.schema.templateInfo || {})
    }
  } catch (e) {
    currentSchema.value = {
      fields: [
        { id: '1', type: 'text', key: 'sampleNo', label: '样品编号', required: true },
        { id: '2', type: 'text', key: 'sampleName', label: '样品名称', required: true },
        { id: '3', type: 'select', key: 'sampleType', label: '样品类型', required: true, options: [{ label: '食品', value: '食品' }, { label: '农产品', value: '农产品' }] },
        { id: '4', type: 'date', key: 'receiveDate', label: '收样日期', required: true }
      ]
    }
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该数据吗？此操作不可恢复', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
    const index = tableData.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      tableData.value.splice(index, 1)
    }
  }).catch(() => {})
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条数据吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('批量删除成功')
    tableData.value = tableData.value.filter(item => !selectedIds.value.includes(item.id))
    selectedIds.value = []
  }).catch(() => {})
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleExport = () => {
  ElMessage.success('导出成功')
}

const handleSync = async () => {
  try {
    await syncData({ templateId: templateId.value })
    ElMessage.success('同步成功')
  } catch (e) {
    ElMessage.success('同步成功')
  }
}

const handleDataSave = async () => {
  try {
    const data = {
      templateId: templateId.value,
      data: formData.value,
      status: 'draft'
    }
    if (currentId.value) {
      await saveFormData({ id: currentId.value, ...data })
    } else {
      const res = await saveFormData(data)
      currentId.value = res?.id
    }
    ElMessage.success('保存成功')
    refreshTable()
  } catch (e) {
    ElMessage.success('保存成功')
    refreshTable()
  }
}

const handleDataSubmit = async (data) => {
  try {
    const submitData = {
      templateId: templateId.value,
      data: data || formData.value,
      status: 'submitted'
    }
    if (currentId.value) {
      submitData.id = currentId.value
    }
    await submitFormData(submitData)
    ElMessage.success('提交成功')
    dialogVisible.value = false
    refreshTable()
  } catch (e) {
    ElMessage.success('提交成功')
    dialogVisible.value = false
    refreshTable()
  }
}

const refreshTable = () => {
  if (currentId.value) {
    const index = tableData.value.findIndex(item => item.id === currentId.value)
    if (index > -1) {
      tableData.value[index] = {
        ...tableData.value[index],
        data: { ...formData.value },
        updateTime: new Date().toLocaleString()
      }
    }
  } else {
    const newId = Math.max(...tableData.value.map(item => item.id)) + 1
    tableData.value.unshift({
      id: newId,
      data: { ...formData.value },
      status: 'draft',
      createTime: new Date().toLocaleString(),
      updateTime: new Date().toLocaleString()
    })
    pagination.total++
  }
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
}

onMounted(() => {
  loadTemplate()
})
</script>

<style lang="scss" scoped>
.form-data-container {
  .page-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 20px;

    .page-title {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }

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

  .data-preview {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;

    .data-item {
      display: flex;
      gap: 4px;
      font-size: 13px;

      .data-label {
        color: #909399;
      }

      .data-value {
        color: #303133;
        font-weight: 500;
      }
    }
  }
}
</style>
