<template>
  <div class="task-container">
    <el-card shadow="hover">
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="任务编号">
            <el-input v-model="searchForm.taskNo" placeholder="请输入" clearable />
          </el-form-item>
          <el-form-item label="任务状态">
            <el-select v-model="searchForm.status" placeholder="请选择" clearable>
              <el-option label="待分配" value="pending" />
              <el-option label="检测中" value="detecting" />
              <el-option label="待审核" value="auditing" />
              <el-option label="已完成" value="completed" />
              <el-option label="已驳回" value="rejected" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="toolbar">
        <el-button type="primary" @click="handleAssign" :disabled="selectedIds.length !== 1">
          <el-icon><User /></el-icon>
          任务分配
        </el-button>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="taskNo" label="任务编号" width="150" />
        <el-table-column prop="sampleName" label="样品名称" min-width="150" />
        <el-table-column prop="detectItems" label="检测项目" min-width="180">
          <template #default="{ row }">
            <span v-if="row.detectItems && row.detectItems.length">
              {{ row.detectItems.slice(0, 2).join(', ') }}
              <span v-if="row.detectItems.length > 2">等{{ row.detectItems.length }}项</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="assignee" label="负责人" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column prop="deadline" label="截止时间" width="120" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleDetail(row)">详情</el-button>
            <el-button type="primary" link size="small" @click="handleFlow(row)">流程</el-button>
            <el-button type="primary" link size="small" @click="handleAssignOne(row)">分配</el-button>
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

    <el-dialog v-model="detailVisible" title="任务详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="任务编号">{{ currentDetail.taskNo }}</el-descriptions-item>
        <el-descriptions-item label="任务状态">
          <el-tag :type="getStatusType(currentDetail.status)" size="small">
            {{ getStatusText(currentDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="样品名称">{{ currentDetail.sampleName }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ currentDetail.assignee }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ currentDetail.deadline }}</el-descriptions-item>
        <el-descriptions-item label="检测项目" :span="2">
          <el-tag v-for="item in currentDetail.detectItems" :key="item" size="small" class="mr-10">
            {{ item }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="任务描述" :span="2">
          {{ currentDetail.description || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="flowVisible" title="流程跟踪" width="600px">
      <el-timeline>
        <el-timeline-item
          v-for="(item, index) in flowLogs"
          :key="index"
          :timestamp="item.time"
          :type="item.type"
          :icon="item.icon"
        >
          <h4>{{ item.title }}</h4>
          <p>{{ item.description }}</p>
          <p>操作人：{{ item.operator }}</p>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>

    <el-dialog v-model="assignVisible" title="任务分配" width="500px">
      <el-form :model="assignForm" :rules="assignRules" ref="assignFormRef" label-width="80px">
        <el-form-item label="任务编号">
          <span>{{ currentAssign.taskNo }}</span>
        </el-form-item>
        <el-form-item label="样品名称">
          <span>{{ currentAssign.sampleName }}</span>
        </el-form-item>
        <el-form-item label="负责人" prop="assignee">
          <el-select v-model="assignForm.assignee" placeholder="请选择负责人" style="width: 100%">
            <el-option label="张三" value="张三" />
            <el-option label="李四" value="李四" />
            <el-option label="王五" value="王五" />
            <el-option label="赵六" value="赵六" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="assignForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'

const loading = ref(false)
const detailVisible = ref(false)
const flowVisible = ref(false)
const assignVisible = ref(false)
const selectedIds = ref([])
const currentDetail = ref({})
const currentAssign = ref({})
const assignFormRef = ref(null)

const searchForm = reactive({
  taskNo: '',
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 36
})

const assignForm = reactive({
  assignee: '',
  remark: ''
})

const assignRules = {
  assignee: [{ required: true, message: '请选择负责人', trigger: 'change' }]
}

const tableData = ref([
  { id: 1, taskNo: 'TK202401001', sampleName: '牛奶样品-001', detectItems: ['菌落总数', '大肠菌群', '蛋白质'], assignee: '张三', status: 'detecting', createTime: '2024-01-15 09:30:00', deadline: '2024-01-18', description: '牛奶常规理化及微生物检测' },
  { id: 2, taskNo: 'TK202401002', sampleName: '猪肉样品-002', detectItems: ['瘦肉精', '菌落总数', '挥发性盐基氮'], assignee: '李四', status: 'pending', createTime: '2024-01-16 10:15:00', deadline: '2024-01-19', description: '' },
  { id: 3, taskNo: 'TK202401003', sampleName: '蔬菜样品-003', detectItems: ['农药残留', '重金属铅'], assignee: '王五', status: 'auditing', createTime: '2024-01-14 08:45:00', deadline: '2024-01-17', description: '蔬菜农残快速检测' },
  { id: 4, taskNo: 'TK202401004', sampleName: '饮用水-004', detectItems: ['菌落总数', '大肠菌群', 'pH值'], assignee: '赵六', status: 'completed', createTime: '2024-01-13 14:20:00', deadline: '2024-01-16', description: '' },
  { id: 5, taskNo: 'TK202401005', sampleName: '食用油-005', detectItems: ['酸价', '过氧化值', '黄曲霉毒素B1'], assignee: '张三', status: 'pending', createTime: '2024-01-17 11:00:00', deadline: '2024-01-20', description: '' },
  { id: 6, taskNo: 'TK202401006', sampleName: '大米-006', detectItems: ['重金属镉', '农药残留'], assignee: '李四', status: 'detecting', createTime: '2024-01-18 09:10:00', deadline: '2024-01-21', description: '重金属专项检测' },
  { id: 7, taskNo: 'TK202401007', sampleName: '鸡蛋-007', detectItems: ['氟苯尼考', '恩诺沙星'], assignee: '王五', status: 'rejected', createTime: '2024-01-12 15:30:00', deadline: '2024-01-15', description: '' },
  { id: 8, taskNo: 'TK202401008', sampleName: '水果-008', detectItems: ['农药残留'], assignee: '赵六', status: 'completed', createTime: '2024-01-11 10:45:00', deadline: '2024-01-14', description: '' },
  { id: 9, taskNo: 'TK202401009', sampleName: '酱油-009', detectItems: ['氨基酸态氮', '苯甲酸'], assignee: '张三', status: 'pending', createTime: '2024-01-19 13:20:00', deadline: '2024-01-22', description: '' },
  { id: 10, taskNo: 'TK202401010', sampleName: '白酒-010', detectItems: ['酒精度', '甲醇', '铅'], assignee: '李四', status: 'detecting', createTime: '2024-01-20 16:00:00', deadline: '2024-01-23', description: '' }
])

const flowLogs = ref([
  { title: '任务创建', time: '2024-01-15 09:30:00', description: '系统创建检测任务', operator: '系统', type: 'primary', icon: 'Plus' },
  { title: '任务分配', time: '2024-01-15 10:00:00', description: '任务分配给张三', operator: '管理员', type: 'success', icon: 'User' },
  { title: '开始检测', time: '2024-01-15 14:30:00', description: '开始进行样品检测', operator: '张三', type: 'warning', icon: 'DataAnalysis' },
  { title: '检测完成', time: '2024-01-16 16:00:00', description: '所有检测项目完成', operator: '张三', type: 'primary', icon: 'CircleCheck' },
  { title: '提交审核', time: '2024-01-16 17:00:00', description: '检测结果提交审核', operator: '张三', type: 'info', icon: 'Document' }
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
    auditing: '待审核',
    completed: '已完成',
    rejected: '已驳回'
  }
  return map[status] || status
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => { loading.value = false }, 500)
}

const handleReset = () => {
  searchForm.taskNo = ''
  searchForm.status = ''
  handleSearch()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleDetail = (row) => {
  currentDetail.value = row
  detailVisible.value = true
}

const handleFlow = (row) => {
  currentDetail.value = row
  flowVisible.value = true
}

const handleAssignOne = (row) => {
  currentAssign.value = row
  assignForm.assignee = ''
  assignForm.remark = ''
  assignVisible.value = true
}

const handleAssign = () => {
  const row = tableData.value.find(item => item.id === selectedIds.value[0])
  if (row) {
    handleAssignOne(row)
  }
}

const handleAssignSubmit = async () => {
  if (!assignFormRef.value) return
  await assignFormRef.value.validate()
  ElMessage.success('分配成功')
  assignVisible.value = false
}
</script>

<style lang="scss" scoped>
.task-container {
  .search-bar {
    margin-bottom: 16px;
  }

  .toolbar {
    margin-bottom: 16px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
