<template>
  <div class="dashboard-container">
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="card-content">
              <div class="card-icon icon-blue">
                <el-icon :size="32"><Box /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-value">{{ stats.sampleTotal }}</div>
                <div class="card-label">样品总数</div>
              </div>
            </div>
            <div class="card-footer">
              <span class="trend up">
                <el-icon><Top /></el-icon>
                12.5%
              </span>
              <span class="text">较上月</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="card-content">
              <div class="card-icon icon-green">
                <el-icon :size="32"><Tickets /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-value">{{ stats.taskTotal }}</div>
                <div class="card-label">任务总数</div>
              </div>
            </div>
            <div class="card-footer">
              <span class="trend up">
                <el-icon><Top /></el-icon>
                8.3%
              </span>
              <span class="text">较上月</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="card-content">
              <div class="card-icon icon-orange">
                <el-icon :size="32"><CircleCheck /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-value">{{ stats.auditPending }}</div>
                <div class="card-label">待审核</div>
              </div>
            </div>
            <div class="card-footer">
              <span class="trend down">
                <el-icon><Bottom /></el-icon>
                5.2%
              </span>
              <span class="text">较上月</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="card-content">
              <div class="card-icon icon-purple">
                <el-icon :size="32"><Document /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-value">{{ stats.reportTotal }}</div>
                <div class="card-label">报告总数</div>
              </div>
            </div>
            <div class="card-footer">
              <span class="trend up">
                <el-icon><Top /></el-icon>
                15.8%
              </span>
              <span class="text">较上月</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="title">样品检测趋势</span>
              <el-radio-group v-model="chartType" size="small">
                <el-radio-button label="week">近7天</el-radio-button>
                <el-radio-button label="month">近30天</el-radio-button>
                <el-radio-button label="year">近一年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <v-chart :option="lineChartOption" :autoresize="true" style="height: 350px;" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span class="title">检测项目分布</span>
          </template>
          <v-chart :option="pieChartOption" :autoresize="true" style="height: 350px;" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span class="title">最近任务</span>
          </template>
          <el-table :data="recentTasks" style="width: 100%">
            <el-table-column prop="taskNo" label="任务编号" width="150" />
            <el-table-column prop="sampleName" label="样品名称" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span class="title">待审核列表</span>
          </template>
          <el-table :data="pendingAudits" style="width: 100%">
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="applicant" label="申请人" width="100" />
            <el-table-column label="操作" width="100">
              <template #default>
                <el-button type="primary" link size="small">审核</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Box, Tickets, CircleCheck, Document, Top, Bottom } from '@element-plus/icons-vue'

const chartType = ref('week')

const stats = reactive({
  sampleTotal: 1258,
  taskTotal: 896,
  auditPending: 42,
  reportTotal: 765
})

const recentTasks = ref([
  { taskNo: 'TK202401001', sampleName: '牛奶样品-001', status: 'detecting' },
  { taskNo: 'TK202401002', sampleName: '猪肉样品-002', status: 'pending' },
  { taskNo: 'TK202401003', sampleName: '蔬菜样品-003', status: 'completed' },
  { taskNo: 'TK202401004', sampleName: '水果样品-004', status: 'auditing' },
  { taskNo: 'TK202401005', sampleName: '粮油样品-005', status: 'detecting' }
])

const pendingAudits = ref([
  { type: '报告', title: '2024年第一季度食品安全检测报告', applicant: '张三' },
  { type: '任务', title: '食品添加剂检测任务分配', applicant: '李四' },
  { type: '样品', title: '进口食品样品登记审批', applicant: '王五' },
  { type: '报告', title: '微生物检测结果报告', applicant: '赵六' }
])

const getStatusType = (status) => {
  const map = {
    pending: 'info',
    detecting: 'warning',
    auditing: 'primary',
    completed: 'success',
    rejected: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    pending: '待分配',
    detecting: '检测中',
    auditing: '审核中',
    completed: '已完成',
    rejected: '已驳回'
  }
  return map[status] || status
}

const lineChartOption = ref({
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['样品登记', '检测完成', '报告出具'],
    bottom: 0
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '15%',
    top: '10%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: '样品登记',
      type: 'line',
      smooth: true,
      data: [32, 45, 38, 52, 48, 28, 35],
      itemStyle: { color: '#409eff' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ]
        }
      }
    },
    {
      name: '检测完成',
      type: 'line',
      smooth: true,
      data: [25, 38, 42, 36, 45, 22, 30],
      itemStyle: { color: '#67c23a' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ]
        }
      }
    },
    {
      name: '报告出具',
      type: 'line',
      smooth: true,
      data: [20, 30, 35, 28, 38, 18, 25],
      itemStyle: { color: '#e6a23c' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(230, 162, 60, 0.3)' },
            { offset: 1, color: 'rgba(230, 162, 60, 0.05)' }
          ]
        }
      }
    }
  ]
})

const pieChartOption = ref({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    right: '5%',
    top: 'center'
  },
  series: [
    {
      name: '检测项目',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 20,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: [
        { value: 335, name: '微生物', itemStyle: { color: '#409eff' } },
        { value: 310, name: '农残', itemStyle: { color: '#67c23a' } },
        { value: 234, name: '重金属', itemStyle: { color: '#e6a23c' } },
        { value: 135, name: '添加剂', itemStyle: { color: '#f56c6c' } },
        { value: 148, name: '毒素', itemStyle: { color: '#909399' } }
      ]
    }
  ]
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 0;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  .card-content {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 10px 0;
  }

  .card-icon {
    width: 64px;
    height: 64px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;

    &.icon-blue {
      background: linear-gradient(135deg, #667eea 0%, #409eff 100%);
    }

    &.icon-green {
      background: linear-gradient(135deg, #52c41a 0%, #67c23a 100%);
    }

    &.icon-orange {
      background: linear-gradient(135deg, #fa8c16 0%, #e6a23c 100%);
    }

    &.icon-purple {
      background: linear-gradient(135deg, #722ed1 0%, #9b59b6 100%);
    }
  }

  .card-info {
    flex: 1;
  }

  .card-value {
    font-size: 28px;
    font-weight: 600;
    color: var(--el-text-color-primary, #303133);
    line-height: 1.2;
  }

  .card-label {
    font-size: 14px;
    color: var(--el-text-color-secondary, #909399);
    margin-top: 4px;
  }

  .card-footer {
    display: flex;
    align-items: center;
    gap: 8px;
    padding-top: 12px;
    border-top: 1px solid var(--el-border-color-lighter, #ebeef5);
    margin-top: 12px;

    .trend {
      display: flex;
      align-items: center;
      font-size: 13px;
      font-weight: 500;

      &.up {
        color: #67c23a;
      }

      &.down {
        color: #f56c6c;
      }
    }

    .text {
      font-size: 13px;
      color: var(--el-text-color-secondary, #909399);
    }
  }
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .title {
      font-weight: 600;
      font-size: 15px;
    }
  }

  :deep(.el-card__header) {
    padding: 15px 20px;
  }
}
</style>
