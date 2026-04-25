<template>
  <div>
    <router-view />
  </div>
</template>

<script setup>
import { get } from "@/net";
import { useStore } from "@/stores";
import router from "./router";

const store = useStore()
if (store.useStore == null) {
  get('/api/user/me', (message) => {
    store.auth.user = message
    router.push('/index')
  }, () => {
    store.auth.user = null
  })
}


</script>

<style scoped></style>