<script setup lang="ts">
import { computed } from 'vue'
import { useThemeStore, resetThemeStore } from '@/composables/useThemeStore'
import { getTemplateDefaultTheme } from '@/templates/registry'
import type { ThemeConfig } from '@/templates/types'

const props = defineProps<{ templateId: string }>()
const theme = useThemeStore(props.templateId)

/** 模板未提供默认主题时不支持主题定制 */
const supported = computed(() => !!getTemplateDefaultTheme(props.templateId))

const colorFields = [
  { key: 'primary', label: '主强调色' },
  { key: 'secondary', label: '次强调色' },
  { key: 'tertiary', label: '第三点缀色' },
  { key: 'bg', label: '页面背景' },
  { key: 'ink', label: '主文字' },
] as const

type PaletteKey = (typeof colorFields)[number]['key']

function getColor(key: PaletteKey): string {
  return theme.value.palette?.[key] ?? '#ffffff'
}

function setColor(key: PaletteKey, value: string) {
  theme.value = {
    ...theme.value,
    palette: { ...theme.value.palette, [key]: value },
  }
}

const fontOptions = [
  { value: 'sans', label: '黑体（现代）' },
  { value: 'serif', label: '宋体（文雅）' },
  { value: 'rounded', label: '圆体（亲和）' },
] as const
const radiusOptions = [
  { value: 'sharp', label: '硬朗' },
  { value: 'soft', label: '柔和' },
  { value: 'round', label: '圆润' },
] as const

function setEnum(key: 'fontFamily' | 'radius', value: string) {
  theme.value = { ...theme.value, [key]: value } as ThemeConfig
}

const onReset = () => resetThemeStore(props.templateId)
</script>

<template>
  <div class="theme-panel">
    <p v-if="!supported" class="tp-tip">当前模板暂不支持主题定制</p>
    <template v-else>
      <section class="tp-section">
        <h3 class="tp-title">配色</h3>
        <label v-for="f in colorFields" :key="f.key" class="tp-row">
          <span class="tp-label">{{ f.label }}</span>
          <input
            type="color"
            class="tp-color"
            :value="getColor(f.key)"
            @input="setColor(f.key, ($event.target as HTMLInputElement).value)"
          />
          <span class="tp-value">{{ getColor(f.key) }}</span>
        </label>
      </section>

      <section class="tp-section">
        <h3 class="tp-title">字体与圆角</h3>
        <label class="tp-row">
          <span class="tp-label">字体</span>
          <select
            class="tp-select"
            :value="theme.fontFamily ?? 'sans'"
            @change="setEnum('fontFamily', ($event.target as HTMLSelectElement).value)"
          >
            <option v-for="o in fontOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
          </select>
        </label>
        <label class="tp-row">
          <span class="tp-label">圆角</span>
          <select
            class="tp-select"
            :value="theme.radius ?? 'soft'"
            @change="setEnum('radius', ($event.target as HTMLSelectElement).value)"
          >
            <option v-for="o in radiusOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
          </select>
        </label>
      </section>

      <button class="tp-reset" type="button" @click="onReset">重置为默认主题</button>
    </template>
  </div>
</template>

<style scoped>
.theme-panel {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.tp-tip {
  font-size: 13px;
  color: rgba(245, 241, 232, 0.5);
}
.tp-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.tp-title {
  margin: 0 0 4px;
  font-size: 13px;
  font-weight: 600;
  color: #d4a574;
}
.tp-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.tp-label {
  flex: 1;
  font-size: 13px;
  color: rgba(245, 241, 232, 0.75);
}
.tp-color {
  width: 32px;
  height: 24px;
  padding: 0;
  border: 1px solid rgba(245, 241, 232, 0.2);
  border-radius: 4px;
  background: transparent;
  cursor: pointer;
}
.tp-value {
  width: 64px;
  font-size: 12px;
  font-family: ui-monospace, monospace;
  color: rgba(245, 241, 232, 0.45);
}
.tp-select {
  flex: 0 0 130px;
  padding: 5px 8px;
  font-size: 13px;
  color: #f5f1e8;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(245, 241, 232, 0.2);
  border-radius: 4px;
}
.tp-reset {
  align-self: flex-start;
  padding: 7px 14px;
  font-size: 13px;
  color: rgba(245, 241, 232, 0.7);
  background: transparent;
  border: 1px solid rgba(245, 241, 232, 0.2);
  border-radius: 4px;
  cursor: pointer;
}
.tp-reset:hover {
  color: #d4a574;
  border-color: #d4a574;
}
</style>
