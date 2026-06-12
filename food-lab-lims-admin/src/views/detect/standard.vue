<template>
  <div class="standard-container">
    <el-card shadow="hover">
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="标准名称">
            <el-input v-model="searchForm.name" placeholder="请输入" clearable />
          </el-form-item>
          <el-form-item label="食品类别">
            <el-select v-model="searchForm.foodType" placeholder="请选择" clearable style="width: 150px">
              <el-option label="粮食加工品" value="grain" />
              <el-option label="食用油" value="oil" />
              <el-option label="肉制品" value="meat" />
              <el-option label="乳制品" value="dairy" />
              <el-option label="饮料" value="beverage" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增标准
        </el-button>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
      >
        <el-table-column prop="standardNo" label="标准编号" width="160" />
        <el-table-column prop="standardName" label="标准名称" min-width="200" />
        <el-table-column prop="foodType" label="食品类别" width="120" />
        <el-table-column prop="detectItem" label="检测项目" width="120" />
        <el-table-column prop="limitValue" label="限量值" width="100" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="publishDate" label="发布日期" width="120" />
        <el-table-column prop="effectiveDate" label="实施日期" width="120" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="标准编号" prop="standardNo">
              <el-input v-model="form.standardNo" placeholder="如：GB 2762-2017" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标准名称" prop="standardName">
              <el-input v-model="form.standardName" placeholder="请输入标准名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="食品类别" prop="foodType">
              <el-select v-model="form.foodType" placeholder="请选择" style="width: 100%">
                <el-option label="粮食加工品" value="grain" />
                <el-option label="食用油" value="oil" />
                <el-option label="肉制品" value="meat" />
                <el-option label="乳制品" value="dairy" />
                <el-option label="饮料" value="beverage" />
                <el-option label="调味品" value="condiment" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检测项目" prop="detectItem">
              <el-select v-model="form.detectItem" placeholder="请选择" style="width: 100%">
                <el-option label="铅" value="铅" />
                <el-option label="镉" value="镉" />
                <el-option label="砷" value="砷" />
                <el-option label="汞" value="汞" />
                <el-option label="黄曲霉毒素B1" value="黄曲霉毒素B1" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="限量值" prop="limitValue">
              <el-input-number v-model="form.limitValue" :min="0" :precision="4" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单位" prop="unit">
              <el-select v-model="form.unit" placeholder="请选择" style="width: 100%">
                <el-option label="mg/kg" value="mg/kg" />
                <el-option label="μg/kg" value="μg/kg" />
                <el-option label="mg/L" value="mg/L" />
                <el-option label="μg/L" value="μg/L" />
                <el-option label="g/kg" value="g/kg" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="限量类型">
              <el-select v-model="form.limitType" style="width: 100%">
                <el-option label="最大限量" value="max" />
                <el-option label="最小限量" value="min" />
                <el-option label="范围" value="range" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="发布日期">
              <el-date-picker
                v-model="form.publishDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实施日期">
              <el-date-picker
                v-model="form.effectiveDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="标准描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入标准描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)

const searchForm = reactive({
  name: '',
  foodType: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: '',
  standardNo: '',
  standardName: '',
  foodType: '',
  detectItem: '',
  limitValue: 0,
  unit: '',
  limitType: 'max',
  publishDate: '',
  effectiveDate: '',
  description: ''
})

const rules = {
  standardNo: [{ required: true, message: '请输入标准编号', trigger: 'blur' }],
  standardName: [{ required: true, message: '请输入标准名称', trigger: 'blur' }],
  foodType: [{ required: true, message: '请选择食品类别', trigger: 'change' }],
  detectItem: [{ required: true, message: '请选择检测项目', trigger: 'change' }],
  limitValue: [{ required: true, message: '请输入限量值', trigger: 'blur' }],
  unit: [{ required: true, message: '请选择单位', trigger: 'change' }]
}

const tableData = ref([
  { id: 1, standardNo: 'GB 2762-2017', standardName: '食品安全国家标准 食品中污染物限量', foodType: 'grain', detectItem: '铅', limitValue: 0.2, unit: 'mg/kg', publishDate: '2017-03-17', effectiveDate: '2017-09-17' },
  { id: 2, standardNo: 'GB 2762-2017', standardName: '食品安全国家标准 食品中污染物限量', foodType: 'meat', detectItem: '铅', limitValue: 0.5, unit: 'mg/kg', publishDate: '2017-03-17', effectiveDate: '2017-09-17' },
  { id: 3, standardNo: 'GB 2761-2017', standardName: '食品安全国家标准 食品中真菌毒素限量', foodType: 'grain', detectItem: '黄曲霉毒素B1', limitValue: 5.0, unit: 'μg/kg', publishDate: '2017-03-17', effectiveDate: '2017-09-17' },
  { id: 4, standardNo: 'GB 2762-2017', standardName: '食品安全国家标准 食品中污染物限量', foodType: 'dairy', detectItem: '砷', limitValue: 0.1, unit: 'mg/kg', publishDate: '2017-03-17', effectiveDate: '2017-09-17' },
  { id: 5, standardNo: 'GB 2762-2017', standardName: '食品安全国家标准 食品中污染物限量', foodType: 'beverage', detectItem: '铅', limitValue: 0.3, unit: 'mg/L', publishDate: '2017-03-17', effectiveDate: '2017-09-17' },
  { id: 6, standardNo: 'GB 2762-2017', standardName: '食品安全国家标准 食品中污染物限量', foodType: 'oil', detectItem: '黄曲霉毒素B1', limitValue: 10.0, unit: 'μg/kg', publishDate: '2017-03-17', effectiveDate: '2017-09-17' },
  { id: 7, standardNo: 'GB 4789.2-2016', standardName: '食品安全国家标准 食品微生物学检验 菌落总数测定', foodType: 'dairy', detectItem: '铅', limitValue: 100000, unit: 'CFU/g', publishDate: '2016-12-23', effectiveDate: '2017-06-23' },
  { id: 8, standardNo: 'GB 2760-2014', standardName: '食品安全国家标准 食品添加剂使用标准', foodType: 'condiment', detectItem: '苯甲酸', limitValue: 1.0, unit: 'g/kg', publishDate: '2014-12-24', effectiveDate: '2015-05-24' }
])

pagination.total = 32

const handleSearch = () => {
  loading.value = true
  setTimeout(() => { loading.value = false }, 500)
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.foodType = ''
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增限量标准'
  Object.assign(form, {
    id: '',
    standardNo: '',
    standardName: '',
    foodType: '',
    detectItem: '',
    limitValue: 0,
    unit: '',
    limitType: 'max',
    publishDate: '',
    effectiveDate: '',
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑限量标准'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleView = (row) => {
  ElMessage.info('详情功能开发中')
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除"${row.standardName}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
  dialogVisible.value = false
}
</script>

<style lang="scss" scoped>
.standard-container {
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
