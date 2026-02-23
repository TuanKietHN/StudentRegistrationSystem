import { defineStore } from 'pinia'

type SnackbarColor = 'success' | 'error' | 'info' | 'warning'

interface SnackbarState {
  open: boolean
  text: string
  color: SnackbarColor
  timeout: number
}

export const useUiStore = defineStore('ui', {
  state: (): { snackbar: SnackbarState } => ({
    snackbar: {
      open: false,
      text: '',
      color: 'info',
      timeout: 2500
    }
  }),
  actions: {
    notify(text: string, color: SnackbarColor = 'info', timeout = 2500) {
      this.snackbar.open = false
      this.snackbar.text = text
      this.snackbar.color = color
      this.snackbar.timeout = timeout
      this.snackbar.open = true
    },
    closeSnackbar() {
      this.snackbar.open = false
    }
  }
})

