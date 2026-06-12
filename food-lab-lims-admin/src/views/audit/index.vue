<template>
  <div class="audit-container">
    <el-card shadow="hover">
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="审核类型">
            <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 150px">
              <el-option label="样品审核" value="sample" />
              <el-option label="任务审核" value="task" />
              <el-option label="报告审核" value="report" />
              <el-option label="结果审核" value="result" />
            </el-select>
          </el-form-item>
          <el-form-item label="申请人">
            <el-input v-model="searchForm.applicant" placeholder="请输入" clearable style="width: 120px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
      >
        <el-table-column prop="businessNo" label="业务编号" width="160" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)" size="small">{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="applicant" label="申请人" width="100" />
        <el-table-column prop="applyTime" label="申请时间" width="160" />
        <el-table-column prop="currentNode" label="当前节点" width="120" />
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="getPriorityTag(row.priority)" size="small">{{ getPriorityText(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="success" link size="small" @click="handleApprove(row)">通过</el-button>
            <el-button type="danger" link size="small" @click="handleReject(row)">驳回</el-button>
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

    <el-dialog v-model="detailVisible" title="审核详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="业务编号">{{ currentDetail.businessNo }}</el-descriptions-item>
        <el-descriptions-item label="审核类型">{{ getTypeText(currentDetail.type) }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ currentDetail.title }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentDetail.applicant }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentDetail.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="当前节点">{{ currentDetail.currentNode }}</el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="getPriorityTag(currentDetail.priority)" size="small">
            {{ getPriorityText(currentDetail.priority) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag type="warning" size="small">待审核</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请说明" :span="2">{{ currentDetail.description || '无' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>审核流程</el-divider>

      <el-steps :active="2" direction="vertical" finish-status="success">
        <el-step title="提交申请">
          <template #description>
            <p>{{ currentDetail.applicant }} 提交申请</p>
            <p style="color: #909399; font-size: 12px">{{ currentDetail.applyTime }}</p>
          </template>
        </el-step>
        <el-step title="一级审核">
          <template #description>
            <p>张三 审核通过</p>
            <p style="color: #909399; font-size: 12px">2024-01-16 10:30:00</p>
          </template>
        </el-step>
        <el-step title="二级审核">
          <template #description>
            <p>等待审核中...</p>
          </template>
        </el-step>
      </el-steps>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="danger" @click="handleReject(currentDetail)">驳回</el-button>
        <el-button type="success" @click="handleApprove(currentDetail)">通过</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectVisible" title="驳回原因" width="500px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="驳回原因">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入驳回原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const detailVisible = ref(false)
const rejectVisible = ref(false)
const currentDetail = ref({})

const searchForm = reactive({
  type: '',
  applicant: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const rejectForm = reactive({
  reason: ''
})

const tableData = ref([
  { id: 1, businessNo: 'SP202401001', type: 'sample', title: '牛奶样品-001 检测申请', applicant: '张三', applyTime: '2024-01-15 09:30:00', currentNode: '一级审核', priority: 'normal', description: '常规牛奶检测项目' },
  { id: 2, businessNo: 'TK202401002', type: 'task', title: '猪肉样品-002 任务分配', applicant: '李四', applyTime: '2024-01-16 10:15:00', currentNode: '二级审核', priority: 'high', description: '紧急检测任务，需要优先处理' },
  { id: 3, businessNo: 'RP202401003', type: 'report', title: '蔬菜样品-003 报告出具', applicant: '王五', applyTime: '2024-01-14 08:45:00', currentNode: '一级审核', priority: 'normal', description: '蔬菜农残检测报告' },
  { id: 4, businessNo: 'RT202401004', type: 'result', title: '饮用水-004 结果审核', applicant: '赵六', applyTime: '2024-01-13 14:20:00', currentNode: '二级审核', priority: 'low', description: '' },
  { id: 5, businessNo: 'SP202401005', type: 'sample', title: '食用油-005 样品登记', applicant: '张三', applyTime: '2024-01-17 11:00:00', currentNode: '一级审核', priority: 'normal', description: '' },
  { id: 6, businessNo: 'TK202401006', type: 'task', title: '大米-006 任务分配', applicant: '李四', applyTime: '2024-01-18 09:10:00', currentNode: '一级审核', priority: 'high', description: '重金属专项检测任务' },
  { id: 7, businessNo: 'RP202401007', type: 'report', title: '鸡蛋-007 报告审核', applicant: '王五', applyTime: '2024-01-12 15:30:00', currentNode: '二级审核', priority: 'normal', description: '' },
  { id: 8, businessNo: 'RT202401008', type: 'result', title: '水果-008 结果审核', applicant: '赵六', applyTime: '2024-01-11 10:45:00', currentNode: '一级审核', priority: 'normal', description: '' }
])

pagination.total = 24

const getTypeText = (type) => {
  const map = { sample: '样品审核', task: '任务审核', report: '报告审核', result: '结果审核' }
  return map[type] || type
}

const getTypeTag = (type) => {
  const map = { sample: 'primary', task: 'success', report: 'warning', result: 'info' }
  return map[type] || 'info'
}

const getPriorityText = (priority) => {
  const map = { high: '高', normal: '中', low: '低' }
  return map[priority] || priority
}

const getPriorityTag = (priority) => {
  const map = { high: 'danger', normal: 'warning', low: 'info' }
  return map[priority] || 'info'
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => { loading.value = false }, 500)
}

const handleReset = () => {
  searchForm.type = ''
  searchForm.applicant = ''
  handleSearch()
}

const handleView = (row) => {
  currentDetail.value = row
  detailVisible.value = true
}

const handleApprove = (row) => {
  ElMessageBox.confirm('确定要通过该审核吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(() => {
    ElMessage.success('审核通过')
    detailVisible.value = false
  }).catch(() => {})
}

const handleReject = (row) => {
  currentDetail.value = row
  rejectForm.reason = ''
  rejectVisible.value = true
}

const confirmReject = () => {
  if (!rejectForm.reason) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  ElMessage.success('驳回成功')
  rejectVisible.value = false
  detailVisible.value = false
}
</script>

<style lang="scss" scoped>
.audit-container {
  .search-bar {
    margin-bottom: 16px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
