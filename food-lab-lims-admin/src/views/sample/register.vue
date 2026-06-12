<template>
  <div class="sample-register">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>样品登记</span>
        </div>
      </template>

      <el-steps :active="activeStep" finish-status="success" align-center>
        <el-step title="基本信息" />
        <el-step title="检测项目" />
        <el-step title="确认提交" />
      </el-steps>

      <div class="step-content">
        <div v-show="activeStep === 0">
          <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="样品名称" prop="sampleName">
                  <el-input v-model="form.sampleName" placeholder="请输入样品名称" />
                </el-form-item>
              </el-col>
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
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="样品来源">
                  <el-input v-model="form.sampleSource" placeholder="请输入样品来源" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="生产单位">
                  <el-input v-model="form.producer" placeholder="请输入生产单位" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="规格型号">
                  <el-input v-model="form.spec" placeholder="请输入规格型号" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="批次号">
                  <el-input v-model="form.batchNo" placeholder="请输入批次号" />
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
                <el-form-item label="样品数量">
                  <el-input-number v-model="form.quantity" :min="1" style="width: 100%" />
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
        </div>

        <div v-show="activeStep === 1">
          <el-form label-width="120px">
            <el-form-item label="检测类别">
              <el-radio-group v-model="detectCategory">
                <el-radio-button label="microbe">微生物检测</el-radio-button>
                <el-radio-button label="heavy">重金属检测</el-radio-button>
                <el-radio-button label="pesticide">农药残留</el-radio-button>
                <el-radio-button label="additive">食品添加剂</el-radio-button>
                <el-radio-button label="other">其他</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-form>
          
          <div class="detect-items">
            <div class="items-title">检测项目选择</div>
            <el-checkbox-group v-model="form.detectItems">
              <el-row :gutter="20">
                <el-col :span="6" v-for="item in detectItemOptions" :key="item.value">
                  <el-checkbox :label="item.label" border>
                    <span>{{ item.label }}</span>
                    <span class="price">¥{{ item.price }}</span>
                  </el-checkbox>
                </el-col>
              </el-row>
            </el-checkbox-group>
          </div>

          <div class="detect-summary">
            <el-statistic title="已选项目" :value="form.detectItems.length" />
            <el-statistic title="检测费用" :value="totalPrice" prefix="¥" />
          </div>
        </div>

        <div v-show="activeStep === 2">
          <el-descriptions title="样品信息确认" :column="2" border>
            <el-descriptions-item label="样品名称">{{ form.sampleName }}</el-descriptions-item>
            <el-descriptions-item label="样品类型">{{ getSampleTypeText(form.sampleType) }}</el-descriptions-item>
            <el-descriptions-item label="样品来源">{{ form.sampleSource }}</el-descriptions-item>
            <el-descriptions-item label="生产单位">{{ form.producer }}</el-descriptions-item>
            <el-descriptions-item label="规格型号">{{ form.spec }}</el-descriptions-item>
            <el-descriptions-item label="批次号">{{ form.batchNo }}</el-descriptions-item>
            <el-descriptions-item label="收样日期">{{ form.receiveDate }}</el-descriptions-item>
            <el-descriptions-item label="样品数量">{{ form.quantity }}份</el-descriptions-item>
            <el-descriptions-item label="检测项目" :span="2">
              <el-tag v-for="item in form.detectItems" :key="item" size="small" class="mr-10 mb-5">
                {{ item }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ form.remark }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <div class="step-actions">
        <el-button v-if="activeStep > 0" @click="prevStep">上一步</el-button>
        <el-button v-if="activeStep < 2" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-if="activeStep === 2" type="primary" @click="handleSubmit">提交登记</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'

const activeStep = ref(0)
const formRef = ref(null)
const detectCategory = ref('microbe')

const form = reactive({
  sampleName: '',
  sampleType: '',
  sampleSource: '',
  producer: '',
  spec: '',
  batchNo: '',
  receiveDate: '',
  quantity: 1,
  detectItems: [],
  remark: ''
})

