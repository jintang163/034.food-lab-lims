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
            <el-tag :type="row.result === 'approved' ? 'success' : 'danger'" size="small">
              {{ row.result === 'approved' ? '通过' : '驳回' }}
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

    <el-dialog v-model="detailVisible" title="审核历史详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="业务编号">{{ currentDetail.businessNo }}</el-descriptions-item>
        <el-descriptions-item label="审核类型">{{ getTypeText(currentDetail.type) }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ currentDetail.title }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentDetail.applicant }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ currentDetail.auditor }}</el-descriptions-item>
        <el-descriptions-item label="审核结果">
          <el-tag :type="currentDetail.result === 'approved' ? 'success' : 'danger'" size="small">
            {{ currentDetail.result === 'approved' ? '通过' : '驳回' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间" :span="2">{{ currentDetail.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="审核时间" :span="2">{{ currentDetail.auditTime }}</el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">{{ currentDetail.auditOpinion || '无' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>审核流程</el-divider>

      <el-steps :active="3" direction="vertical" finish-status="success">
        <el-step title="提交申请">
          <template #description>
            <p>{{ currentDetail.applicant }} 提交申请</p>
            <p style="color: #909399; font-size: 12px">{{ currentDetail.applyTime }}</p>
          </template>
        </el-step>
        <el-step title="一级审核">
          <template #description>
            <p>张三 {{ currentDetail.result === 'approved' ? '审核通过' : '审核驳回' }}</p>
            <p style="color: #909399; font-size: 12px">2024-01-16 10:30:00</p>
          </template>
        </el-step>
        <el-step :status="currentDetail.result === 'approved' ? 'success' : 'error'" title="二级审核">
          <template #description>
            <p>{{ currentDetail.auditor }} {{ currentDetail.result === 'approved' ? '审核通过' : '审核驳回' }}</p>
            <p style="color: #909399; font-size: 12px">{{ currentDetail.auditTime }}</p>
            <p v-if="currentDetail.result === 'rejected' && currentDetail.auditOpinion" style="color: #f56c6c; font-size: 12px; margin-top: 5px">
              驳回原因：{{ currentDetail.auditOpinion }}
            </p>
          </template>
        </el-step>
      </el-steps>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const loading = ref(false)
const detailVisible = ref(false)
const currentDetail = ref({})

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

const tableData = ref([
  { id: 1, businessNo: 'SP202401001', type: 'sample', title: '牛奶样品-001 检测申请', applicant: '张三', auditor: '主管A', result: 'approved', applyTime: '2024-01-15 09:30:00', auditTime: '2024-01-15 14:30:00', auditOpinion: '符合检测要求，同意' },
  { id: 2, businessNo: 'TK202401002', type: 'task', title: '猪肉样品-002 任务分配', applicant: '李四', auditor: '主管B', result: 'approved', applyTime: '2024-01-16 10:15:00', auditTime: '2024-01-16 15:00:00', auditOpinion: '同意分配' },
  { id: 3, businessNo: 'RP202401003', type: 'report', title: '蔬菜样品-003 报告出具', applicant: '王五', auditor: '主管A', result: 'rejected', applyTime: '2024-01-14 08:45:00', auditTime: '2024-01-14 16:20:00', auditOpinion: '检测数据不完整，请补充后重新提交' },
  { id: 4, businessNo: 'RT202401004', type: 'result', title: '饮用水-004 结果审核', applicant: '赵六', auditor: '主管C', result: 'approved', applyTime: '2024-01-13 14:20:00', auditTime: '2024-01-13 17:30:00', auditOpinion: '' },
  { id: 5, businessNo: 'SP202401005', type: 'sample', title: '食用油-005 样品登记', applicant: '张三', auditor: '主管B', result: 'approved', applyTime: '2024-01-17 11:00:00', auditTime: '2024-01-17 14:00:00', auditOpinion: '资料齐全，同意登记' },
  { id: 6, businessNo: 'TK202401006', type: 'task', title: '大米-006 任务分配', applicant: '李四', auditor: '主管A', result: 'approved', applyTime: '2024-01-18 09:10:00', auditTime: '2024-01-18 11:30:00', auditOpinion: '' },
  { id: 7, businessNo: 'RP202401007', type: 'report', title: '鸡蛋-007 报告审核', applicant: '王五', auditor: '主管C', result: 'approved', applyTime: '2024-01-12 15:30:00', auditTime: '2024-01-12 17:00:00', auditOpinion: '报告数据完整，格式规范，同意出具' },
  { id: 8, businessNo: 'RT202401008', type: 'result', title: '水果-008 结果审核', applicant: '赵六', auditor: '主管B', result: 'rejected', applyTime: '2024-01-11 10:45:00', auditTime: '2024-01-11 15:20:00', auditOpinion: '检测结果异常，需要复检确认' }
])

pagination.total = 68

const getTypeText = (type) => {
  const map = { sample: '样品审核', task: '任务审核', report: '报告审核', result: '结果审核' }
  return map[type] || type
}

const getTypeTag = (type) => {
  const map = { sample: 'primary', task: 'success', report: 'warning', result: 'info' }
  return map[type] || 'info'
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => { loading.value = false }, 500)
}

const handleReset = () => {
  searchForm.type = ''
  searchForm.result = ''
  searchForm.auditor = ''
  handleSearch()
}

const handleView = (row) => {
  currentDetail.value = row
  detailVisible.value = true
}
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
}
</style>
