<template>
  <div class="report-container">
    <el-card shadow="hover">
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="报告编号">
            <el-input v-model="searchForm.reportNo" placeholder="请输入" clearable />
          </el-form-item>
          <el-form-item label="报告状态">
            <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 130px">
              <el-option label="草稿" value="draft" />
              <el-option label="待审核" value="pending" />
              <el-option label="已发布" value="published" />
              <el-option label="已作废" value="void" />
            </el-select>
          </el-form-item>
          <el-form-item label="报告类型">
            <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 130px">
              <el-option label="检测报告" value="detect" />
              <el-option label="评价报告" value="evaluation" />
              <el-option label="验证报告" value="verification" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="toolbar">
        <el-button type="primary" @click="handleGenerate">
          <el-icon><Plus /></el-icon>
          生成报告
        </el-button>
        <el-button type="success" :disabled="selectedIds.length === 0" @click="handleBatchExport">
          <el-icon><Download /></el-icon>
          批量导出
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
        <el-table-column prop="reportNo" label="报告编号" width="180" />
        <el-table-column prop="reportTitle" label="报告标题" min-width="200" />
        <el-table-column prop="sampleName" label="样品名称" width="150" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)" size="small">{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createUser" label="创建人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handlePreview(row)">预览</el-button>
            <el-button type="success" link size="small" @click="handleExport(row)">导出</el-button>
            <el-button type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="generateVisible" title="生成报告" width="600px">
      <el-form :model="generateForm" :rules="generateRules" ref="generateFormRef" label-width="100px">
        <el-form-item label="报告标题" prop="reportTitle">
          <el-input v-model="generateForm.reportTitle" placeholder="请输入报告标题" />
        </el-form-item>
        <el-form-item label="报告类型" prop="type">
          <el-select v-model="generateForm.type" placeholder="请选择" style="width: 100%">
            <el-option label="检测报告" value="detect" />
            <el-option label="评价报告" value="evaluation" />
            <el-option label="验证报告" value="verification" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联样品" prop="sampleIds">
          <el-select
            v-model="generateForm.sampleIds"
            multiple
            placeholder="请选择样品"
            style="width: 100%"
          >
            <el-option label="牛奶样品-001" value="1" />
            <el-option label="猪肉样品-002" value="2" />
            <el-option label="蔬菜样品-003" value="3" />
            <el-option label="饮用水-004" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="报告模板">
          <el-select v-model="generateForm.templateId" placeholder="请选择模板" style="width: 100%">
            <el-option label="默认检测报告模板" value="1" />
            <el-option label="食品安全检测报告模板" value="2" />
            <el-option label="委托检测报告模板" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="generateForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateVisible = false">取消</el-button>
        <el-button type="primary" @click="handleGenerateSubmit">生成</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="报告预览" width="900px" top="5vh">
      <div class="report-preview">
        <div class="report-header">
          <h1 class="report-title">{{ currentReport.reportTitle }}</h1>
          <div class="report-no">报告编号：{{ currentReport.reportNo }}</div>
        </div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="样品名称">{{ currentReport.sampleName }}</el-descriptions-item>
          <el-descriptions-item label="样品编号">{{ currentReport.reportNo }}</el-descriptions-item>
          <el-descriptions-item label="委托单位">{{ currentReport.client }}</el-descriptions-item>
          <el-descriptions-item label="检测类别">{{ getTypeText(currentReport.type) }}</el-descriptions-item>
          <el-descriptions-item label="收样日期">2024-01-15</el-descriptions-item>
          <el-descriptions-item label="报告日期">2024-01-20</el-descriptions-item>
        </el-descriptions>

        <el-divider>检测项目及结果</el-divider>

        <el-table :data="detectResults" border size="small">
          <el-table-column prop="itemName" label="检测项目" width="150" />
          <el-table-column prop="method" label="检测方法" min-width="200" />
          <el-table-column prop="result" label="检测结果" width="100" align="center" />
          <el-table-column prop="unit" label="单位" width="80" align="center" />
          <el-table-column prop="limit" label="限量值" width="100" align="center" />
          <el-table-column prop="judge" label="判定" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.judge === '合格' ? 'success' : 'danger'" size="small">
                {{ row.judge }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <el-divider />

        <div class="report-footer">
          <p>备注：本报告仅对所检样品负责</p>
          <div class="signatures">
            <div class="sig-item">
              <span>检测人：</span>
              <span class="sig-name">张三</span>
            </div>
            <div class="sig-item">
              <span>审核人：</span>
              <span class="sig-name">李四</span>
            </div>
            <div class="sig-item">
              <span>批准人：</span>
              <span class="sig-name">王五</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="success" @click="handleExport(currentReport)">导出PDF</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download } from '@element-plus/icons-vue'

const loading = ref(false)
const generateVisible = ref(false)
const previewVisible = ref(false)
const generateFormRef = ref(null)
const selectedIds = ref([])
const currentReport = ref({})

