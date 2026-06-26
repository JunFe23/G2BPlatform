<template>
  <div class="cts" ref="rootRef">
    <button type="button" class="cts-trigger" @click="open = !open">
      <span class="cts-trigger-text">{{ summary }}</span>
      <span class="cts-caret">▾</span>
    </button>

    <div v-if="open" class="cts-panel">
      <!-- 좌측: 중분류(상위) 목록 -->
      <ul class="cts-left">
        <li
          v-for="mid in mids"
          :key="mid"
          class="cts-mid"
          :class="{ active: mid === activeMid }"
          @click="activeMid = mid"
        >
          <span class="cts-mid-name">{{ mid }}</span>
          <span v-if="midSelectedCount(mid)" class="cts-mid-badge">{{ midSelectedCount(mid) }}</span>
        </li>
      </ul>

      <!-- 우측: 활성 중분류의 소분류(하위) 체크박스 + 전체선택 -->
      <div class="cts-right">
        <label class="cts-all">
          <input
            type="checkbox"
            :checked="isAllSelected(activeMid)"
            @change="toggleAll(activeMid)"
          />
          <strong>{{ activeMid }} 전체선택</strong>
        </label>
        <label v-for="name in subsOf(activeMid)" :key="name" class="cts-sub">
          <input type="checkbox" :checked="isSelected(name)" @change="toggleName(name)" />
          <span>{{ name }}</span>
        </label>
      </div>
    </div>

    <!-- 선택 요약 칩 -->
    <div v-if="modelValue.length > 0" class="cts-chips">
      <span v-for="name in modelValue" :key="name" class="cts-chip">
        <span class="cts-chip-label">{{ name }}</span>
        <button type="button" class="cts-chip-x" @click="toggleName(name)" aria-label="제거">×</button>
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  // { 중분류: [소분류, ...] }
  categoryMap: { type: Object, default: () => ({}) },
  // 선택된 소분류 이름 배열
  modelValue: { type: Array, default: () => [] },
  placeholder: { type: String, default: '공공조달분류 선택' },
})
const emit = defineEmits(['update:modelValue'])

const rootRef = ref(null)
const open = ref(false)
const activeMid = ref('')

const mids = computed(() => Object.keys(props.categoryMap))

// 중분류 로드(filter-options 비동기) 시 첫 항목을 활성으로
watch(
  mids,
  (v) => {
    if ((!activeMid.value || !v.includes(activeMid.value)) && v.length) activeMid.value = v[0]
  },
  { immediate: true },
)

function subsOf(mid) {
  return props.categoryMap[mid] || []
}
function isSelected(name) {
  return props.modelValue.includes(name)
}
function midSelectedCount(mid) {
  return subsOf(mid).filter((n) => isSelected(n)).length
}
function isAllSelected(mid) {
  const s = subsOf(mid)
  return s.length > 0 && s.every((n) => isSelected(n))
}
function toggleName(name) {
  if (isSelected(name)) {
    emit('update:modelValue', props.modelValue.filter((n) => n !== name))
  } else {
    emit('update:modelValue', [...props.modelValue, name])
  }
}
function toggleAll(mid) {
  const s = subsOf(mid)
  if (isAllSelected(mid)) {
    emit('update:modelValue', props.modelValue.filter((n) => !s.includes(n)))
  } else {
    const set = new Set(props.modelValue)
    s.forEach((n) => set.add(n))
    emit('update:modelValue', [...set])
  }
}

const summary = computed(() =>
  props.modelValue.length ? `${props.placeholder} · ${props.modelValue.length}개` : props.placeholder,
)

function onDocClick(e) {
  if (rootRef.value && !rootRef.value.contains(e.target)) open.value = false
}
onMounted(() => document.addEventListener('click', onDocClick))
onUnmounted(() => document.removeEventListener('click', onDocClick))
</script>

<style scoped>
.cts {
  position: relative;
  min-width: 220px;
}
.cts-trigger {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 6px 10px;
  background: #fff;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  color: #334155;
  cursor: pointer;
  font-size: 0.9em;
}
.cts-trigger-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cts-caret {
  color: #94a3b8;
}
.cts-panel {
  position: absolute;
  top: calc(100% + 4px);
  /* 필터가 우측 정렬이라 left 기준이면 화면 밖으로 잘림 → 우측 기준으로 펼침 */
  right: 0;
  left: auto;
  z-index: 30;
  display: flex;
  width: 460px;
  max-width: 90vw;
  max-height: 320px;
  background: #fff;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.16);
  overflow: hidden;
}
.cts-left {
  width: 160px;
  margin: 0;
  padding: 4px;
  list-style: none;
  overflow-y: auto;
  border-right: 1px solid #e2e8f0;
  background: #f8fafc;
}
.cts-mid {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.88em;
  color: #334155;
}
.cts-mid:hover {
  background: #eef2f7;
}
.cts-mid.active {
  background: #e0e7ff;
  color: #3730a3;
  font-weight: 600;
}
.cts-mid-badge {
  min-width: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: #4f46e5;
  color: #fff;
  font-size: 0.78em;
  text-align: center;
}
.cts-right {
  flex: 1;
  padding: 6px 10px;
  overflow-y: auto;
}
.cts-all {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 4px;
  border-bottom: 1px solid #e2e8f0;
  margin-bottom: 4px;
  color: #1d4ed8;
  cursor: pointer;
}
.cts-sub {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 4px;
  font-size: 0.88em;
  color: #334155;
  cursor: pointer;
}
.cts-sub:hover {
  background: #f8fafc;
}
.cts-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 4px;
}
.cts-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 6px 2px 8px;
  background: #e0e7ff;
  color: #3730a3;
  border-radius: 12px;
  font-size: 0.78em;
}
.cts-chip-x {
  border: 0;
  background: transparent;
  color: #4f46e5;
  cursor: pointer;
  font-size: 1.05em;
  line-height: 1;
  padding: 0;
}
.cts-chip-x:hover {
  color: #1e1b4b;
}
</style>
