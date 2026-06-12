<template>
  <div class="role-container">
    <el-card shadow="hover">
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="角色名称">
            <el-input v-model="searchForm.roleName" placeholder="请输入" clearable />
          </el-form-item>
          <el-form-item label="角色编码">
            <el-input v-model="searchForm.roleCode" placeholder="请输入" clearable />
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
          新增角色
        </el-button>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
      >
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="roleDesc" label="角色描述" min-width="200" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'danger'" size="small">
              {{ row.status === '1' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="handlePermission(row)">权限配置</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="1">启用</el-radio>
            <el-radio value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色描述">
          <el-input
            v-model="form.roleDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permissionVisible" title="权限配置" width="600px">
      <el-tree
        ref="treeRef"
        :data="menuTree"
        show-checkbox
        node-key="id"
        default-expand-all
        :default-checked-keys="checkedKeys"
        :props="{ label: 'name', children: 'children' }"
      />
      <template #footer>
        <el-button @click="permissionVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePermissionSubmit">确定</el-button>
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
const permissionVisible = ref(false)
const formRef = ref(null)
const treeRef = ref(null)
const checkedKeys = ref([])

const searchForm = reactive({
  roleName: '',
  roleCode: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: '',
  roleName: '',
  roleCode: '',
  roleDesc: '',
  sort: 0,
  status: '1'
})

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

const tableData = ref([
  { id: 1, roleName: '系统管理员', roleCode: 'admin', roleDesc: '拥有系统所有权限', sort: 1, status: '1', createTime: '2024-01-01 00:00:00' },
  { id: 2, roleName: '检测员', roleCode: 'tester', roleDesc: '负责样品检测工作', sort: 2, status: '1', createTime: '2024-01-02 10:00:00' },
  { id: 3, roleName: '审核员', roleCode: 'auditor', roleDesc: '负责审核检测结果和报告', sort: 3, status: '1', createTime: '2024-01-03 09:00:00' },
  { id: 4, roleName: '业务员', roleCode: 'sales', roleDesc: '负责业务对接和客户管理', sort: 4, status: '1', createTime: '2024-01-04 11:00:00' },
  { id: 5, roleName: '普通用户', roleCode: 'user', roleDesc: '基础查看权限', sort: 5, status: '1', createTime: '2024-01-05 14:00:00' }
])

pagination.total = 5

const menuTree = ref([
  {
    id: 1,
    name: '仪表盘',
    children: [
      { id: 11, name: '查看仪表盘' }
    ]
  },
  {
    id: 2,
    name: '样品管理',
    children: [
      { id: 21, name: '样品列表' },
      { id: 22, name: '样品登记' },
      { id: 23, name: '批量导入' }
    ]
  },
  {
    id: 3,
    name: '任务管理',
    children: [
      { id: 31, name: '任务列表' },
      { id: 32, name: '任务分配' }
    ]
  },
  {
    id: 4,
    name: '检测项目管理',
    children: [
      { id: 41, name: '检测项目' },
      { id: 42, name: '限量标准' }
    ]
  },
  {
    id: 5,
    name: '审核管理',
    children: [
      { id: 51, name: '待审列表' },
      { id: 52, name: '审核历史' }
    ]
  },
  {
    id: 6,
    name: '报告管理',
    children: [
      { id: 61, name: '报告列表' },
      { id: 62, name: '报告生成' }
    ]
  },
  {
    id: 7,
    name: '系统管理',
    children: [
      { id: 71, name: '用户管理' },
      { id: 72, name: '角色管理' },
      { id: 73, name: '菜单管理' }
    ]
  }
])

const handleSearch = () => {
  loading.value = true
  setTimeout(() => { loading.value = false }, 500)
}

const handleReset = () => {
  searchForm.roleName = ''
  searchForm.roleCode = ''
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增角色'
  Object.assign(form, {
    id: '',
    roleName: '',
    roleCode: '',
    roleDesc: '',
    sort: 0,
    status: '1'
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑角色'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除角色"${row.roleName}"吗？`, '提示', {
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

const handlePermission = (row) => {
  checkedKeys.value = [1, 11, 2, 21, 22, 23, 3, 31, 32]
  permissionVisible.value = true
}

const handlePermissionSubmit = () => {
  ElMessage.success('权限配置保存成功')
  permissionVisible.value = false
}
</script>

<style lang="scss" scoped>
.role-container {
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
