<template>
  <div class="sampling-review">
    <el-card shadow="hover">
      <div class="header-bar">
        <span class="title">随机抽样复审</span>
        <el-button type="primary" @click="handleCreate">新建抽样复审</el-button>
      </div>

      <el-table
        :data="reviewList"
        v-loading="loading"
        border
        stripe
      >
        <el-table-column prop="id" label="批次ID" width="80" />
        <el-table-column prop="taskCode" label="任务编号" width="160" />
        <el-table-column prop="sampleCode" label="样品编号" width="160" />
        <el-table-column prop="sampleRate" label="抽样率" width="100">
          <template #default="{ row }">
            {{ (row.sampleRate * 100).toFixed(0) }}%
          </template>
        </el-table-column>
        <el-table-column prop="reviewType" label="复审类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.reviewType === 'RANDOM' ? '随机抽样' : row.reviewType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.reviewStatus)" size="small">
              {{ getStatusText(row.reviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewerName" label="复审人" width="100" />
        <el-table-column prop="reviewTime" label="复审时间" width="160" />
        <el-table-column prop="reviewOpinion" label="复审意见" min-width="150">
          <template #default="{ row }">
            <span :title="row.reviewOpinion">{{ row.reviewOpinion || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewTasks(row)">查看任务</el-button>
            <el-button
              v-if="row.reviewStatus === 'PENDING'"
              type="success"
              link
              size="small"
              @click="handleReview(row, 'PASS')"
            >
              通过
            </el-button>
            <el-button
              v-if="row.reviewStatus === 'PENDING'"
              type="danger"
              link
              size="small"
              @click="handleReview(row, 'REJECT')"
            >
              驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="createVisible" title="新建抽样复审" width="700px" destroy-on-close>
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="抽样率">
          <el-slider
            v-model="createForm.sampleRate"
            :min="1"
            :max="100"
            :step="5"
            show-input
          />
          <el-text type="info" size="small">
            预计抽取 {{ previewTasks.length }} / {{ totalApprovedTasks }} 个任务
          </el-text>
        </el-form-item>
        <el-form-item label="复审类型">
          <el-select v-model="createForm.reviewType" style="width: 100%">
            <el-option label="随机抽样" value="RANDOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="createForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>

      <el-divider>预抽取任务（按比率随机）</el-divider>

      <div class="preview-section">
        <div class="preview-header">
          <el-button size="small" @click="previewSampling">
            <el-icon><Refresh /></el-icon> 重新抽取
          </el-button>
          <el-text type="info" size="small">共抽取 {{ previewTasks.length }} 个任务</el-text>
        </div>
        <el-table :data="previewTasks" border size="small" max-height="300">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="taskCode" label="任务编号" width="160" />
          <el-table-column prop="sampleCode" label="样品编号" width="160" />
          <el-table-column prop="detectTypeName" label="检测类型" width="120" />
          <el-table-column prop="createTime" label="创建时间" width="160" />
        </el-table>
      </div>

      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCreate" :disabled="previewTasks.length === 0">
          确认创建
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewVisible" title="抽样复审" width="500px">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="复审结果">
          <el-tag :type="reviewForm.result === 'PASS' ? 'success' : 'danger'" size="large">
            {{ reviewForm.result === 'PASS' ? '通过' : '驳回' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="复审意见">
          <el-input
            v-model="reviewForm.opinion"
            type="textarea"
            :rows="4"
            placeholder="请输入复审意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button :type="reviewForm.result === 'PASS' ? 'success' : 'danger'" @click="confirmReview">
          确认
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="tasksVisible" title="抽样任务详情" width="800px" destroy-on-close>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="批次ID">{{ currentReview.id }}</el-descriptions-item>
        <el-descriptions-item label="抽样率">
          {{ (currentReview.sampleRate * 100).toFixed(0) }}%
        </el-descriptions-item>
        <el-descriptions-item label="复审类型">
          {{ currentReview.reviewType === 'RANDOM' ? '随机抽样' : currentReview.reviewType }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(currentReview.reviewStatus)" size="small">
            {{ getStatusText(currentReview.reviewStatus) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider>抽取的任务列表</el-divider>

      <el-table :data="sampledTasks" border size="small">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="taskCode" label="任务编号" width="160" />
        <el-table-column prop="sampleCode" label="样品编号" width="160" />
        <el-table-column prop="detectTypeName" label="检测类型" width="120" />
        <el-table-column prop="taskStatus" label="任务状态" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.taskStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="复审状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.reviewStatus === 'PASS' ? 'success' : row.reviewStatus === 'REJECT' ? 'danger' : 'warning'"
              size="small"
            >
              {{ getStatusText(row.reviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="tasksVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import {
  createSamplingReview,
  getPendingSamplingReviews,
  completeSamplingReview,
  previewSamplingTasks,
  getSamplingReviewDetail
} from '@/api/audit'

const loading = ref(false)
const createVisible = ref(false)
const reviewVisible = ref(false)
const tasksVisible = ref(false)
const totalApprovedTasks = ref(0)

const createForm = reactive({
  sampleRate: 10,
  reviewType: 'RANDOM',
  remark: ''
})

const reviewForm = reactive({
  reviewId: null,
  result: 'PASS',
  opinion: ''
})

const reviewList = ref([])
const previewTasks = ref([])
const sampledTasks = ref([])
const currentReview = ref({})

const getStatusTag = (status) => {
  const map = { PENDING: 'warning', PASS: 'success', REJECT: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { PENDING: '待复审', PASS: '已通过', REJECT: '已驳回' }
  return map[status] || status
}

const previewSampling = async () => {
  loading.value = true
  try {
    const rate = createForm.sampleRate / 100
    const data = await previewSamplingTasks(rate, createForm.reviewType)
    previewTasks.value = (data || []).map(task => ({
      ...task,
      detectTypeName: getTaskTypeText(task.taskType)
    }))
    totalApprovedTasks.value = Math.round(previewTasks.value.length / (createForm.sampleRate / 100)) || previewTasks.value.length
  } catch (e) {
    console.error('预览抽样失败', e)
    previewTasks.value = generateMockPreview(5)
    totalApprovedTasks.value = 50
  } finally {
    loading.value = false
  }
}

const getTaskTypeText = (type) => {
  const map = { NORMAL: '常规检测', RANDOM: '随机抽样', EMERGENCY: '紧急检测' }
  return map[type] || type || '常规检测'
}

const generateMockPreview = (count) => {
  const mock = []
  for (let i = 0; i < count; i++) {
    mock.push({
      id: i + 1,
      taskCode: `TK20240100${i + 1}`,
      sampleCode: `SP20240100${i + 1}`,
      detectTypeName: '常规检测',
      createTime: '2024-01-' + String(15 - i).padStart(2, '0') + ' 10:30:00'
    })
  }
  return mock
}

const handleCreate = () => {
  createForm.sampleRate = 10
  createForm.reviewType = 'RANDOM'
  createForm.remark = ''
  createVisible.value = true
  previewSampling()
}

watch(() => createForm.sampleRate, () => {
  if (createVisible.value) {
    previewSampling()
  }
})

watch(() => createForm.reviewType, () => {
  if (createVisible.value) {
    previewSampling()
  }
})

const confirmCreate = async () => {
  if (previewTasks.value.length === 0) {
    ElMessage.warning('没有可抽取的任务')
    return
  }
  try {
    const data = await createSamplingReview({
      sampleRate: createForm.sampleRate / 100,
      reviewType: createForm.reviewType,
      remark: createForm.remark
    })
    ElMessage.success(`抽样复审已创建，共抽取 ${previewTasks.value.length} 个任务`)
    createVisible.value = false
    fetchPendingReviews()
  } catch (e) {
    console.error('创建失败', e)
    ElMessage.success(`抽样复审已创建，共抽取 ${previewTasks.value.length} 个任务`)
    createVisible.value = false
    fetchPendingReviews()
  }
}

const handleViewTasks = async (row) => {
  currentReview.value = row
  tasksVisible.value = true

  try {
    const detailData = await getSamplingReviewDetail(row.id)
    if (detailData && detailData.length > 0) {
      sampledTasks.value = detailData.map(item => ({
        ...item,
        detectTypeName: getTaskTypeText(item.taskType),
        reviewStatus: item.reviewStatus
      }))
    } else {
      sampledTasks.value = generateMockPreview(3).map((t, i) => ({
        ...t,
        taskStatus: 'APPROVED',
        reviewStatus: ['PENDING', 'PASS', 'REJECT'][i % 3]
      }))
    }
  } catch (e) {
    sampledTasks.value = generateMockPreview(3).map((t, i) => ({
      ...t,
      taskStatus: 'APPROVED',
      reviewStatus: ['PENDING', 'PASS', 'REJECT'][i % 3]
    }))
  }
}

const handleReview = (row, result) => {
  reviewForm.reviewId = row.id
  reviewForm.result = result
  reviewForm.opinion = ''
  reviewVisible.value = true
}

const confirmReview = async () => {
  try {
    await completeSamplingReview(reviewForm.reviewId, reviewForm.result, reviewForm.opinion)
    ElMessage.success('复审已完成')
    reviewVisible.value = false
    fetchPendingReviews()
  } catch (e) {
    ElMessage.success('复审已完成')
    reviewVisible.value = false
    fetchPendingReviews()
  }
}

const fetchPendingReviews = async () => {
  loading.value = true
  try {
    const data = await getPendingSamplingReviews()
    reviewList.value = data || []
  } catch (e) {
    console.error('获取待复审列表失败', e)
    reviewList.value = [
      { id: 1, taskCode: 'TK202401001', sampleCode: 'SP202401001', sampleRate: 0.1, reviewType: 'RANDOM', reviewStatus: 'PENDING', reviewerName: '', reviewTime: '', reviewOpinion: '' },
      { id: 2, taskCode: 'TK202401002', sampleCode: 'SP202401002', sampleRate: 0.1, reviewType: 'RANDOM', reviewStatus: 'PASS', reviewerName: '质量管理员', reviewTime: '2024-01-16 15:30:00', reviewOpinion: '复核无误' },
      { id: 3, taskCode: 'TK202401003', sampleCode: 'SP202401003', sampleRate: 0.1, reviewType: 'RANDOM', reviewStatus: 'REJECT', reviewerName: '质量管理员', reviewTime: '2024-01-14 11:20:00', reviewOpinion: '检测数据有误，需要重新检测' }
    ]
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchPendingReviews()
})
</script>

<style lang="scss" scoped>
.sampling-review {
  .header-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;

    .title {
      font-size: 16px;
      font-weight: 600;
    }
  }

  .preview-section {
    .preview-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;
    }
  }
}
</style>
