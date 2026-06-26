<template>
  <div class="msc">
    <div class="msc-input-wrap">
      <input
        type="text"
        v-model="search"
        :placeholder="placeholder"
        @input="open = true"
        @focus="open = true"
        @blur="closeLater"
      />
      <div v-if="open && filtered.length > 0" class="msc-option-list">
        <button
          v-for="opt in filtered"
          :key="opt"
          type="button"
          class="msc-option"
          :class="{ 'is-selected': isSelected(opt) }"
          @mousedown.prevent="toggle(opt)"
        >
          {{ opt }}
        </button>
      </div>
    </div>
    <div v-if="modelValue.length > 0" class="msc-chips">
      <span v-for="opt in modelValue" :key="opt" class="msc-chip">
        <span class="msc-chip-label">{{ opt }}</span>
        <button type="button" class="msc-chip-remove" @click="remove(opt)" aria-label="제거">×</button>
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  options: { type: Array, default: () => [] },
  modelValue: { type: Array, default: () => [] },
  placeholder: { type: String, default: '검색' },
})
const emit = defineEmits(['update:modelValue'])

const search = ref('')
const open = ref(false)
let hideTimer = null

// 검색어로 옵션 필터 (대소문자 무시 부분일치). 옵션은 소수라 클라이언트 필터.
const filtered = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return props.options
  return props.options.filter((o) => String(o).toLowerCase().includes(q))
})

function isSelected(opt) {
  return props.modelValue.includes(opt)
}

function toggle(opt) {
  if (isSelected(opt)) {
    emit('update:modelValue', props.modelValue.filter((o) => o !== opt))
  } else {
    emit('update:modelValue', [...props.modelValue, opt])
  }
  // 다음 선택을 위해 검색어 초기화, 드롭다운은 열린 채 유지(mousedown.prevent로 포커스 유지)
  search.value = ''
}

function remove(opt) {
  emit('update:modelValue', props.modelValue.filter((o) => o !== opt))
}

function closeLater() {
  if (hideTimer) clearTimeout(hideTimer)
  hideTimer = setTimeout(() => {
    open.value = false
  }, 150)
}
</script>

<style scoped>
.msc {
  min-width: 180px;
}
.msc-input-wrap {
  position: relative;
}
.msc input[type='text'] {
  box-sizing: border-box;
  width: 100%;
}
.msc-option-list {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  z-index: 20;
  max-height: 260px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.14);
}
.msc-option {
  display: block;
  width: 100%;
  padding: 8px 10px;
  border: 0;
  background: #fff;
  color: #334155;
  text-align: left;
  cursor: pointer;
  font-weight: 600;
}
.msc-option:hover {
  background: #f1f5f9;
}
.msc-option.is-selected {
  background: #eff6ff;
}
.msc-option.is-selected::after {
  content: ' ✓';
  color: #2563eb;
}
.msc-chips {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 4px;
  margin-top: 4px;
}
.msc-chip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  padding: 3px 6px 3px 8px;
  background: #e0e7ff;
  color: #3730a3;
  border-radius: 6px;
  font-size: 0.8em;
  line-height: 1.4;
}
.msc-chip-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.msc-chip-remove {
  border: 0;
  background: transparent;
  color: #4f46e5;
  cursor: pointer;
  font-size: 1.1em;
  line-height: 1;
  padding: 0;
}
.msc-chip-remove:hover {
  color: #1e1b4b;
}
</style>