const searchForm = reactive({
  reportNo: '',
  status: '',
  type: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const generateForm = reactive({
  reportTitle: '',
  type: '',
  sampleIds: [],
  templateId: '',
  remark: ''
})

const generateRules = {
  reportTitle: [{ required: true, message: '请输入报告标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择报告类型', trigger: 'change' }],
  sampleIds: [{ required: true, message: '请选择关联样品', trigger: 'change' }]
}

const tableData = ref([
  { id: 1, reportNo: 'RP202401001', reportTitle: '牛奶样品-001 食品安全检测报告', sampleName: '牛奶样品-001', type: 'detect', status: 'published', createUser: '张三', createTime: '2024-01-20 10:30:00', client: '蒙牛乳业' },
  { id: 2, reportNo: 'RP202401002', reportTitle: '猪肉样品-002 兽药残留检测报告', sampleName: '猪肉样品-002', type: 'detect', status: 'pending', createUser: '李四', createTime: '2024-01-19 14:20:00', client: '双汇发展' },
  { id: 3, reportNo: 'RP202401003', reportTitle: '蔬菜样品-003 农药残留检测报告', sampleName: '蔬菜样品-003', type: 'evaluation', status: 'published', createUser: '王五', createTime: '2024-01-18 16:45:00', client: '新发地市场' },
  { id: 4, reportNo: 'RP202401004', reportTitle: '饮用水-004 水质检测报告', sampleName: '饮用水-004', type: 'detect', status: 'draft', createUser: '赵六', createTime: '2024-01-17 09:15:00', client: '娃哈哈' },
  { id: 5, reportNo: 'RP202401005', reportTitle: '食用油-005 质量检测报告', sampleName: '食用油-005', type: 'verification', status: 'published', createUser: '张三', createTime: '2024-01-16 11:30:00', client: '金龙鱼' },
  { id: 6, reportNo: 'RP202401006', reportTitle: '大米-006 重金属检测报告', sampleName: '大米-006', type: 'detect', status: 'void', createUser: '李四', createTime: '2024-01-15 13:50:00', client: '五常大米' },
  { id: 7, reportNo: 'RP202401007', reportTitle: '鸡蛋-007 兽药残留检测报告', sampleName: '鸡蛋-007', type: 'detect', status: 'published', createUser: '王五', createTime: '2024-01-14 15:20:00', client: '德青源' },
  { id: 8, reportNo: 'RP202401008', reportTitle: '水果-008 农药残留检测报告', sampleName: '水果-008', type: 'evaluation', status: 'pending', createUser: '赵六', createTime: '2024-01-13 10:40:00', client: '百果园' }
])

pagination.total = 42

const detectResults = ref([
  { itemName: '菌落总数', method: 'GB 4789.2-2016', result: '1.2×10³', unit: 'CFU/g', limit: '≤1×10⁴', judge: '合格' },
  { itemName: '大肠菌群', method: 'GB 4789.3-2016', result: '<10', unit: 'MPN/g', limit: '≤100', judge: '合格' },
  { itemName: '沙门氏菌', method: 'GB 4789.4-2016', result: '未检出', unit: '/25g', limit: '不得检出', judge: '合格' },
  { itemName: '蛋白质', method: 'GB 5009.5-2016', result: '3.2', unit: 'g/100g', limit: '≥2.9', judge: '合格' },
  { itemName: '铅', method: 'GB 5009.12-2017', result: '0.05', unit: 'mg/kg', limit: '≤0.05', judge: '合格' },
  { itemName: '黄曲霉毒素M1', method: 'GB 5009.24-2016', result: '<0.005', unit: 'μg/kg', limit: '≤0.5', judge: '合格' }
])

const getTypeText = (type) => {
  const map = { detect: '检测报告', evaluation: '评价报告', verification: '验证报告' }
  return map[type] || type
}

const getTypeTag = (type) => {
  const map = { detect: 'primary', evaluation: 'success', verification: 'warning' }
  return map[type] || 'info'
}

const getStatusText = (status) => {
  const map = { draft: '草稿', pending: '待审核', published: '已发布', void: '已作废' }
  return map[status] || status
}

const getStatusTag = (status) => {
  const map = { draft: 'info', pending: 'warning', published: 'success', void: 'danger' }
  return map[status] || 'info'
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => { loading.value = false }, 500)
}

const handleReset = () => {
  searchForm.reportNo = ''
  searchForm.status = ''
  searchForm.type = ''
  handleSearch()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleGenerate = () => {
  Object.assign(generateForm, {
    reportTitle: '',
    type: '',
    sampleIds: [],
    templateId: '',
    remark: ''
  })
  generateVisible.value = true
}

const handleGenerateSubmit = async () => {
  if (!generateFormRef.value) return
  await generateFormRef.value.validate()
  ElMessage.success('报告生成成功')
  generateVisible.value = false
}

const handlePreview = (row) => {
  currentReport.value = row
  previewVisible.value = true
}

const handleExport = (row) => {
  ElMessage.success(`正在导出报告：${row.reportNo}`)
}

const handleBatchExport = () => {
  ElMessage.success(`正在批量导出 ${selectedIds.value.length} 份报告`)
}

const handleEdit = (row) => {
  ElMessage.info('编辑功能开发中')
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除报告"${row.reportNo}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
}
</script>

<style lang="scss" scoped>
.report-container {
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

  .report-preview {
    padding: 20px;
    background: #fff;
    min-height: 500px;

    .report-header {
      text-align: center;
      margin-bottom: 20px;

      .report-title {
        font-size: 22px;
        font-weight: bold;
        margin-bottom: 10px;
      }

      .report-no {
        font-size: 14px;
        color: #666;
      }
    }

    .report-footer {
      margin-top: 30px;

      p {
        margin-bottom: 10px;
        font-size: 13px;
        color: #666;
      }

      .signatures {
        display: flex;
        justify-content: flex-end;
        gap: 40px;
        margin-top: 20px;

        .sig-item {
          display: flex;
          align-items: center;

          .sig-name {
            min-width: 80px;
            border-bottom: 1px solid #333;
            text-align: center;
          }
        }
      }
    }
  }
}
</style>
