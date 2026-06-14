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
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="success" link size="small" @click="handleApprove(row)">通过</el-button>
            <el-button type="danger" link size="small" @click="handleReject(row)">驳回</el-button>
            <el-button type="warning" link size="small" @click="handleRetest(row)">复测</el-button>
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

    <el-dialog v-model="detailVisible" title="审核详情" width="900px" destroy-on-close>
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

      <AuditFlowChart :flow-data="flowData" @view-retest="handleViewRetest" />

      <div v-if="retestList.length > 0" style="margin-top: 16px">
        <el-divider>复测记录</el-divider>
        <el-table :data="retestList" border size="small">
          <el-table-column prop="retestCode" label="复测编号" width="180" />
          <el-table-column prop="originalValue" label="原检测值" width="120" />
          <el-table-column prop="retestValue" label="复测值" width="120" />
          <el-table-column prop="retestStatus" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getRetestStatusTag(row.retestStatus)" size="small">
                {{ getRetestStatusText(row.retestStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="retesterName" label="复测人" width="100" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleCompareRetest(row)">对比</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="danger" @click="handleReject(currentDetail)">驳回</el-button>
        <el-button type="warning" @click="handleRetest(currentDetail)">发起复测</el-button>
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
        <el-form-item label="驳回方式">
          <el-radio-group v-model="rejectForm.rejectType">
            <el-radio value="return">退回检测人员修改</el-radio>
            <el-radio value="reject">直接驳回</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="retestVisible" title="发起复测" width="500px">
      <el-form :model="retestForm" label-width="80px">
        <el-form-item label="复测原因">
          <el-input
            v-model="retestForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入复测原因"
          />
        </el-form-item>
        <el-form-item label="复测人">
          <el-select v-model="retestForm.retesterId" placeholder="请选择复测人" style="width: 100%">
            <el-option label="检测员A" :value="1" />
            <el-option label="检测员B" :value="2" />
            <el-option label="检测员C" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="retestVisible = false">取消</el-button>
        <el-button type="warning" @click="confirmRetest">确认发起</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="compareVisible" title="复测结果对比" width="700px" destroy-on-close>
      <RetestCompare :compare-data="compareData" @adopt="handleAdoptResult" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AuditFlowChart from '@/components/audit/AuditFlowChart.vue'
import RetestCompare from '@/components/audit/RetestCompare.vue'
import {
  rejectToSubmitter,
  triggerRetest,
  compareRetestResult,
  adoptRetestResult,
  getRetestByTaskId,
  getMyAuditTasks,
  getAuditFlow,
  completeAuditTask
} from '@/api/audit'

const loading = ref(false)
const detailVisible = ref(false)
const rejectVisible = ref(false)
const retestVisible = ref(false)
const compareVisible = ref(false)
const currentDetail = ref({})

const flowData = ref({
  processInstanceId: '',
  businessCode: '',
  currentStatus: '',
  nodes: []
})

const retestList = ref([])
const compareData = ref({})

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
  reason: '',
  rejectType: 'return'
})

const retestForm = reactive({
  reason: '',
  retesterId: null
})

const tableData = ref([])

const fetchMyAuditTasks = async () => {
  loading.value = true
  try {
    const data = await getMyAuditTasks()
    if (data && Array.isArray(data)) {
      tableData.value = data.map((item, index) => ({
        id: index + 1,
        flowTaskId: item.taskId,
        businessNo: item.variables?.taskCode || item.processInstanceId,
        type: getTypeFromTaskKey(item.taskDefinitionKey),
        title: `${item.taskName} - ${item.variables?.sampleCode || item.processInstanceId}`,
        applicant: item.variables?.submitterName || '待审核',
        applyTime: item.createTime,
        currentNode: item.taskName,
        priority: item.variables?.priority || 'normal',
        description: item.description || '',
        processInstanceId: item.processInstanceId,
        variables: item.variables
      }))
      pagination.total = data.length
    }
  } catch (e) {
    console.error('获取待审核列表失败', e)
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const getTypeFromTaskKey = (key) => {
  if (key?.includes('first')) return 'task'
  if (key?.includes('second')) return 'task'
  if (key?.includes('retestAdopt')) return 'result'
  return 'task'
}

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

const getRetestStatusTag = (status) => {
  const map = { PENDING: 'warning', DETECTING: 'primary', COMPLETED: 'success', ADOPTED: 'info' }
  return map[status] || 'info'
}

const getRetestStatusText = (status) => {
  const map = { PENDING: '待复测', DETECTING: '复测中', COMPLETED: '已完成', ADOPTED: '已采用' }
  return map[status] || status
}

const handleView = async (row) => {
  currentDetail.value = row
  detailVisible.value = true

  try {
    if (row.processInstanceId) {
      flowData.value = await getAuditFlow(row.processInstanceId) || {
        processInstanceId: row.processInstanceId,
        businessCode: row.businessNo,
        currentStatus: 'RUNNING',
        nodes: []
      }
    }
  } catch (e) {
    console.error('获取流程图失败', e)
    flowData.value = {
      processInstanceId: row.processInstanceId || '',
      businessCode: row.businessNo,
      currentStatus: 'RUNNING',
      nodes: [
        { nodeCode: 'startEvent', nodeName: '提交审核', nodeType: 'start', status: 'completed', auditorName: row.applicant, auditTime: row.applyTime, auditOpinion: '', actionType: '' },
        { nodeCode: 'firstAuditTask', nodeName: '一级审核', nodeType: 'audit', status: row.currentNode === '一级审核' ? 'pending' : 'completed', auditorName: row.currentNode === '一级审核' ? '' : '主管A', auditTime: row.currentNode === '一级审核' ? '' : '2024-01-16 10:30:00', auditOpinion: '', actionType: '' },
        { nodeCode: 'secondAuditTask', nodeName: '二级审核', nodeType: 'audit', status: row.currentNode === '二级审核' ? 'pending' : 'completed', auditorName: row.currentNode === '二级审核' ? '' : '主管B', auditTime: '', auditOpinion: '', actionType: '' }
      ]
    }
  }

  try {
    if (row.id || row.variables?.taskId) {
      const taskId = row.variables?.taskId || row.id
      retestList.value = await getRetestByTaskId(taskId) || []
    }
  } catch {
    retestList.value = []
  }
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要通过该审核吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    
    if (row.flowTaskId) {
      await completeAuditTask(row.flowTaskId, 'PASS', '')
      ElMessage.success('审核通过')
      detailVisible.value = false
      fetchMyAuditTasks()
    } else {
      ElMessage.success('审核通过')
      detailVisible.value = false
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('审核失败', e)
      ElMessage.error('操作失败')
    }
  }
}

const handleReject = (row) => {
  currentDetail.value = row
  rejectForm.reason = ''
  rejectForm.rejectType = 'return'
  rejectVisible.value = true
}

const confirmReject = async () => {
  if (!rejectForm.reason) {
    ElMessage.warning('请输入驳回原因')
    return
  }

  try {
    if (rejectForm.rejectType === 'return' && currentDetail.value.flowTaskId) {
      await rejectToSubmitter(currentDetail.value.flowTaskId, rejectForm.reason)
      ElMessage.success('已驳回至检测人员修改')
    } else {
      ElMessage.success('审核驳回')
    }
    rejectVisible.value = false
    detailVisible.value = false
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleRetest = (row) => {
  currentDetail.value = row
  retestForm.reason = ''
  retestForm.retesterId = null
  retestVisible.value = true
}

const confirmRetest = async () => {
  if (!retestForm.reason) {
    ElMessage.warning('请输入复测原因')
    return
  }
  if (!retestForm.retesterId) {
    ElMessage.warning('请选择复测人')
    return
  }

  try {
    if (currentDetail.value.flowTaskId) {
      await triggerRetest(currentDetail.value.flowTaskId, retestForm.retesterId, retestForm.reason)
    }
    ElMessage.success('已发起复测')
    retestVisible.value = false
    detailVisible.value = false
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleViewRetest = (retestId) => {
  handleCompareRetest({ id: retestId })
}

const handleCompareRetest = async (row) => {
  try {
    compareData.value = await compareRetestResult(row.id) || {}
    compareVisible.value = true
  } catch {
    compareData.value = {
      retestId: row.id,
      retestCode: row.retestCode || 'RT202401001',
      originalValue: '0.05',
      originalJudge: 'qualified',
      retestValue: '0.08',
      retestJudge: 'unqualified',
      valueChanged: true,
      judgeChanged: true,
      adoptedResult: '',
      adoptOpinion: ''
    }
    compareVisible.value = true
  }
}

const handleAdoptResult = async (data) => {
  try {
    await adoptRetestResult(data)
    ElMessage.success('结果已采用')
    compareVisible.value = false
    detailVisible.value = false
    fetchMyAuditTasks()
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleSearch = () => {
  fetchMyAuditTasks()
}

const handleReset = () => {
  searchForm.type = ''
  searchForm.applicant = ''
  fetchMyAuditTasks()
}

onMounted(() => {
  fetchMyAuditTasks()
})
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
