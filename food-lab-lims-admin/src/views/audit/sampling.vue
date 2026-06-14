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
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
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

    <el-dialog v-model="createVisible" title="新建抽样复审" width="500px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="抽样率">
          <el-slider v-model="createForm.sampleRate" :min="1" :max="100" :step="5" show-input />
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
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCreate">确认创建</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  createSamplingReview,
  getPendingSamplingReviews,
  completeSamplingReview
} from '@/api/audit'

const loading = ref(false)
const createVisible = ref(false)
const reviewVisible = ref(false)

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

const reviewList = ref([
  { id: 1, taskCode: 'TK202401001', sampleCode: 'SP202401001', sampleRate: 0.1, reviewType: 'RANDOM', reviewStatus: 'PENDING', reviewerName: '', reviewTime: '', reviewOpinion: '' },
  { id: 2, taskCode: 'TK202401002', sampleCode: 'SP202401002', sampleRate: 0.15, reviewType: 'RANDOM', reviewStatus: 'PASS', reviewerName: '质量管理员', reviewTime: '2024-01-16 15:30:00', reviewOpinion: '复核无误' },
  { id: 3, taskCode: 'TK202401003', sampleCode: 'SP202401003', sampleRate: 0.2, reviewType: 'RANDOM', reviewStatus: 'REJECT', reviewerName: '质量管理员', reviewTime: '2024-01-14 11:20:00', reviewOpinion: '检测数据有误，需要重新检测' }
])

const getStatusTag = (status) => {
  const map = { PENDING: 'warning', PASS: 'success', REJECT: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { PENDING: '待复审', PASS: '已通过', REJECT: '已驳回' }
  return map[status] || status
}

const handleCreate = () => {
  createForm.sampleRate = 10
  createForm.reviewType = 'RANDOM'
  createForm.remark = ''
  createVisible.value = true
}

const confirmCreate = async () => {
  try {
    await createSamplingReview({
      sampleRate: createForm.sampleRate / 100,
      reviewType: createForm.reviewType,
      remark: createForm.remark
    })
    ElMessage.success('抽样复审已创建')
    createVisible.value = false
  } catch {
    ElMessage.error('创建失败')
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
    const item = reviewList.value.find(r => r.id === reviewForm.reviewId)
    if (item) {
      item.reviewStatus = reviewForm.result
      item.reviewOpinion = reviewForm.opinion
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

onMounted(async () => {
  try {
    const data = await getPendingSamplingReviews()
    if (data && data.length) {
      reviewList.value = data
    }
  } catch {
    // use mock data
  }
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
}
</style>
