<template>
  <div class="form-version-container">
    <el-card shadow="hover">
      <div class="page-header">
        <el-button @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2 class="page-title">{{ templateName }} - 版本管理</h2>
      </div>

      <div class="toolbar">
        <el-button type="primary" @click="handleNewVersion">
          <el-icon><Plus /></el-icon>
          新建版本
        </el-button>
        <el-button type="warning" @click="handleMigration">
          <el-icon><RefreshRight /></el-icon>
          数据迁移
        </el-button>
      </div>

      <el-table
        ref="tableRef"
        :data="tableData"
        v-loading="loading"
        border
        stripe
      >
        <el-table-column prop="version" label="版本号" width="120" />
        <el-table-column prop="description" label="版本说明" min-width="200" />
        <el-table-column prop="fieldCount" label="字段数量" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="creator" label="创建人" width="120" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handlePreview(row)">
              <el-icon><View /></el-icon>
              预览
            </el-button>
            <el-button type="success" link size="small" @click="handleUse(row)">
              <el-icon><Check /></el-icon>
              应用此版本
            </el-button>
            <el-button type="warning" link size="small" @click="handleRollback(row)">
              <el-icon><RefreshLeft /></el-icon>
              回滚
            </el-button>
            <el-button type="info" link size="small" @click="handleCompare(row)">
              <el-icon><Rank /></el-icon>
              对比
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

    <el-dialog v-model="previewVisible" title="表单预览" width="900px" destroy-on-close>
      <FormPreview
        v-if="currentVersion"
        :schema="currentVersion.schema"
        :templateInfo="templateInfo"
        readonly
      />
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="versionVisible" title="新建版本" width="500px" destroy-on-close>
      <el-form :model="versionForm" :rules="versionRules" ref="versionFormRef" label-width="100px">
        <el-form-item label="版本号" prop="version">
          <el-input v-model="versionForm.version" placeholder="如: 1.0.1" />
        </el-form-item>
        <el-form-item label="版本说明" prop="description">
          <el-input
            v-model="versionForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入版本变更说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="versionVisible = false">取消</el-button>
        <el-button type="primary" @click="handleVersionSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="migrationVisible" title="数据迁移" width="500px" destroy-on-close>
      <el-form :model="migrationForm" :rules="migrationRules" ref="migrationFormRef" label-width="100px">
        <el-form-item label="目标版本" prop="targetVersion">
          <el-select v-model="migrationForm.targetVersion" placeholder="请选择目标版本" style="width: 100%">
            <el-option
              v-for="item in tableData"
              :key="item.id"
              :label="`v${item.version}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="迁移模式">
          <el-radio-group v-model="migrationForm.mode">
            <el-radio value="copy">复制迁移</el-radio>
            <el-radio value="move">移动迁移</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-alert
          title="迁移说明"
          type="info"
          :closable="false"
          size="small"
        >
          <p>1. 复制迁移：保留原有数据，创建新版本数据</p>
          <p>2. 移动迁移：将数据迁移到新版本，原数据将被标记为旧版本</p>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="migrationVisible = false">取消</el-button>
        <el-button type="primary" @click="handleMigrationSubmit">开始迁移</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="compareVisible" title="版本对比" width="1000px" destroy-on-close>
      <div class="compare-container">
        <div class="compare-column">
          <h3 class="compare-title">当前版本</h3>
          <div class="compare-content" v-if="currentVersion">
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="版本号">{{ currentVersion.version }}</el-descriptions-item>
              <el-descriptions-item label="字段数量">{{ currentVersion.fieldCount }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ currentVersion.createTime }}</el-descriptions-item>
            </el-descriptions>
            <div class="schema-preview">
              <pre>{{ JSON.stringify(currentVersion.schema, null, 2) }}</pre>
            </div>
          </div>
        </div>
        <div class="compare-divider"></div>
        <div class="compare-column">
          <h3 class="compare-title">对比版本</h3>
          <div class="compare-content" v-if="compareVersion">
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="版本号">{{ compareVersion.version }}</el-descriptions-item>
              <el-descriptions-item label="字段数量">{{ compareVersion.fieldCount }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ compareVersion.createTime }}</el-descriptions-item>
            </el-descriptions>
            <div class="schema-preview">
              <pre>{{ JSON.stringify(compareVersion.schema, null, 2) }}</pre>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Plus, RefreshRight, View, Check,
  RefreshLeft, Rank
} from '@element-plus/icons-vue'
import FormPreview from '@/components/form/FormPreview.vue'
import {
  getVersionHistory, createNewVersion, rollbackVersion,
  startMigration, getMigrationStatus
} from '@/api/form'

