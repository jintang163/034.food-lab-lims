<template>
  <div class="template-container">
    <el-card shadow="hover">
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="模板名称">
            <el-input v-model="searchForm.templateName" placeholder="请输入模板名称" clearable />
          </el-form-item>
          <el-form-item label="模板编码">
            <el-input v-model="searchForm.templateCode" placeholder="请输入模板编码" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择" clearable>
              <el-option label="草稿" value="draft" />
              <el-option label="已发布" value="published" />
              <el-option label="已停用" value="disabled" />
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
          新建模板
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
        <el-table-column prop="templateName" label="模板名称" min-width="150" />
        <el-table-column prop="templateCode" label="模板编码" width="150" />
        <el-table-column prop="version" label="版本号" width="100" />
        <el-table-column prop="fieldCount" label="字段数量" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleDesign(row)">
              <el-icon><Edit /></el-icon>
              设计
            </el-button>
            <el-button type="success" link size="small" @click="handleVersion(row)">
              <el-icon><Clock /></el-icon>
              版本
            </el-button>
            <el-button type="warning" link size="small" @click="handleData(row)">
              <el-icon><DataLine /></el-icon>
              数据
            </el-button>
            <el-button type="info" link size="small" @click="handlePreview(row)">
              <el-icon><View /></el-icon>
              预览
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="form.templateName" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板编码" prop="templateCode">
          <el-input v-model="form.templateCode" placeholder="请输入模板编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="表单预览" width="800px" destroy-on-close>
      <FormPreview v-if="currentTemplate" :schema="currentTemplate.schema" :readonly="true" />
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Delete, Edit, Clock, DataLine, View } from '@element-plus/icons-vue'
import FormPreview from '@/components/form/FormPreview.vue'
import { getTemplatePage, addTemplate, updateTemplate, deleteTemplate } from '@/api/form'

const router = useRouter()
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const previewVisible = ref(false)
const tableRef = ref(null)
const formRef = ref(null)
const selectedIds = ref([])
const currentTemplate = ref(null)

const searchForm = reactive({
  templateName: '',
  templateCode: '',
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: '',
  templateName: '',
  templateCode: '',
  description: ''
})

const rules = {
  templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  templateCode: [{ required: true, message: '请输入模板编码', trigger: 'blur' }]
}

const tableData = ref([
  { id: 1, templateName: '样品登记单', templateCode: 'sample_register', version: '1.2', fieldCount: 12, status: 'published', createTime: '2024-01-15 10:30:00', updateTime: '2024-01-20 14:20:00', description: '样品信息登记表单', schema: { fields: [] } },
  { id: 2, templateName: '检测结果录入', templateCode: 'detect_result', version: '1.0', fieldCount: 8, status: 'published', createTime: '2024-01-10 09:15:00', updateTime: '2024-01-10 09:15:00', description: '检测结果录入表单', schema: { fields: [] } },
  { id: 3, templateName: '设备校准记录', templateCode: 'equipment_calibration', version: '2.1', fieldCount: 15, status: 'draft', createTime: '2024-01-05 16:45:00', updateTime: '2024-01-18 11:30:00', description: '设备校准信息记录', schema: { fields: [] } },
  { id: 4, templateName: '环境监测记录', templateCode: 'environment_monitor', version: '1.0', fieldCount: 6, status: 'disabled', createTime: '2024-01-01 08:00:00', updateTime: '2024-01-08 10:00:00', description: '实验室环境监测', schema: { fields: [] } },
  { id: 5, templateName: '试剂验收单', templateCode: 'reagent_acceptance', version: '1.3', fieldCount: 10, status: 'published', createTime: '2024-01-12 13:20:00', updateTime: '2024-01-19 09:50:00', description: '试剂入库验收表单', schema: { fields: [] } }
])

pagination.total = 28

const getStatusType = (status) => {
  const map = {
    draft: 'info',
    published: 'success',
    disabled: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    draft: '草稿',
    published: '已发布',
    disabled: '已停用'
  }
  return map[status] || status
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

const handleReset = () => {
  searchForm.templateName = ''
  searchForm.templateCode = ''
  searchForm.status = ''
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新建模板'
  Object.assign(form, {
    id: '',
    templateName: '',
    templateCode: '',
    description: ''
  })
  dialogVisible.value = true
}

const handleDesign = (row) => {
  router.push({ path: '/form/template/designer', query: { id: row.id } })
}

const handleVersion = (row) => {
  router.push({ path: '/form/version', query: { templateId: row.id, templateName: row.templateName } })
}

const handleData = (row) => {
  router.push({ path: '/form/data', query: { templateId: row.id, templateName: row.templateName } })
}

const handlePreview = (row) => {
  currentTemplate.value = row
  previewVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该模板吗？此操作不可恢复', '提示', {
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
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条模板吗？`, '提示', {
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

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  if (isEdit.value) {
    const index = tableData.value.findIndex(item => item.id === form.id)
    if (index > -1) {
      tableData.value[index] = { ...tableData.value[index], ...form }
    }
    ElMessage.success('更新成功')
  } else {
    const newId = Math.max(...tableData.value.map(item => item.id)) + 1
    tableData.value.unshift({
      ...form,
      id: newId,
      version: '1.0',
      fieldCount: 0,
      status: 'draft',
      createTime: new Date().toLocaleString(),
      updateTime: new Date().toLocaleString(),
      schema: { fields: [] }
    })
    pagination.total++
    ElMessage.success('创建成功')
  }
  dialogVisible.value = false
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
}
</script>

<style lang="scss" scoped>
.template-container {
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
}
</style>
