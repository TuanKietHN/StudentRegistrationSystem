<template>
  <v-card>
    <v-card-text>
      <PageHeader title="Quản trị Users">
        <template #actions>
          <v-btn v-if="isAdmin" color="primary" variant="flat" @click="openCreateDialog">Thêm user</v-btn>
        </template>
      </PageHeader>
      <v-row>
        <v-col cols="12" md="6">
          <v-text-field
              v-model="keyword"
              label="Tìm kiếm theo username/email..."
              @update:model-value="handleSearch"
          />
        </v-col>
        <v-col cols="12" md="6">
          <v-select
              v-model="roleFilter"
              :items="roleOptions"
              label="Lọc theo role"
              clearable
              @update:model-value="handleSearch"
          />
        </v-col>
      </v-row>

      <v-progress-linear v-if="loading" indeterminate class="mb-4" />

      <v-table>
        <thead>
        <tr>
          <th>ID</th>
          <th>Username</th>
          <th>Email</th>
          <th>Role</th>
          <th>Hành động</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="u in users" :key="u.id">
          <td>{{ u.id }}</td>
          <td>{{ u.username }}</td>
          <td>{{ u.email }}</td>
          <td>
            <v-chip v-for="r in splitRoles(u.role)" :key="r" size="small" variant="tonal" class="mr-1 mb-1">
              {{ r }}
            </v-chip>
          </td>
          <td>
            <v-btn size="small" variant="text" :disabled="!isAdmin" @click="openEditDialog(u)">Sửa</v-btn>
            <v-btn size="small" color="error" variant="text" :disabled="!isAdmin" @click="openDeleteDialog(u)">Xóa</v-btn>
          </td>
        </tr>
        <tr v-if="users.length === 0">
          <td colspan="5" class="text-center py-6">Không có dữ liệu</td>
        </tr>
        </tbody>
      </v-table>

      <div class="d-flex align-center justify-center mt-4">
        <v-btn :disabled="page === 1" variant="text" @click="changePage(page - 1)">Trước</v-btn>
        <div class="mx-4">Trang {{ page }} / {{ totalPages }}</div>
        <v-btn :disabled="page === totalPages" variant="text" @click="changePage(page + 1)">Sau</v-btn>
      </div>
    </v-card-text>
  </v-card>

  <v-dialog v-model="dialogOpen" max-width="760">
    <v-card>
      <v-card-title class="text-h6">
        {{ editingId ? 'Cập nhật user' : 'Thêm user' }}
      </v-card-title>
      <v-card-text>
        <v-form ref="formRef" @submit.prevent="saveUser">
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.username" label="Username" :disabled="!!editingId" :rules="editingId ? [] : rules.username" />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.email" label="Email" :rules="rules.email" />
            </v-col>
            <v-col cols="12" md="6">
              <v-select v-model="form.role" :items="roleOptions" label="Role" :rules="rules.role" />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                  v-model="form.password"
                  label="Mật khẩu"
                  :type="showPassword ? 'text' : 'password'"
                  autocomplete="new-password"
                  :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
                  @click:append-inner="showPassword = !showPassword"
                  :rules="editingId ? [] : rules.password"
              />
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>
      <v-card-actions class="justify-end">
        <v-btn variant="text" @click="dialogOpen = false">Hủy</v-btn>
        <v-btn color="primary" variant="flat" :loading="saving" @click="saveUser">Lưu</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <ConfirmDialog
      v-model="deleteOpen"
      title="Xóa user"
      :text="`Bạn có chắc chắn muốn xóa user ${deleting?.username || ''} (${deleting?.email || ''}) không?`"
      :loading="deletingLoading"
      @confirm="confirmDelete"
  />
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { userService, type UserSummary } from '@/api/services/user.service'
import PageHeader from '@/components/ui/PageHeader.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'

const authStore = useAuthStore()
const uiStore = useUiStore()
const isAdmin = computed(() => (authStore.currentUser?.role || '').split(',').includes('ADMIN'))