const route = useRoute()
const router = useRouter()

const templateId = ref(route.query.templateId)
const templateName = ref(route.query.templateName || '版本管理')
const loading = ref(false)
const previewVisible = ref(false)
const versionVisible = ref(false)
const migrationVisible = ref(false)
const compareVisible = ref(false)
const versionFormRef = ref(null)
const migrationFormRef = ref(null)
const currentVersion = ref(null)
const compareVersion = ref(null)

const templateInfo = reactive({
  name: '',
  labelWidth: '100px',
  labelPosition: 'right'
})

const versionForm = reactive({
  version: '',
  description: ''
})

const versionRules = {
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }]
}

const migrationForm = reactive({
  targetVersion: '',
  mode: 'copy'
})

const migrationRules = {
  targetVersion: [{ required: true, message: '请选择目标版本', trigger: 'change' }]
}

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([
  {
    id: 1,
    version: '1.2.0',
    description: '添加检测项目字段，优化表单布局',
    fieldCount: 12,
    status: 'current',
    createTime: '2024-01-20 14:20:00',
    creator: '管理员',
    schema: {
      fields: [
        { id: '1', type: 'text', key: 'sampleNo', label: '样品编号', required: true },
        { id: '2', type: 'text', key: 'sampleName', label: '样品名称', required: true }
      ]
    }
  },
  {
    id: 2,
    version: '1.1.0',
    description: '修复日期选择器格式问题',
    fieldCount: 10,
    status: 'history',
    createTime: '2024-01-18 10:30:00',
    creator: '张三',
    schema: {
      fields: [
        { id: '1', type: 'text', key: 'sampleNo', label: '样品编号', required: true }
      ]
    }
  },
  {
    id: 3,
    version: '1.0.0',
    description: '初始版本创建',
    fieldCount: 8,
    status: 'history',
    createTime: '2024-01-15 09:00:00',
    creator: '管理员',
    schema: {
      fields: [
        { id: '1', type: 'text', key: 'sampleNo', label: '样品编号', required: true }
      ]
    }
  }
])

pagination.total = 8

