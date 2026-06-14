<template>
  <div class="audit-flow-chart">
    <div class="flow-header">
      <span class="flow-title">审核流程</span>
      <el-tag :type="getStatusTagType(flowData.currentStatus)" size="small">
        {{ getStatusText(flowData.currentStatus) }}
      </el-tag>
    </div>

    <div class="flow-container">
      <div
        v-for="(node, index) in flowData.nodes"
        :key="node.nodeCode"
        class="flow-node-wrapper"
      >
        <div class="flow-node" :class="getNodeClass(node)">
          <div class="node-icon">
            <el-icon v-if="node.nodeType === 'start'" :size="20"><CircleCheck /></el-icon>
            <el-icon v-else-if="node.nodeType === 'end' && node.status === 'approved'" :size="20"><SuccessFilled /></el-icon>
            <el-icon v-else-if="node.nodeType === 'end' && node.status === 'rejected'" :size="20"><CircleCloseFilled /></el-icon>
            <el-icon v-else-if="node.nodeType === 'end' && node.status === 'retest'" :size="20"><RefreshRight /></el-icon>
            <el-icon v-else-if="node.status === 'completed'" :size="20"><Select /></el-icon>
            <el-icon v-else :size="20"><Clock /></el-icon>
          </div>

          <div class="node-content">
            <div class="node-title">{{ node.nodeName }}</div>
            <div v-if="node.auditorName" class="node-auditor">{{ node.auditorName }}</div>
            <div v-if="node.auditTime" class="node-time">{{ formatTime(node.auditTime) }}</div>
            <div v-if="node.auditOpinion" class="node-opinion">
              <el-text type="info" size="small">{{ node.auditOpinion }}</el-text>
            </div>
            <div v-if="node.actionType === 'RETEST'" class="node-action">
              <el-tag type="warning" size="small">发起复测</el-tag>
            </div>
            <div v-if="node.actionType === 'REJECT'" class="node-action">
              <el-tag type="danger" size="small">驳回</el-tag>
            </div>
            <div v-if="node.retestId" class="node-retest-link">
              <el-link type="primary" :underline="false" @click="$emit('viewRetest', node.retestId)">
                查看复测记录
              </el-link>
            </div>
          </div>
        </div>

        <div v-if="index < flowData.nodes.length - 1" class="flow-connector">
          <div class="connector-line" :class="{ active: isConnectorActive(node, flowData.nodes[index + 1]) }"></div>
          <div class="connector-arrow">
            <el-icon :size="12"><ArrowDown /></el-icon>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { CircleCheck, SuccessFilled, CircleCloseFilled, RefreshRight, Select, Clock, ArrowDown } from '@element-plus/icons-vue'

const props = defineProps({
  flowData: {
    type: Object,
    default: () => ({
      processInstanceId: '',
      businessCode: '',
      currentStatus: '',
      nodes: []
    })
  }
})

defineEmits(['viewRetest'])

const getStatusTagType = (status) => {
  const map = { RUNNING: 'warning', COMPLETED: 'success' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { RUNNING: '进行中', COMPLETED: '已完成' }
  return map[status] || status
}

const getNodeClass = (node) => {
  const classMap = {
    completed: 'node-completed',
    pending: 'node-pending',
    approved: 'node-approved',
    rejected: 'node-rejected',
    retest: 'node-retest'
  }
  return classMap[node.status] || ''
}

const isConnectorActive = (currentNode, nextNode) => {
  return currentNode.status === 'completed' || currentNode.status === 'approved'
}

const formatTime = (time) => {
  if (!time) return ''
  if (typeof time === 'string') return time.replace('T', ' ').substring(0, 19)
  return time
}
</script>

<style lang="scss" scoped>
.audit-flow-chart {
  .flow-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 20px;

    .flow-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .flow-container {
    padding-left: 8px;
  }

  .flow-node-wrapper {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
  }

  .flow-node {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 12px 16px;
    border-radius: 8px;
    background: #f5f7fa;
    min-width: 280px;
    transition: all 0.3s;

    &.node-completed {
      background: #f0f9eb;
      border: 1px solid #e1f3d8;

      .node-icon {
        color: #67c23a;
      }
    }

    &.node-pending {
      background: #fdf6ec;
      border: 1px solid #faecd8;

      .node-icon {
        color: #e6a23c;
      }
    }

    &.node-approved {
      background: #f0f9eb;
      border: 1px solid #67c23a;

      .node-icon {
        color: #67c23a;
      }
    }

    &.node-rejected {
      background: #fef0f0;
      border: 1px solid #fbc4c4;

      .node-icon {
        color: #f56c6c;
      }
    }

    &.node-retest {
      background: #fdf6ec;
      border: 1px solid #e6a23c;

      .node-icon {
        color: #e6a23c;
      }
    }

    .node-icon {
      flex-shrink: 0;
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      background: #fff;
    }

    .node-content {
      flex: 1;

      .node-title {
        font-size: 14px;
        font-weight: 500;
        color: #303133;
        margin-bottom: 4px;
      }

      .node-auditor {
        font-size: 13px;
        color: #606266;
      }

      .node-time {
        font-size: 12px;
        color: #909399;
        margin-top: 2px;
      }

      .node-opinion {
        margin-top: 6px;
        padding: 4px 8px;
        background: #fff;
        border-radius: 4px;
      }

      .node-action {
        margin-top: 4px;
      }

      .node-retest-link {
        margin-top: 4px;
      }
    }
  }

  .flow-connector {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 0 0 0 22px;
    height: 32px;

    .connector-line {
      width: 2px;
      flex: 1;
      background: #dcdfe6;

      &.active {
        background: #67c23a;
      }
    }

    .connector-arrow {
      color: #909399;
    }
  }
}
</style>
