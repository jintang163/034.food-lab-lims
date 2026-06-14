<template>
  <div class="retest-compare">
    <div class="compare-header">
      <span class="compare-title">复测结果对比</span>
      <el-tag v-if="compareData.adoptedResult" :type="compareData.adoptedResult === 'RETEST' ? 'success' : 'info'" size="small">
        {{ compareData.adoptedResult === 'RETEST' ? '已采用复测结果' : '已采用原结果' }}
      </el-tag>
    </div>

    <div class="compare-table">
      <div class="compare-row compare-header-row">
        <div class="compare-cell label-cell"></div>
        <div class="compare-cell original-cell">原检测结果</div>
        <div class="compare-cell retest-cell">复测结果</div>
        <div class="compare-cell diff-cell">差异</div>
      </div>

      <div class="compare-row">
        <div class="compare-cell label-cell">检测值</div>
        <div class="compare-cell original-cell">
          <span class="value-text">{{ compareData.originalValue || '-' }}</span>
        </div>
        <div class="compare-cell retest-cell">
          <span class="value-text" :class="{ changed: compareData.valueChanged }">
            {{ compareData.retestValue || '-' }}
          </span>
        </div>
        <div class="compare-cell diff-cell">
          <el-tag v-if="compareData.valueChanged" type="warning" size="small">有变化</el-tag>
          <el-tag v-else type="success" size="small">一致</el-tag>
        </div>
      </div>

      <div class="compare-row">
        <div class="compare-cell label-cell">判定</div>
        <div class="compare-cell original-cell">
          <el-tag :type="getJudgeTagType(compareData.originalJudge)" size="small">
            {{ getJudgeText(compareData.originalJudge) }}
          </el-tag>
        </div>
        <div class="compare-cell retest-cell">
          <el-tag :type="getJudgeTagType(compareData.retestJudge)" size="small">
            {{ getJudgeText(compareData.retestJudge) }}
          </el-tag>
        </div>
        <div class="compare-cell diff-cell">
          <el-tag v-if="compareData.judgeChanged" type="danger" size="small">判定改变</el-tag>
          <el-tag v-else type="success" size="small">一致</el-tag>
        </div>
      </div>
    </div>

    <div v-if="!compareData.adoptedResult" class="adopt-section">
      <el-divider>选择采用结果</el-divider>
      <el-form :model="adoptForm" label-width="80px">
        <el-form-item label="采用结果">
          <el-radio-group v-model="adoptForm.adoptedResult">
            <el-radio value="ORIGINAL">采用原检测结果</el-radio>
            <el-radio value="RETEST">采用复测结果</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="采用意见">
          <el-input
            v-model="adoptForm.adoptOpinion"
            type="textarea"
            :rows="3"
            placeholder="请输入采用意见"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleAdopt">确认采用</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div v-else class="adopted-info">
      <el-divider>采用记录</el-divider>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="采用结果">
          <el-tag :type="compareData.adoptedResult === 'RETEST' ? 'success' : 'info'" size="small">
            {{ compareData.adoptedResult === 'RETEST' ? '复测结果' : '原检测结果' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="采用意见">
          {{ compareData.adoptOpinion || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  compareData: {
    type: Object,
    default: () => ({
      retestId: null,
      retestCode: '',
      originalValue: '',
      originalJudge: '',
      retestValue: '',
      retestJudge: '',
      valueChanged: false,
      judgeChanged: false,
      adoptedResult: '',
      adoptOpinion: ''
    })
  }
})

const emit = defineEmits(['adopt'])

const adoptForm = reactive({
  adoptedResult: '',
  adoptOpinion: ''
})

const getJudgeTagType = (judge) => {
  if (judge === 'qualified') return 'success'
  if (judge === 'unqualified') return 'danger'
  return 'info'
}

const getJudgeText = (judge) => {
  const map = { qualified: '合格', unqualified: '不合格', pending: '待判定' }
  return map[judge] || judge || '-'
}

const handleAdopt = () => {
  if (!adoptForm.adoptedResult) {
    ElMessage.warning('请选择采用结果')
    return
  }

  ElMessageBox.confirm(
    `确定采用${adoptForm.adoptedResult === 'RETEST' ? '复测' : '原检测'}结果吗？`,
    '确认采用',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    emit('adopt', {
      retestId: props.compareData.retestId,
      adoptedResult: adoptForm.adoptedResult,
      adoptOpinion: adoptForm.adoptOpinion
    })
  }).catch(() => {})
}
</script>

<style lang="scss" scoped>
.retest-compare {
  .compare-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;

    .compare-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .compare-table {
    border: 1px solid #ebeef5;
    border-radius: 4px;
    overflow: hidden;
  }

  .compare-row {
    display: flex;
    border-bottom: 1px solid #ebeef5;

    &:last-child {
      border-bottom: none;
    }
  }

  .compare-header-row {
    background: #f5f7fa;

    .compare-cell {
      font-weight: 600;
      color: #606266;
    }
  }

  .compare-cell {
    flex: 1;
    padding: 10px 12px;
    font-size: 13px;
    color: #303133;

    &.label-cell {
      flex: 0 0 80px;
      background: #fafafa;
      color: #909399;
      font-weight: 500;
    }

    &.diff-cell {
      flex: 0 0 100px;
    }
  }

  .value-text {
    &.changed {
      color: #e6a23c;
      font-weight: 600;
    }
  }

  .adopt-section,
  .adopted-info {
    margin-top: 8px;
  }
}
</style>
