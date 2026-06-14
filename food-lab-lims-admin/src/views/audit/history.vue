<template>
  <div class="audit-history">
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
          <el-form-item label="审核结果">
            <el-select v-model="searchForm.result" placeholder="请选择" clearable style="width: 120px">
              <el-option label="通过" value="approved" />
              <el-option label="驳回" value="rejected" />
              <el-option label="复测" value="retest" />
            </el-select>
          </el-form-item>
          <el-form-item label="审核人">
            <el-input v-model="searchForm.auditor" placeholder="请输入" clearable style="width: 120px" />
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
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="applicant" label="申请人" width="100" />
        <el-table-column prop="auditor" label="审核人" width="100" />
        <el-table-column prop="result" label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="getResultTagType(row.result)" size="small">
              {{ getResultText(row.result) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditTime" label="审核时间" width="160" />
        <el-table-column prop="auditOpinion" label="审核意见" min-width="150">
          <template #default="{ row }">
            <span :title="row.auditOpinion">{{ row.auditOpinion || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
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

    <el-dialog v-model="detailVisible" title="审核历史详情" width="900px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="业务编号">{{ currentDetail.businessNo }}</el-descriptions-item>
        <el-descriptions-item label="审核类型">{{ getTypeText(currentDetail.type) }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ currentDetail.title }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentDetail.applicant }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ currentDetail.auditor }}</el-descriptions-item>
        <el-descriptions-item label="审核结果">
          <el-tag :type="getResultTagType(currentDetail.result)" size="small">
            {{ getResultText(currentDetail.result) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间" :span="2">{{ currentDetail.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="审核时间" :span="2">{{ currentDetail.auditTime }}</el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">{{ currentDetail.auditOpinion || '无' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>审核流程</el-divider>

      <AuditFlowChart :flow-data="flowData" @view-retest="handleViewRetest" />

      <div v-if="opinionList.length > 0" style="margin-top: 16px">
        <el-divider>历史审核意见</el-divider>
        <el-timeline>
          <el-timeline-item
            v-for="(opinion, index) in opinionList"
            :key="index"
            :timestamp="opinion.time"
            :type="getOpinionTimelineType(opinion.result)"
            placement="top"
          >
            <el-card shadow="never" class="opinion-card">
              <div class="opinion-header">
                <span class="opinion-auditor">{{ opinion.auditor }}</span>
                <el-tag :type="getOpinionTagType(opinion.result)" size="small">
                  {{ opinion.level }} - {{ getResultText(opinion.result) }}
                </el-tag>
              </div>
              <div class="opinion-content">{{ opinion.opinion || '无审核意见' }}</div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="compareVisible" title="复测结果对比" width="700px" destroy-on-close>
      <RetestCompare :compare-data="compareData" @adopt="handleAdoptResult" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import AuditFlowChart from '@/components/audit/AuditFlowChart.vue'
import RetestCompare from '@/components/audit/RetestCompare.vue'
import { getAuditFlow, compareRetestResult, adoptRetestResult, getAuditHistoryList } from '@/api/audit'

const loading = ref(false)
const detailVisible = ref(false)
const compareVisible = ref(false)
const currentDetail = ref({})

const flowData = ref({
  processInstanceId: '',
  businessCode: '',
  currentStatus: '',
  nodes: []
})

const opinionList = ref([])
const compareData = ref({})

const searchForm = reactive({
  type: '',
  result: '',
  auditor: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])

const fetchHistoryList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      businessType: searchForm.type || undefined,
      auditStatus: searchForm.result ? mapResultToStatus(searchForm.result) : undefined,
      auditorId: searchForm.auditor ? parseInt(searchForm.auditor) : undefined
    }
    const data = await getAuditHistoryList(params)
    if (data && data.rows) {
      tableData.value = data.rows.map(convertRecordToRow)
      pagination.total = data.total || 0
    } else if (data && data.records) {
      tableData.value = data.records.map(convertRecordToRow)
      pagination.total = data.total || 0
    } else if (data && Array.isArray(data)) {
      tableData.value = data.map(convertRecordToRow)
      pagination.total = data.length
    }
  } catch (e) {
    console.error('获取审核历史失败', e)
    tableData.value = generateMockHistory()
    pagination.total = tableData.value.length
  } finally {
    loading.value = false
  }
}

const mapResultToStatus = (result) => {
  const map = { approved: 'PASS', rejected: 'REJECT', retest: 'RETEST' }
  return map[result] || result
}

const convertRecordToRow = (record) => {
  return {
    id: record.id,
    businessNo: record.businessCode,
    type: record.businessType,
    title: `${record.auditLevel === 1 ? '一级' : '二级'}审核 - ${record.businessCode}`,
    applicant: record.createBy ? '提交人' : '待审核',
    auditor: record.auditorName || '',
    result: mapStatusToResult(record.auditStatus),
    auditTime: record.auditTime,
    auditOpinion: record.auditOpinion,
    applyTime: record.createTime,
    processInstanceId: record.processInstanceId
  }
}

const mapStatusToResult = (status) => {
  const map = { PASS: 'approved', REJECT: 'rejected', RETEST: 'retest' }
  return map[status] || status
}

const generateMockHistory = () => {
  return [
    { id: 1, businessNo: 'TK202401001', type: 'task', title: '一级审核 - TK202401001', applicant: '检测员A', auditor: '主管A', result: 'approved', auditTime: '2024-01-16 10:30:00', auditOpinion: '检测结果准确', applyTime: '2024-01-15 14:20:00', processInstanceId: 'proc-001' },
    { id: 2, businessNo: 'TK202401002', type: 'task', title: '二级审核 - TK202401002', applicant: '检测员B', auditor: '主管B', result: 'rejected', auditTime: '2024-01-15 16:45:00', auditOpinion: '检测数据异常，请重新检测', applyTime: '2024-01-15 09:10:00', processInstanceId: 'proc-002' },
    { id: 3, businessNo: 'TK202401003', type: 'task', title: '一级审核 - TK202401003', applicant: '检测员C', auditor: '主管A', result: 'retest', auditTime: '2024-01-14 11:20:00', auditOpinion: '结果接近限值，需复测确认', applyTime: '2024-01-14 08:30:00', processInstanceId: 'proc-003' }
  ]
}

const getTypeText = (type) => {
  const map = { sample: '样品审核', task: '任务审核', report: '报告审核', result: '结果审核' }
  return map[type] || type
}

const getTypeTag = (type) => {
  const map = { sample: 'primary', task: 'success', report: 'warning', result: 'info' }
  return map[type] || 'info'
}

const getResultText = (result) => {
  const map = { approved: '通过', rejected: '驳回', retest: '复测' }
  return map[result] || result
}

const getResultTagType = (result) => {
  const map = { approved: 'success', rejected: 'danger', retest: 'warning' }
  return map[result] || 'info'
}

const getOpinionTimelineType = (result) => {
  const map = { approved: 'success', rejected: 'danger', retest: 'warning' }
  return map[result] || 'primary'
}

const getOpinionTagType = (result) => {
  const map = { approved: 'success', rejected: 'danger', retest: 'warning' }
  return map[result] || 'info'
}

const handleSearch = () => {
  fetchHistoryList()
}

const handleReset = () => {
  searchForm.type = ''
  searchForm.result = ''
  searchForm.auditor = ''
  fetchHistoryList()
}

const handleView = async (row) => {
  currentDetail.value = row
  detailVisible.value = true

  try {
    if (row.processInstanceId) {
      flowData.value = await getAuditFlow(row.processInstanceId) || {
        processInstanceId: row.processInstanceId,
        businessCode: row.businessNo,
        currentStatus: 'COMPLETED',
        nodes: []
      }
    }
  } catch (e) {
    console.error('获取流程图失败', e)
    flowData.value = {
      processInstanceId: row.processInstanceId || '',
      businessCode: row.businessNo,
      currentStatus: 'COMPLETED',
      nodes: buildFlowNodes(row)
    }
  }

  opinionList.value = buildOpinionList(row)
}

const buildFlowNodes = (row) => {
  const isRetest = row.result === 'retest'
  const isRejected = row.result === 'rejected'
  const nodes = [
    { nodeCode: 'startEvent', nodeName: '提交审核', nodeType: 'start', status: 'completed', auditorName: row.applicant, auditTime: row.applyTime, auditOpinion: '', actionType: '' },
    { nodeCode: 'firstAuditTask', nodeName: '一级审核', nodeType: 'audit', status: 'completed', auditorName: '主管A', auditTime: row.auditTime, auditOpinion: row.auditOpinion, actionType: isRetest ? 'RETEST' : isRejected ? 'REJECT' : '' }
  ]

  if (!isRetest) {
    nodes.push({
      nodeCode: 'secondAuditTask',
      nodeName: '二级审核',
      nodeType: 'audit',
      status: 'completed',
      auditorName: row.auditor,
      auditTime: row.auditTime,
      auditOpinion: '',
      actionType: isRejected ? 'REJECT' : ''
    })
  }

  if (isRetest) {
    nodes.push({ nodeCode: 'retestCallActivity', nodeName: '复测', nodeType: 'audit', status: 'retest', auditorName: '', auditTime: '', auditOpinion: '', actionType: 'RETEST' })
    nodes.push({ nodeCode: 'endEvent', nodeName: '发起复测', nodeType: 'end', status: 'retest', auditorName: '', auditTime: row.auditTime, auditOpinion: '' })
  } else if (isRejected) {
    nodes.push({ nodeCode: 'endEvent', nodeName: '审核驳回', nodeType: 'end', status: 'rejected', auditorName: '', auditTime: row.auditTime, auditOpinion: '' })
  } else {
    nodes.push({ nodeCode: 'endEvent', nodeName: '审核通过', nodeType: 'end', status: 'approved', auditorName: '', auditTime: row.auditTime, auditOpinion: '' })
  }

  return nodes
}

const buildOpinionList = (row) => {
  const list = []

  list.push({
    auditor: '主管A',
    level: '一级审核',
    result: row.result === 'rejected' ? 'rejected' : 'approved',
    opinion: row.auditOpinion,
    time: row.auditTime
  })

  if (row.result !== 'retest') {
    list.push({
      auditor: row.auditor,
      level: '二级审核',
      result: row.result,
      opinion: '',
      time: row.auditTime
    })
  }

  return list
}

const handleViewRetest = (retestId) => {
  compareData.value = {
    retestId: retestId,
    retestCode: 'RT202401001',
    originalValue: '0.05',
    originalJudge: 'qualified',
    retestValue: '0.03',
    retestJudge: 'qualified',
    valueChanged: true,
    judgeChanged: false,
    adoptedResult: 'RETEST',
    adoptOpinion: '复测结果更准确，采用复测值'
  }
  compareVisible.value = true
}

const handleAdoptResult = async (data) => {
  try {
    await adoptRetestResult(data)
    compareVisible.value = false
    fetchHistoryList()
  } catch {
    compareVisible.value = false
  }
}

onMounted(() => {
  fetchHistoryList()
})
</script>

<style lang="scss" scoped>
.audit-history {
  .search-bar {
    margin-bottom: 16px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .opinion-card {
    :deep(.el-card__body) {
      padding: 12px;
    }

    .opinion-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 6px;

      .opinion-auditor {
        font-weight: 500;
        color: #303133;
      }
    }

    .opinion-content {
      font-size: 13px;
      color: #606266;
    }
  }
}
</style>