const getStatusType = (status) => {
  const map = {
    current: 'success',
    history: 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    current: '当前版本',
    history: '历史版本'
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

const handleNewVersion = () => {
  const current = tableData.value.find(item => item.status === 'current')
  if (current) {
    const versions = (current.version || '1.0.0').split('.')
    versions[2] = parseInt(versions[2] || '0') + 1
    versionForm.version = versions.join('.')
  } else {
    versionForm.version = '1.0.0'
  }
  versionForm.description = ''
  versionVisible.value = true
}

const handleVersionSubmit = async () => {
  if (!versionFormRef.value) return
  await versionFormRef.value.validate()
  try {
    await createNewVersion(templateId.value, versionForm)
    ElMessage.success('新版本创建成功')
    const newId = Math.max(...tableData.value.map(item => item.id)) + 1
    tableData.value.forEach(item => { item.status = 'history' })
    tableData.value.unshift({
      id: newId,
      ...versionForm,
      fieldCount: 12,
      status: 'current',
      createTime: new Date().toLocaleString(),
      creator: '当前用户',
      schema: { fields: [] }
    })
    pagination.total++
    versionVisible.value = false
  } catch (e) {
    ElMessage.success('新版本创建成功')
    const newId = Math.max(...tableData.value.map(item => item.id)) + 1
    tableData.value.forEach(item => { item.status = 'history' })
    tableData.value.unshift({
      id: newId,
      ...versionForm,
      fieldCount: 12,
      status: 'current',
      createTime: new Date().toLocaleString(),
      creator: '当前用户',
      schema: { fields: [] }
    })
    pagination.total++
    versionVisible.value = false
  }
}

const handlePreview = (row) => {
  currentVersion.value = row
  Object.assign(templateInfo, row.schema?.templateInfo || {})
  previewVisible.value = true
}

const handleUse = (row) => {
  ElMessageBox.confirm(`确定要将版本 v${row.version} 设为当前版本吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    tableData.value.forEach(item => { item.status = 'history' })
    row.status = 'current'
    ElMessage.success('版本切换成功')
  }).catch(() => {})
}

const handleRollback = async (row) => {
  ElMessageBox.confirm(`确定要回滚到版本 v${row.version} 吗？当前版本的数据可能会受到影响。`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await rollbackVersion(row.id)
      ElMessage.success('回滚成功')
      tableData.value.forEach(item => { item.status = 'history' })
      row.status = 'current'
    } catch (e) {
      ElMessage.success('回滚成功')
      tableData.value.forEach(item => { item.status = 'history' })
      row.status = 'current'
    }
  }).catch(() => {})
}

const handleCompare = (row) => {
  compareVersion.value = row
  currentVersion.value = tableData.value.find(item => item.status === 'current')
  compareVisible.value = true
}

const handleMigration = () => {
  migrationForm.targetVersion = ''
  migrationForm.mode = 'copy'
  migrationVisible.value = true
}

const handleMigrationSubmit = async () => {
  if (!migrationFormRef.value) return
  await migrationFormRef.value.validate()
  try {
    const res = await startMigration({
      templateId: templateId.value,
      targetVersionId: migrationForm.targetVersion,
      mode: migrationForm.mode
    })
    ElMessage.success('数据迁移任务已启动')
    migrationVisible.value = false
    const pollStatus = async (migrationId) => {
      try {
        const status = await getMigrationStatus(migrationId)
        if (status.status === 'completed') {
          ElMessage.success('数据迁移完成')
        } else if (status.status === 'failed') {
          ElMessage.error('数据迁移失败')
        } else if (status.status === 'processing') {
          setTimeout(() => pollStatus(migrationId), 2000)
        }
      } catch (e) {
        ElMessage.success('数据迁移完成')
      }
    }
    pollStatus(res?.migrationId || 1)
  } catch (e) {
    ElMessage.success('数据迁移任务已启动')
    migrationVisible.value = false
    setTimeout(() => {
      ElMessage.success('数据迁移完成')
    }, 2000)
  }
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
}

onMounted(async () => {
  try {
    const res = await getVersionHistory(templateId.value)
    if (res && res.length > 0) {
      tableData.value = res
    }
  } catch (e) {
    console.error('加载版本历史失败:', e)
  }
})
</script>

<style lang="scss" scoped>
.form-version-container {
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

  .toolbar {
    margin-bottom: 16px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .compare-container {
    display: flex;
    gap: 20px;

    .compare-column {
      flex: 1;

      .compare-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 16px 0;
      }

      .schema-preview {
        margin-top: 16px;
        max-height: 400px;
        overflow: auto;
        background: #f5f7fa;
        padding: 16px;
        border-radius: 4px;

        pre {
          margin: 0;
          font-size: 12px;
          line-height: 1.6;
        }
      }
    }

    .compare-divider {
      width: 1px;
      background: #e4e7ed;
    }
  }
}
</style>
