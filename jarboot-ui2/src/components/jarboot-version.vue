<template>
  <div class="version-title" v-show="store.version">
    <el-button link icon="Service" type="primary" @click="state.dialog = true">{{ $t('HELP') }}</el-button>
    <el-dialog v-model="state.dialog" :title="$t('HELP')" width="680px">
      <el-form label-suffix=":" label-width="auto">
        <el-form-item :label="$t('SYS_VER')">
          <span>v{{ store.version + (store.inDocker ? '(Docker)' : '') }}</span>
        </el-form-item>
        <el-form-item :label="$t('CLUSTER_MODE')">
          <span>{{ store.host ? $t('YES') : $t('NO') }}</span>
        </el-form-item>
        <el-form-item :label="$t('MACHINE_CODE')">
          <span>{{ store.machineCode }}</span>
        </el-form-item>
        <el-form-item :label="$t('MENU_DOCS')">
          <el-link :href="DOCS_URL" type="primary" target="_blank">{{ DOCS_URL }}</el-link>
        </el-form-item>
        <el-form-item label="API">
          <el-link :href="getApiUrl()" type="primary" target="_blank">{{ getApiUrl() }}</el-link>
        </el-form-item>
        <el-form-item :label="$t('CLI_DOWNLOAD')">
          <el-link :href="state.cliDownloadUrl" type="primary" target="_blank">client-tools.zip</el-link>
        </el-form-item>
        <el-form-item :label="$t('OS')">
          <span>{{ store.os }}</span>
        </el-form-item>
        <el-form-item label="JAVA">
          <span>{{ store.jdk }}</span>
        </el-form-item>
        <el-form-item v-if="store.dev" label="DEV">
          <span>{{ store.dev ? $t('YES') : $t('NO') }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="state.dialog = false">{{ $t('CLOSE') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { useBasicStore } from '@/stores';
import { reactive } from 'vue';
import { DOCS_URL } from '@/common/CommonConst';

const store = useBasicStore();
const state = reactive({
  dialog: false,
  cliDownloadUrl: `${window.location.protocol}//${window.location.host}/api/jarboot/public/serverRuntime/client-tools.zip`,
});

function getApiUrl(): string {
  if (store.dev) {
    return `${window.location.protocol}//${window.location.hostname}:9899/index.html`;
  }
  return `${window.location.protocol}//${window.location.host}/api-doc/index.html`;
}
</script>

<style scoped>
.version-title {
  color: var(--el-text-color-regular);
  display: inline-block;
  font-size: var(--el-font-size-small);
}
</style>
