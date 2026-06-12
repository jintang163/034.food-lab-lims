<template>
  <div class="sample-container">
    <el-card shadow="hover">
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="样品编号">
            <el-input v-model="searchForm.sampleNo" placeholder="请输入样品编号" clearable />
          </el-form-item>
          <el-form-item label="样品名称">
            <el-input v-model="searchForm.sampleName" placeholder="请输入样品名称" clearable />
          </el-form-item>
          <el-form-item label="样品类型">
            <el-select v-model="searchForm.sampleType" placeholder="请选择" clearable>
              <el-option label="食品" value="food" />
              <el-option label="农产品" value="agricultural" />
              <el-option label="饮用水" value="water" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择" clearable>
              <el-option label="待检测" value="pending" />
              <el-option label="检测中" value="detecting" />
              <el-option label="已完成" value="completed" />
              <el-option label="已审核" value="audited" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          样品登记
        </el-button>
        <el-button type="success" @click="handleImport">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
        <el-button type="warning" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>

      <el-table
        ref="tableRef"
        :data="tableData"
        v-loading="loading"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="sampleNo" label="样品编号" width="150" />
        <el-table-column prop="sampleName" label="样品名称" min-width="150" />
        <el-table-column prop="sampleType" label="样品类型" width="100">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ getSampleTypeText(row.sampleType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sampleSource" label="样品来源" min-width="120" />
        <el-table-column prop="detectItems" label="检测项目" min-width="150">
          <template #default="{ row }">
            <span v-if="row.detectItems && row.detectItems.length">
              {{ row.detectItems.slice(0, 3).join(', ') }}
              <span v-if="row.detectItems.length > 3">等{{ row.detectItems.length }}项</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="receiveDate" label="收样日期" width="120" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="样品编号" prop="sampleNo">
              <el-input v-model="form.sampleNo" :disabled="isEdit" placeholder="自动生成" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="样品名称" prop="sampleName">
              <el-input v-model="form.sampleName" placeholder="请输入样品名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="样品类型" prop="sampleType">
              <el-select v-model="form.sampleType" placeholder="请选择" style="width: 100%">
                <el-option label="食品" value="food" />
                <el-option label="农产品" value="agricultural" />
                <el-option label="饮用水" value="water" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="样品来源" prop="sampleSource">
              <el-input v-model="form.sampleSource" placeholder="请输入样品来源" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生产单位">
              <el-input v-model="form.producer" placeholder="请输入生产单位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格型号">
              <el-input v-model="form.spec" placeholder="请输入规格型号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="收样日期" prop="receiveDate">
              <el-date-picker
                v-model="form.receiveDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检测项目" prop="detectItems">
              <el-select
                v-model="form.detectItems"
                multiple
                placeholder="请选择检测项目"
                style="width: 100%"
              >
                <el-option label="菌落总数" value="total_count" />
                <el-option label="大肠菌群" value="coliform" />
                <el-option label="沙门氏菌" value="salmonella" />
                <el-option label="金黄色葡萄球菌" value="staphylococcus" />
                <el-option label="铅" value="lead" />
                <el-option label="砷" value="arsenic" />
                <el-option label="农药残留" value="pesticide" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="样品详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="样品编号">{{ currentDetail.sampleNo }}</el-descriptions-item>
        <el-descriptions-item label="样品名称">{{ currentDetail.sampleName }}</el-descriptions-item>
        <el-descriptions-item label="样品类型">{{ getSampleTypeText(currentDetail.sampleType) }}</el-descriptions-item>
        <el-descriptions-item label="样品来源">{{ currentDetail.sampleSource }}</el-descriptions-item>
        <el-descriptions-item label="生产单位">{{ currentDetail.producer }}</el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ currentDetail.spec }}</el-descriptions-item>
        <el-descriptions-item label="收样日期">{{ currentDetail.receiveDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentDetail.status)" size="small">
            {{ getStatusText(currentDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="检测项目" :span="2">
          <el-tag v-for="item in currentDetail.detectItems" :key="item" size="small" class="mr-10">
            {{ item }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentDetail.remark }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, Plus, Upload, Download, Delete
} from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const detailVisible = ref(false)
const tableRef = ref(null)
const formRef = ref(null)
const selectedIds = ref([])
const currentDetail = ref({})

const searchForm = reactive({
  sampleNo: '',
  sampleName: '',
  sampleType: '',
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: '',
  sampleNo: '',
  sampleName: '',
  sampleType: '',
  sampleSource: '',
  producer: '',
  spec: '',
  receiveDate: '',
  detectItems: [],
  remark: ''
})

const rules = {
  sampleName: [{ required: true, message: '请输入样品名称', trigger: 'blur' }],
  sampleType: [{ required: true, message: '请选择样品类型', trigger: 'change' }],
  receiveDate: [{ required: true, message: '请选择收样日期', trigger: 'change' }],
  detectItems: [{ required: true, message: '请选择检测项目', trigger: 'change' }]
}

const tableData = ref([
  { id: 1, sampleNo: 'SP202401001', sampleName: '牛奶样品-001', sampleType: 'food', sampleSource: '蒙牛乳业', producer: '蒙牛乳业（集团）股份有限公司', spec: '250ml/盒', detectItems: ['菌落总数', '大肠菌群', '蛋白质'], status: 'pending', receiveDate: '2024-01-15', remark: '常规检测' },
  { id: 2, sampleNo: 'SP202401002', sampleName: '猪肉样品-002', sampleType: 'food', sampleSource: '双汇发展', producer: '河南双汇投资发展股份有限公司', spec: '500g/份', detectItems: ['瘦肉精', '菌落总数', '挥发性盐基氮'], status: 'detecting', receiveDate: '2024-01-16', remark: '' },
  { id: 3, sampleNo: 'SP202401003', sampleName: '蔬菜样品-003', sampleType: 'agricultural', sampleSource: '新发地市场', producer: '北京新发地农产品批发市场', spec: '1kg/份', detectItems: ['农药残留', '重金属铅'], status: 'completed', receiveDate: '2024-01-14', remark: '蔬菜农残快速检测' },
  { id: 4, sampleNo: 'SP202401004', sampleName: '饮用水-004', sampleType: 'water', sampleSource: '娃哈哈', producer: '杭州娃哈哈集团有限公司', spec: '550ml/瓶', detectItems: ['菌落总数', '大肠菌群', 'pH值'], status: 'audited', receiveDate: '2024-01-13', remark: '' },
  { id: 5, sampleNo: 'SP202401005', sampleName: '食用油-005', sampleType: 'food', sampleSource: '金龙鱼', producer: '益海嘉里金龙鱼粮油食品股份有限公司', spec: '5L/桶', detectItems: ['酸价', '过氧化值', '黄曲霉毒素B1'], status: 'pending', receiveDate: '2024-01-17', remark: '' },
  { id: 6, sampleNo: 'SP202401006', sampleName: '大米-006', sampleType: 'agricultural', sampleSource: '五常大米', producer: '五常市大米协会', spec: '10kg/袋', detectItems: ['重金属镉', '农药残留'], status: 'detecting', receiveDate: '2024-01-18', remark: '重金属专项检测' },
  { id: 7, sampleNo: 'SP202401007', sampleName: '鸡蛋-007', sampleType: 'food', sampleSource: '德青源', producer: '北京德青源农业科技股份有限公司', spec: '30枚/盒', detectItems: ['氟苯尼考', '恩诺沙星'], status: 'completed', receiveDate: '2024-01-12', remark: '' },
  { id: 8, sampleNo: 'SP202401008', sampleName: '水果-008', sampleType: 'agricultural', sampleSource: '百果园', producer: '深圳百果园实业发展有限公司', spec: '1kg/份', detectItems: ['农药残留'], status: 'audited', receiveDate: '2024-01-11', remark: '' },
  { id: 9, sampleNo: 'SP202401009', sampleName: '酱油-009', sampleType: 'food', sampleSource: '海天味业', producer: '佛山市海天调味食品股份有限公司', spec: '500ml/瓶', detectItems: ['氨基酸态氮', '苯甲酸'], status: 'pending', receiveDate: '2024-01-19', remark: '' },
  { id: 10, sampleNo: 'SP202401010', sampleName: '白酒-010', sampleType: 'food', sampleSource: '茅台', producer: '贵州茅台酒股份有限公司', spec: '500ml/瓶', detectItems: ['酒精度', '甲醇', '铅'], status: 'detecting', receiveDate: '2024-01-20', remark: '' }
])

pagination.total = 56

const getSampleTypeText = (type) => {
  const map = {
    food: '食品',
    agricultural: '农产品',
    water: '饮用水',
    other: '其他'
  }
  return map[type] || type
}

const getStatusType = (status) => {
  const map = {
    pending: 'info',
    detecting: 'warning',
    completed: 'primary',
    audited: 'success'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    pending: '待检测',
    detecting: '检测中',
    completed: '已完成',
    audited: '已审核'
  }
  return map[status] || status
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

const handleReset = () => {
  searchForm.sampleNo = ''
  searchForm.sampleName = ''
  searchForm.sampleType = ''
  searchForm.status = ''
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '样品登记'
  Object.assign(form, {
    id: '',
    sampleNo: '',
    sampleName: '',
    sampleType: '',
    sampleSource: '',
    producer: '',
    spec: '',
    receiveDate: '',
    detectItems: [],
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑样品'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleView = (row) => {
  currentDetail.value = row
  detailVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该样品吗？此操作不可恢复', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条样品吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('批量删除成功')
    selectedIds.value = []
  }).catch(() => {})
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleImport = () => {
  ElMessage.info('批量导入功能开发中')
}

const handleExport = () => {
  ElMessage.success('导出成功')
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  ElMessage.success(isEdit.value ? '更新成功' : '登记成功')
  dialogVisible.value = false
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
}
</script>

<style lang="scss" scoped>
.sample-container {
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