const rules = {
  sampleName: [{ required: true, message: '请输入样品名称', trigger: 'blur' }],
  sampleType: [{ required: true, message: '请选择样品类型', trigger: 'change' }],
  receiveDate: [{ required: true, message: '请选择收样日期', trigger: 'change' }]
}

const detectItemOptions = ref([
  { label: '菌落总数', value: 'total_count', price: 50, category: 'microbe' },
  { label: '大肠菌群', value: 'coliform', price: 60, category: 'microbe' },
  { label: '沙门氏菌', value: 'salmonella', price: 80, category: 'microbe' },
  { label: '金黄色葡萄球菌', value: 'staphylococcus', price: 90, category: 'microbe' },
  { label: '铅', value: 'lead', price: 100, category: 'heavy' },
  { label: '砷', value: 'arsenic', price: 100, category: 'heavy' },
  { label: '镉', value: 'cadmium', price: 100, category: 'heavy' },
  { label: '汞', value: 'mercury', price: 120, category: 'heavy' },
  { label: '有机磷农药', value: 'organic_phosphorus', price: 200, category: 'pesticide' },
  { label: '有机氯农药', value: 'organic_chlorine', price: 250, category: 'pesticide' },
  { label: '拟除虫菊酯', value: 'pyrethroid', price: 180, category: 'pesticide' },
  { label: '苯甲酸钠', value: 'sodium_benzoate', price: 80, category: 'additive' },
  { label: '山梨酸钾', value: 'potassium_sorbate', price: 80, category: 'additive' },
  { label: '胭脂红', value: 'carmine', price: 70, category: 'additive' },
  { label: '黄曲霉毒素B1', value: 'aflatoxin_b1', price: 150, category: 'other' },
  { label: '酸价', value: 'acid_value', price: 60, category: 'other' }
])

const totalPrice = computed(() => {
  let total = 0
  form.detectItems.forEach(item => {
    const found = detectItemOptions.value.find(opt => opt.label === item)
    if (found) total += found.price
  })
  return total
})

const getSampleTypeText = (type) => {
  const map = {
    food: '食品',
    agricultural: '农产品',
    water: '饮用水',
    other: '其他'
  }
  return map[type] || type
}

const nextStep = async () => {
  if (activeStep.value === 0) {
    if (!formRef.value) return
    try {
      await formRef.value.validate()
      activeStep.value++
    } catch (e) {
    }
  } else if (activeStep.value === 1) {
    if (form.detectItems.length === 0) {
      ElMessage.warning('请至少选择一个检测项目')
      return
    }
    activeStep.value++
  }
}

const prevStep = () => {
  if (activeStep.value > 0) {
    activeStep.value--
  }
}

const handleSubmit = () => {
  ElMessage.success('样品登记成功！')
  form.sampleName = ''
  form.sampleType = ''
  form.detectItems = []
  activeStep.value = 0
}
</script>

<style lang="scss" scoped>
.sample-register {
  .card-header {
    font-weight: 600;
    font-size: 16px;
  }

  .step-content {
    padding: 30px 20px;
    min-height: 400px;
  }

  .detect-items {
    margin-top: 20px;

    .items-title {
      font-size: 14px;
      font-weight: 500;
      margin-bottom: 15px;
      color: var(--el-text-color-primary, #303133);
    }

    :deep(.el-checkbox) {
      margin-bottom: 12px;
      width: 100%;
    }

    .price {
      margin-left: 8px;
      color: #f56c6c;
      font-size: 12px;
    }
  }

  .detect-summary {
    display: flex;
    gap: 40px;
    margin-top: 30px;
    padding: 20px;
    background-color: var(--el-fill-color-light, #f5f7fa);
    border-radius: 8px;
  }

  .step-actions {
    display: flex;
    justify-content: center;
    gap: 20px;
    padding-top: 20px;
    border-top: 1px solid var(--el-border-color-lighter, #ebeef5);
  }
}
</style>
