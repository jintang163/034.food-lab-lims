<template>
  <div class="form-designer-page">
    <FormDesigner :templateId="templateId" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import FormDesigner from '@/components/form/FormDesigner.vue'

const route = useRoute()
const templateId = ref(null)

onMounted(() => {
  templateId.value = route.query.id
})
</script>

<style lang="scss" scoped>
.form-designer-page {
  width: 100%;
  height: 100vh;
  overflow: hidden;
}
</style>
