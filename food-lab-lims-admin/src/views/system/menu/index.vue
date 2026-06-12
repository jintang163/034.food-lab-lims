<template>
  <div class="menu-container">
    <el-card shadow="hover">
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增菜单
        </el-button>
      </div>

      <el-table
        :data="menuTree"
        v-loading="loading"
        border
        stripe
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
      >
        <el-table-column prop="name" label="菜单名称" min-width="200" />
        <el-table-column prop="icon" label="图标" width="80">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" min-width="200" />
        <el-table-column prop="component" label="组件路径" min-width="200" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)" size="small">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="visible" label="显示" width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.visible" size="small" :active-value="'1'" :inactive-value="'0'" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'danger'" size="small">
              {{ row.status === '1' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleAddChild(row)">添加子菜单</el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="上级菜单">
              <el-tree-select
                v-model="form.parentId"
                :data="menuOptions"
                :props="{ label: 'name', value: 'id', children: 'children' }"
                check-strictly
                placeholder="请选择上级菜单"
                style="width: 100%"
                default-expand-all
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单类型" prop="type">
              <el-radio-group v-model="form.type">
                <el-radio value="M">目录</el-radio>
                <el-radio value="C">菜单</el-radio>
                <el-radio value="F">按钮</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="菜单名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入菜单名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="图标">
              <el-input v-model="form.icon" placeholder="请输入图标名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="form.type !== 'F'">
          <el-col :span="12">
            <el-form-item label="路由路径">
              <el-input v-model="form.path" placeholder="请输入路由路径" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组件路径">
              <el-input v-model="form.component" placeholder="请输入组件路径" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="权限标识">
              <el-input v-model="form.permission" placeholder="如：system:user:add" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否显示">
              <el-radio-group v-model="form.visible">
                <el-radio value="1">显示</el-radio>
                <el-radio value="0">隐藏</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio value="1">启用</el-radio>
                <el-radio value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
const formRef = ref(null)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: '',
  parentId: 0,
  type: 'C',
  name: '',
  icon: '',
  path: '',
  component: '',
  permission: '',
  sort: 0,
  visible: '1',
  status: '1',
  remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

const menuTree = ref([
  {
    id: 1,
    name: '仪表盘',
    icon: 'Odometer',
    path: '/dashboard',
    component: 'dashboard/index',
    type: 'M',
    sort: 1,
    visible: '1',
    status: '1',
    children: [
      {
        id: 11,
        name: '仪表盘首页',
        icon: '',
        path: '/dashboard',
        component: 'dashboard/index',
        type: 'C',
        sort: 1,
        visible: '1',
        status: '1'
      }
    ]
  },
  {
    id: 2,
    name: '样品管理',
    icon: 'Box',
    path: '/sample',
    component: 'layout/index',
    type: 'M',
    sort: 2,
    visible: '1',
    status: '1',
    children: [
      { id: 21, name: '样品列表', icon: 'List', path: '/sample/list', component: 'sample/index', type: 'C', sort: 1, visible: '1', status: '1' },
      { id: 22, name: '样品登记', icon: 'Plus', path: '/sample/register', component: 'sample/register', type: 'C', sort: 2, visible: '1', status: '1' }
    ]
  },
  {
    id: 3,
    name: '任务管理',
    icon: 'Tickets',
    path: '/task',
    component: 'layout/index',
    type: 'M',
    sort: 3,
    visible: '1',
    status: '1',
    children: [
      { id: 31, name: '任务列表', icon: 'List', path: '/task/list', component: 'task/index', type: 'C', sort: 1, visible: '1', status: '1' }
    ]
  },
  {
    id: 4,
    name: '检测项目管理',
    icon: 'DataAnalysis',
    path: '/detect',
    component: 'layout/index',
    type: 'M',
    sort: 4,
    visible: '1',
    status: '1',
    children: [
      { id: 41, name: '检测项目', icon: 'Operation', path: '/detect/item', component: 'detect/index', type: 'C', sort: 1, visible: '1', status: '1' },
      { id: 42, name: '限量标准', icon: 'Document', path: '/detect/standard', component: 'detect/standard', type: 'C', sort: 2, visible: '1', status: '1' }
    ]
  },
  {
    id: 5,
    name: '审核管理',
    icon: 'CircleCheck',
    path: '/audit',
    component: 'layout/index',
    type: 'M',
    sort: 5,
    visible: '1',
    status: '1',
    children: [
      { id: 51, name: '待审列表', icon: 'Clock', path: '/audit/pending', component: 'audit/index', type: 'C', sort: 1, visible: '1', status: '1' },
      { id: 52, name: '审核历史', icon: 'History', path: '/audit/history', component: 'audit/history', type: 'C', sort: 2, visible: '1', status: '1' }
    ]
  },
  {
    id: 6,
    name: '报告管理',
    icon: 'Document',
    path: '/report',
    component: 'layout/index',
    type: 'M',
    sort: 6,
    visible: '1',
    status: '1',
    children: [
      { id: 61, name: '报告列表', icon: 'List', path: '/report/list', component: 'report/index', type: 'C', sort: 1, visible: '1', status: '1' }
    ]
  },
  {
    id: 7,
    name: '系统管理',
    icon: 'Setting',
    path: '/system',
    component: 'layout/index',
    type: 'M',
    sort: 7,
    visible: '1',
    status: '1',
    children: [
      { id: 71, name: '用户管理', icon: 'User', path: '/system/user', component: 'system/user/index', type: 'C', sort: 1, visible: '1', status: '1' },
      { id: 72, name: '角色管理', icon: 'UserFilled', path: '/system/role', component: 'system/role/index', type: 'C', sort: 2, visible: '1', status: '1' },
      { id: 73, name: '菜单管理', icon: 'Menu', path: '/system/menu', component: 'system/menu/index', type: 'C', sort: 3, visible: '1', status: '1' }
    ]
  }
])

const menuOptions = ref([{ id: 0, name: '顶级菜单', children: menuTree.value }])

const getTypeText = (type) => {
  const map = { M: '目录', C: '菜单', F: '按钮' }
  return map[type] || type
}

const getTypeTag = (type) => {
  const map = { M: 'primary', C: 'success', F: 'info' }
  return map[type] || 'info'
}

const handleAdd = () => {
  dialogTitle.value = '新增菜单'
  Object.assign(form, {
    id: '',
    parentId: 0,
    type: 'C',
    name: '',
    icon: '',
    path: '',
    component: '',
    permission: '',
    sort: 0,
    visible: '1',
    status: '1',
    remark: ''
  })
  dialogVisible.value = true
}

const handleAddChild = (row) => {
  dialogTitle.value = '新增子菜单'
  Object.assign(form, {
    id: '',
    parentId: row.id,
    type: 'C',
    name: '',
    icon: '',
    path: '',
    component: '',
    permission: '',
    sort: 0,
    visible: '1',
    status: '1',
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑菜单'
  Object.assign(form, { ...row, parentId: row.parentId || 0 })
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除菜单"${row.name}"吗？`, '提示', {
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
  ElMessage.success('操作成功')
  dialogVisible.value = false
}
</script>

<style lang="scss" scoped>
.menu-container {
  .toolbar {
    margin-bottom: 16px;
  }
}
</style>
