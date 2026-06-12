<template>
  <div class="user-container">
    <el-card shadow="hover">
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="用户名">
            <el-input v-model="searchForm.username" placeholder="请输入" clearable />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="searchForm.phone" placeholder="请输入" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
              <el-option label="启用" value="1" />
              <el-option label="禁用" value="0" />
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
          新增用户
        </el-button>
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon>
          批量删除
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
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="roles" label="角色" min-width="150">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role" size="small" class="mr-5">
              {{ role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'danger'" size="small">
              {{ row.status === '1' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="handleRole(row)">分配角色</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" placeholder="请输入昵称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="密码" prop="password" v-if="!isEdit">
              <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门">
              <el-select v-model="form.deptId" placeholder="请选择部门" style="width: 100%">
                <el-option label="检测部" value="1" />
                <el-option label="质量部" value="2" />
                <el-option label="业务部" value="3" />
                <el-option label="行政部" value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="1">启用</el-radio>
            <el-radio value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleVisible" title="分配角色" width="500px">
      <el-form label-width="80px">
        <el-form-item label="用户名">{{ currentUser.username }}</el-form-item>
        <el-form-item label="昵称">{{ currentUser.nickname }}</el-form-item>
        <el-form-item label="角色">
          <el-checkbox-group v-model="selectedRoles">
            <el-checkbox v-for="role in roleOptions" :key="role.id" :label="role.name">
              {{ role.name }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRoleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const roleVisible = ref(false)
const formRef = ref(null)
const selectedIds = ref([])
const currentUser = ref({})
const selectedRoles = ref([])

const searchForm = reactive({
  username: '',
  phone: '',
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: '',
  username: '',
  nickname: '',
  email: '',
  phone: '',
  password: '',
  deptId: '',
  status: '1',
  remark: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const roleOptions = ref([
  { id: 1, name: '系统管理员', code: 'admin' },
  { id: 2, name: '检测员', code: 'tester' },
  { id: 3, name: '审核员', code: 'auditor' },
  { id: 4, name: '业务员', code: 'sales' },
  { id: 5, name: '普通用户', code: 'user' }
])

const tableData = ref([
  { id: 1, username: 'admin', nickname: '系统管理员', email: 'admin@example.com', phone: '13800138001', deptName: '行政部', roles: ['系统管理员'], status: '1', createTime: '2024-01-01 00:00:00' },
  { id: 2, username: 'zhangsan', nickname: '张三', email: 'zhangsan@example.com', phone: '13800138002', deptName: '检测部', roles: ['检测员'], status: '1', createTime: '2024-01-02 10:30:00' },
  { id: 3, username: 'lisi', nickname: '李四', email: 'lisi@example.com', phone: '13800138003', deptName: '质量部', roles: ['审核员'], status: '1', createTime: '2024-01-03 09:15:00' },
  { id: 4, username: 'wangwu', nickname: '王五', email: 'wangwu@example.com', phone: '13800138004', deptName: '检测部', roles: ['检测员'], status: '1', createTime: '2024-01-04 14:20:00' },
  { id: 5, username: 'zhaoliu', nickname: '赵六', email: 'zhaoliu@example.com', phone: '13800138005', deptName: '业务部', roles: ['业务员'], status: '0', createTime: '2024-01-05 11:45:00' },
  { id: 6, username: 'sunqi', nickname: '孙七', email: 'sunqi@example.com', phone: '13800138006', deptName: '检测部', roles: ['检测员', '审核员'], status: '1', createTime: '2024-01-06 08:30:00' },
  { id: 7, username: 'zhouba', nickname: '周八', email: 'zhouba@example.com', phone: '13800138007', deptName: '质量部', roles: ['审核员'], status: '1', createTime: '2024-01-07 13:50:00' },
  { id: 8, username: 'wujiu', nickname: '吴九', email: 'wujiu@example.com', phone: '13800138008', deptName: '行政部', roles: ['普通用户'], status: '1', createTime: '2024-01-08 10:10:00' }
])

pagination.total = 28

const handleSearch = () => {
  loading.value = true
  setTimeout(() => { loading.value = false }, 500)
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.phone = ''
  searchForm.status = ''
  handleSearch()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  Object.assign(form, {
    id: '',
    username: '',
    nickname: '',
    email: '',
    phone: '',
    password: '',
    deptId: '',
    status: '1',
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户"${row.username}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个用户吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('批量删除成功')
    selectedIds.value = []
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
  dialogVisible.value = false
}

const handleRole = (row) => {
  currentUser.value = row
  selectedRoles.value = [...row.roles]
  roleVisible.value = true
}

const handleRoleSubmit = () => {
  ElMessage.success('角色分配成功')
  roleVisible.value = false
}
</script>

<style lang="scss" scoped>
.user-container {
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
