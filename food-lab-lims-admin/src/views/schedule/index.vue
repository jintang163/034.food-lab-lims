<template>
  <div class="schedule-gantt-page">
    <el-card class="toolbar-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="loadData"
          />
          <el-select v-model="viewMode" style="width: 140px; margin-left: 12px" @change="loadData">
            <el-option label="按仪器视图" value="instrument" />
            <el-option label="按人员视图" value="person" />
          </el-select>
          <el-button type="primary" style="margin-left: 12px" @click="openGenerateDialog">
            <el-icon><MagicStick /></el-icon>智能排程
          </el-button>
          <el-button style="margin-left: 8px" @click="loadData">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
          <el-button type="success" style="margin-left: 8px" :disabled="!currentBatchNo" @click="doPublish">
            <el-icon><Check /></el-icon>发布排程
          </el-button>
          <el-button type="warning" style="margin-left: 8px" @click="checkConflictsAction">
            <el-icon><Warning /></el-icon>冲突检测
          </el-button>
        </div>
        <div class="toolbar-right">
          <div class="legend">
            <span class="legend-item"><i class="dot high"></i>高优先级</span>
            <span class="legend-item"><i class="dot medium"></i>中优先级</span>
            <span class="legend-item"><i class="dot low"></i>低优先级</span>
            <span class="legend-item"><i class="dot done"></i>已完成</span>
          </div>
        </div>
      </div>
    </el-card>

    <el-card class="gantt-card" v-loading="loading">
      <div class="gantt-container">
        <div class="gantt-sidebar">
          <div class="gantt-header-cell gantt-resource-header">资源</div>
          <div
            class="gantt-resource-row"
            v-for="res in resources"
            :key="res.id"
          >
            <div class="gantt-resource-cell">
              <el-tag :type="res.type === 'instrument' ? 'primary' : 'success'" size="small">
                {{ res.type === 'instrument' ? '仪器' : '人员' }}
              </el-tag>
              <span class="resource-name" :title="res.name">{{ res.name }}</span>
              <span v-if="res.code" class="resource-code">({{ res.code }})</span>
            </div>
          </div>
        </div>

        <div class="gantt-main" ref="ganttMain">
          <div class="gantt-timeline-header" :style="{ width: timelineWidth + 'px' }">
            <div
              class="gantt-day-header"
              v-for="(day, idx) in dayList"
              :key="idx"
              :class="{ weekend: isWeekend(day) }"
            >
              <div class="day-date">{{ formatDay(day) }}</div>
              <div class="day-week">{{ formatWeek(day) }}</div>
            </div>
          </div>

          <div class="gantt-body">
            <div
              class="gantt-row"
              v-for="(res, rIdx) in resources"
              :key="res.id"
              :style="{ width: timelineWidth + 'px' }"
            >
              <div
                class="gantt-day-cell"
                v-for="(day, dIdx) in dayList"
                :key="dIdx"
                :class="{ weekend: isWeekend(day) }"
              >
                <div
                  v-for="(task, tIdx) in getTasksByResourceAndDay(res, day)"
                  :key="tIdx"
                  class="gantt-task"
                  :class="[getTaskClass(task), { draggable: task.canDrag }]"
                  :style="getTaskStyle(task, day)"
                  :title="getTaskTooltip(task)"
                  draggable="true"
                  @dragstart="handleDragStart($event, task)"
                  @dragover.prevent="handleDragOver($event)"
                  @drop="handleDrop($event, res, day)"
                  @dragend="handleDragEnd"
                  @click="openTaskDetail(task)"
                >
                  <span class="task-name">{{ truncate(task.name, 12) }}</span>
                  <span v-if="task.detectPersonName" class="task-person">{{ task.detectPersonName }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="generateVisible" title="智能排程生成" width="560px">
      <el-form :model="generateForm" label-width="120px">
        <el-form-item label="排程范围" required>
          <el-date-picker
            v-model="generateForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="算法类型">
          <el-select v-model="generateForm.algorithmType">
            <el-option label="贪心算法（优先高优先级+最早完成）" value="GREEDY" />
            <el-option label="遗传算法（更优解，较慢）" value="GENETIC" />
          </el-select>
        </el-form-item>
        <el-form-item label="考虑人员休假">
          <el-switch v-model="generateForm.considerStaffLeave" />
        </el-form-item>
        <el-form-item label="考虑仪器日历">
          <el-switch v-model="generateForm.considerInstrumentCalendar" />
        </el-form-item>
        <el-form-item label="自动发布">
          <el-switch v-model="generateForm.autoPublish" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateVisible = false">取消</el-button>
        <el-button type="primary" @click="doGenerate" :loading="generating">生成排程</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="adjustVisible" title="调整排程" width="520px">
      <el-form :model="adjustForm" label-width="100px" v-if="selectedTask">
        <el-form-item label="任务">
          <span>{{ selectedTask.name }}</span>
        </el-form-item>
        <el-form-item label="当前仪器">
          <span>{{ selectedTask.instrumentName }}</span>
        </el-form-item>
        <el-form-item label="目标仪器">
          <el-select v-model="adjustForm.newInstrumentId" clearable placeholder="不更换仪器" style="width:100%">
            <el-option
              v-for="ins in resources.filter(r => r.type === 'instrument')"
              :key="ins.id"
              :label="ins.name"
              :value="ins.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="当前人员" v-if="selectedTask.detectPersonName">
          <span>{{ selectedTask.detectPersonName }}</span>
        </el-form-item>
        <el-form-item label="目标人员">
          <el-select v-model="adjustForm.newDetectPersonId" clearable placeholder="不更换人员" style="width:100%">
            <el-option
              v-for="p in resources.filter(r => r.type === 'person')"
              :key="p.id - 1000000"
              :label="p.name"
              :value="p.id - 1000000"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="新开始时间" required>
          <el-date-picker
            v-model="adjustForm.newStartTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="新结束时间" required>
          <el-date-picker
            v-model="adjustForm.newEndTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="调整原因">
          <el-input v-model="adjustForm.reason" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustVisible = false">取消</el-button>
        <el-button type="primary" @click="doAdjust" :loading="adjusting">确认调整</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="排程任务详情" size="480px">
      <div v-if="selectedTask" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="任务名称">{{ selectedTask.name }}</el-descriptions-item>
          <el-descriptions-item label="仪器">{{ selectedTask.instrumentName }}</el-descriptions-item>
          <el-descriptions-item label="检测人员" v-if="selectedTask.detectPersonName">
            {{ selectedTask.detectPersonName }}
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatDateTime(selectedTask.start) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ formatDateTime(selectedTask.end) }}</el-descriptions-item>
          <el-descriptions-item label="预计时长">{{ selectedTask.duration }}分钟</el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="priorityTagType(selectedTask.priority)">{{ priorityText(selectedTask.priority) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag>{{ selectedTask.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="排程批次">{{ selectedTask.scheduleBatchNo }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ selectedTask.source === 'AUTO' ? '自动排程' : '手动调整' }}</el-descriptions-item>
          <el-descriptions-item label="备注" v-if="selectedTask.remark">{{ selectedTask.remark }}</el-descriptions-item>
        </el-descriptions>
        <div class="detail-actions" style="margin-top: 20px">
          <el-button type="primary" @click="openAdjustDialog">调整排程</el-button>
          <el-button type="danger" @click="doCancel">取消排程</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, Refresh, Check, Warning } from '@element-plus/icons-vue'
import {
  generateSchedule,
  getGanttData,
  getGanttResources,
  checkConflicts,
  adjustSchedule,
  cancelSchedule,
  publishSchedule
} from '@/api/schedule'

const loading = ref(false)
const generating = ref(false)
const adjusting = ref(false)

const today = new Date()
const startDefault = new Date(today.getFullYear(), today.getMonth(), today.getDate())
const endDefault = new Date(today.getFullYear(), today.getMonth(), today.getDate() + 14)
function fmt(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const dateRange = ref([fmt(startDefault), fmt(endDefault)])
const viewMode = ref('instrument')
const resources = ref([])
const tasks = ref([])
const currentBatchNo = ref('')

const dayList = computed(() => {
  const days = []
  if (!dateRange.value || dateRange.value.length !== 2) return days
  let [s, e] = dateRange.value
  const start = new Date(s)
  const end = new Date(e)
  const cur = new Date(start)
  while (cur <= end) {
    days.push(new Date(cur))
    cur.setDate(cur.getDate() + 1)
  }
  return days
})

const DAY_WIDTH = 160
const timelineWidth = computed(() => dayList.value.length * DAY_WIDTH)

const generateVisible = ref(false)
const generateForm = reactive({
  dateRange: [],
  algorithmType: 'GREEDY',
  considerStaffLeave: true,
  considerInstrumentCalendar: true,
  autoPublish: false
})

const adjustVisible = ref(false)
const adjustForm = reactive({
  id: null,
  newStartTime: '',
  newEndTime: '',
  newInstrumentId: null,
  newDetectPersonId: null,
  reason: ''
})

const detailVisible = ref(false)
const selectedTask = ref(null)

let draggingTask = null

function openGenerateDialog() {
  generateForm.dateRange = [...dateRange.value]
  generateVisible.value = true
}

async function doGenerate() {
  if (!generateForm.dateRange || generateForm.dateRange.length !== 2) {
    ElMessage.warning('请选择排程日期范围')
    return
  }
  generating.value = true
  try {
    const payload = {
      startDate: generateForm.dateRange[0],
      endDate: generateForm.dateRange[1],
      algorithmType: generateForm.algorithmType,
      considerStaffLeave: generateForm.considerStaffLeave,
      considerInstrumentCalendar: generateForm.considerInstrumentCalendar,
      autoPublish: generateForm.autoPublish
    }
    const res = await generateSchedule(payload)
    const batchNo = res.data
    currentBatchNo.value = batchNo
    ElMessage.success(`排程生成成功，批次号：${batchNo}`)
    generateVisible.value = false
    await loadData()
  } catch (e) {
    ElMessage.error(e.message || '排程生成失败')
  } finally {
    generating.value = false
  }
}

async function loadData() {
  loading.value = true
  try {
    if (!dateRange.value || dateRange.value.length !== 2) return
    const [s, e] = dateRange.value
    const [resData, resRes] = await Promise.all([
      getGanttData(s, e, viewMode.value),
      getGanttResources(viewMode.value)
    ])
    tasks.value = resData.data || []
    resources.value = resRes.data || []
    if (tasks.value.length > 0) {
      const batches = [...new Set(tasks.value.map(t => t.scheduleBatchNo))].filter(Boolean)
      if (batches.length > 0) currentBatchNo.value = batches[batches.length - 1]
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function checkConflictsAction() {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请先选择日期范围')
    return
  }
  try {
    const [s, e] = dateRange.value
    const res = await checkConflicts(s, e)
    const list = res.data || []
    if (list.length === 0) {
      ElMessage.success('未检测到排程冲突')
    } else {
      let msg = `检测到 ${list.length} 个冲突：\n`
      list.slice(0, 5).forEach((c, i) => {
        msg += `${i + 1}. ${c.conflictType} - ${c.resourceName}: ${c.taskName1} 与 ${c.taskName2}\n`
      })
      if (list.length > 5) msg += `...还有 ${list.length - 5} 个`
      ElMessageBox.alert(msg, '冲突警告', { type: 'warning', dangerouslyUseHTMLString: false })
    }
  } catch (e) {
    console.error(e)
  }
}

async function doPublish() {
  if (!currentBatchNo.value) {
    ElMessage.warning('没有可发布的排程批次')
    return
  }
  try {
    await ElMessageBox.confirm(`确认发布批次 ${currentBatchNo.value} 的排程？发布后将同步到仪器日历。`, '确认发布', { type: 'warning' })
    await publishSchedule(currentBatchNo.value)
    ElMessage.success('发布成功')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '发布失败')
  }
}

function formatDay(d) {
  return `${d.getMonth() + 1}/${d.getDate()}`
}

function formatWeek(d) {
  return ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][d.getDay()]
}

function isWeekend(d) {
  return d.getDay() === 0 || d.getDay() === 6
}

function formatDateTime(dt) {
  if (!dt) return ''
  const d = new Date(dt)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function getTasksByResourceAndDay(res, day) {
  const dayStart = new Date(day.getFullYear(), day.getMonth(), day.getDate()).getTime()
  const dayEnd = dayStart + 24 * 3600 * 1000
  return tasks.value.filter(t => {
    let matchRes = false
    if (viewMode.value === 'instrument') matchRes = t.instrumentId === res.id
    else matchRes = t.detectPersonId + 1000000 === res.id || (!t.detectPersonId && res.type === 'person')
    if (!matchRes) return false
    const tStart = new Date(t.start).getTime()
    const tEnd = new Date(t.end).getTime()
    return tStart < dayEnd && tEnd > dayStart
  })
}

function getTaskClass(task) {
  const status = task.status
  if (status === 'COMPLETED') return 'task-completed'
  if (status === 'IN_PROGRESS') return 'task-progress'
  if (task.priority === 'HIGH') return 'task-high'
  if (task.priority === 'LOW') return 'task-low'
  return 'task-medium'
}

function getTaskStyle(task, day) {
  const dayStart = new Date(day.getFullYear(), day.getMonth(), day.getDate())
  const tStart = new Date(task.start)
  const tEnd = new Date(task.end)
  const dayMs = 24 * 3600 * 1000

  const effStart = Math.max(tStart.getTime(), dayStart.getTime())
  const effEnd = Math.min(tEnd.getTime(), dayStart.getTime() + dayMs)
  const leftPct = ((effStart - dayStart.getTime()) / dayMs) * 100
  const widthPct = Math.max(((effEnd - effStart) / dayMs) * 100, 8)

  return {
    left: `${leftPct}%`,
    width: `${widthPct}%`,
    background: task.color || '#3b82f6'
  }
}

function getTaskTooltip(task) {
  return `${task.name}\n仪器: ${task.instrumentName}\n人员: ${task.detectPersonName || '-'}\n时间: ${formatDateTime(task.start)} ~ ${formatDateTime(task.end)}\n时长: ${task.duration}分钟`
}

function truncate(s, n) {
  if (!s) return ''
  return s.length > n ? s.slice(0, n) + '…' : s
}

function priorityTagType(p) {
  if (p === 'HIGH') return 'danger'
  if (p === 'LOW') return 'success'
  return 'primary'
}
function priorityText(p) {
  return { HIGH: '高', MEDIUM: '中', LOW: '低' }[p] || p
}

function handleDragStart(e, task) {
  if (!task.canDrag) {
    e.preventDefault()
    return
  }
  draggingTask = task
  e.dataTransfer.effectAllowed = 'move'
}
function handleDragOver(e) {
  e.dataTransfer.dropEffect = 'move'
}
function handleDrop(e, res, day) {
  if (!draggingTask) return
  const t = draggingTask
  const dt = new Date(day.getFullYear(), day.getMonth(), day.getDate(), 9, 0)
  const end = new Date(dt.getTime() + t.duration * 60000)
  selectedTask.value = t
  adjustForm.id = t.id
  adjustForm.newStartTime = dt.toISOString().replace('Z', '')
  adjustForm.newEndTime = end.toISOString().replace('Z', '')
  adjustForm.newInstrumentId = null
  adjustForm.newDetectPersonId = null
  if (res.type === 'instrument') {
    if (res.id !== t.instrumentId) {
      adjustForm.newInstrumentId = res.id
    }
  } else if (res.type === 'person') {
    const realPersonId = res.id - 1000000
    if (realPersonId !== t.detectPersonId) {
      adjustForm.newDetectPersonId = realPersonId
    }
  }
  adjustForm.reason = '拖拽调整'
  adjustVisible.value = true
  draggingTask = null
}
function handleDragEnd() {
  draggingTask = null
}

function openTaskDetail(task) {
  selectedTask.value = task
  detailVisible.value = true
}

function openAdjustDialog() {
  if (!selectedTask.value) return
  const t = selectedTask.value
  adjustForm.id = t.id
  adjustForm.newStartTime = t.start
  adjustForm.newEndTime = t.end
  adjustForm.reason = ''
  detailVisible.value = false
  adjustVisible.value = true
}

async function doAdjust() {
  if (!adjustForm.newStartTime || !adjustForm.newEndTime) {
    ElMessage.warning('请选择新的时间段')
    return
  }
  adjusting.value = true
  try {
    await adjustSchedule({ ...adjustForm })
    ElMessage.success('调整成功')
    adjustVisible.value = false
    await loadData()
  } catch (e) {
    ElMessage.error(e.message || '调整失败')
  } finally {
    adjusting.value = false
  }
}

async function doCancel() {
  if (!selectedTask.value) return
  try {
    await ElMessageBox.confirm('确认取消该排程任务？', '确认', { type: 'warning' })
    await cancelSchedule(selectedTask.value.id, '手动取消')
    ElMessage.success('取消成功')
    detailVisible.value = false
    await loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '取消失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.schedule-gantt-page {
  padding: 16px;
  background: #f5f7fa;
  min-height: 100vh;

  .toolbar-card { margin-bottom: 16px; }
  .gantt-card { margin-bottom: 16px; }

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;

    .toolbar-left { display: flex; align-items: center; flex-wrap: wrap; }

    .legend {
      display: flex;
      gap: 16px;

      .legend-item {
        display: inline-flex;
        align-items: center;
        font-size: 12px;
        color: #666;

        .dot {
          width: 10px;
          height: 10px;
          border-radius: 3px;
          margin-right: 6px;
          display: inline-block;

          &.high { background: #ef4444; }
          &.medium { background: #3b82f6; }
          &.low { background: #22c55e; }
          &.done { background: #9ca3af; }
        }
      }
    }
  }

  .gantt-container {
    display: flex;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    overflow: hidden;

    .gantt-sidebar {
      width: 260px;
      flex-shrink: 0;
      border-right: 1px solid #ebeef5;
      background: #fafafa;

      .gantt-header-cell {
        height: 60px;
        padding: 10px;
        font-weight: 600;
        border-bottom: 1px solid #ebeef5;
        display: flex;
        align-items: center;
        background: #f0f2f5;
      }

      .gantt-resource-row {
        border-bottom: 1px solid #f0f0f0;
      }

      .gantt-resource-cell {
        height: 72px;
        padding: 8px 12px;
        display: flex;
        align-items: center;
        gap: 8px;

        .resource-name {
          font-size: 13px;
          font-weight: 500;
          max-width: 120px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        .resource-code {
          font-size: 11px;
          color: #999;
        }
      }
    }

    .gantt-main {
      flex: 1;
      overflow-x: auto;
      overflow-y: hidden;

      .gantt-timeline-header {
        display: flex;
        position: sticky;
        top: 0;
        z-index: 2;
        background: #f0f2f5;
        border-bottom: 1px solid #ebeef5;

        .gantt-day-header {
          flex: 0 0 160px;
          width: 160px;
          height: 60px;
          padding: 6px;
          text-align: center;
          border-right: 1px solid #ebeef5;
          box-sizing: border-box;

          &.weekend { background: #fff4e6; }

          .day-date { font-size: 14px; font-weight: 600; }
          .day-week { font-size: 11px; color: #999; margin-top: 2px; }
        }
      }

      .gantt-body {
        .gantt-row {
          display: flex;
          height: 72px;
          border-bottom: 1px solid #f0f0f0;

          .gantt-day-cell {
            flex: 0 0 160px;
            width: 160px;
            position: relative;
            border-right: 1px solid #f5f5f5;
            box-sizing: border-box;

            &.weekend { background: #fffdf8; }

            .gantt-task {
              position: absolute;
              top: 8px;
              height: 56px;
              padding: 4px 8px;
              border-radius: 4px;
              color: #fff;
              font-size: 11px;
              cursor: pointer;
              box-sizing: border-box;
              overflow: hidden;
              transition: transform .15s ease, box-shadow .15s ease;
              display: flex;
              flex-direction: column;
              justify-content: center;
              box-shadow: 0 1px 3px rgba(0,0,0,.15);
              border: 1px solid rgba(255,255,255,.2);

              &:hover {
                transform: translateY(-1px);
                box-shadow: 0 3px 8px rgba(0,0,0,.2);
                z-index: 5;
              }

              &.draggable { cursor: move; }

              &.task-completed { background: #9ca3af !important; }
              &.task-progress { background: #f59e0b !important; }

              .task-name {
                font-weight: 600;
                line-height: 1.2;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }
              .task-person {
                margin-top: 2px;
                font-size: 10px;
                opacity: .9;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }
            }
          }
        }
      }
    }
  }

  .detail-content {
    .detail-actions {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
    }
  }
}
</style>