const users = ref<UserSummary[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const totalPages = ref(1)
const keyword = ref('')
const roleFilter = ref<string | null>(null)
let searchTimeout: any = null

const roleOptions = ['ADMIN', 'TEACHER', 'STUDENT']

const dialogOpen = ref(false)
const deleteOpen = ref(false)
const saving = ref(false)
const deletingLoading = ref(false)
const editingId = ref<number | null>(null)
const deleting = ref<UserSummary | null>(null)
const formRef = ref<any>(null)

const form = ref({
  username: '',
  email: '',
  role: 'STUDENT',
  password: ''
})
const showPassword = ref(false)

const rules = {
  username: [(v: string) => !!v || 'Username không được để trống'],
  email: [(v: string) => !!v || 'Email không được để trống'],
  role: [(v: string) => !!v || 'Role không được để trống'],
  password: [(v: string) => !!v || 'Mật khẩu không được để trống']
}

const splitRoles = (role: string) => {
  return (role || '')
      .split(',')
      .map(s => s.trim())
      .filter(Boolean)
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await userService.getAll({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      role: roleFilter.value || undefined
    })
    const data = res.data.data
    users.value = data.data
    totalPages.value = data.totalPages
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    page.value = 1
    fetchUsers()
  }, 350)
}

const changePage = (newPage: number) => {
  if (newPage > 0 && newPage <= totalPages.value) {
    page.value = newPage
    fetchUsers()
  }
}

const resetForm = () => {
  form.value = {
    username: '',
    email: '',
    role: 'STUDENT',
    password: ''
  }
  showPassword.value = false
}

const openCreateDialog = () => {
  if (!isAdmin.value) return
  editingId.value = null
  resetForm()
  dialogOpen.value = true
}

const openEditDialog = (u: UserSummary) => {
  if (!isAdmin.value) return
  editingId.value = u.id
  form.value = {
    username: u.username,
    email: u.email,
    role: splitRoles(u.role)[0] || 'STUDENT',
    password: ''
  }
  showPassword.value = false
  dialogOpen.value = true
}

const saveUser = async () => {
  if (!isAdmin.value) return
  const res = await formRef.value?.validate?.()
  if (res && res.valid === false) return

  saving.value = true
  try {
    if (editingId.value) {
      const payload: any = {
        email: form.value.email,
        role: form.value.role
      }
      if (form.value.password) payload.password = form.value.password
      await userService.update(editingId.value, payload)
      uiStore.notify('Cập nhật user thành công', 'success')
    } else {
      await userService.create({
        username: form.value.username,
        email: form.value.email,
        password: form.value.password,
        role: form.value.role
      })
      uiStore.notify('Tạo user thành công', 'success')
    }
    dialogOpen.value = false
    fetchUsers()
  } catch (err: any) {
    const msg = err?.response?.data?.message || 'Thao tác thất bại'
    const details = err?.response?.data?.data
    if (details && typeof details === 'object') {
      const lines = Object.entries(details).map(([k, v]) => `${k}: ${v}`)
      uiStore.notify(`${msg} - ${lines.join(', ')}`, 'error', 5000)
    } else {
      uiStore.notify(msg, 'error', 4000)
    }
  } finally {
    saving.value = false
  }
}

const openDeleteDialog = (u: UserSummary) => {
  if (!isAdmin.value) return
  deleting.value = u
  deleteOpen.value = true
}

const confirmDelete = async () => {
  if (!deleting.value) return
  deletingLoading.value = true
  try {
    await userService.delete(deleting.value.id)
    uiStore.notify('Xóa user thành công', 'success')
    deleteOpen.value = false
    fetchUsers()
  } catch (err: any) {
    uiStore.notify(err?.response?.data?.message || 'Xóa thất bại', 'error', 4000)
  } finally {
    deletingLoading.value = false
  }
}

onMounted(fetchUsers)
</script>
