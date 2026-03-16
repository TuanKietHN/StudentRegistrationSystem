import { onBeforeUnmount } from 'vue'

export function useDebounceFn<TArgs extends any[]>(
  fn: (...args: TArgs) => void | Promise<void>,
  wait = 300
) {
  let timeout: number | null = null

  const cancel = () => {
    if (timeout) {
      window.clearTimeout(timeout)
      timeout = null
    }
  }

  const debounced = (...args: TArgs) => {
    cancel()
    timeout = window.setTimeout(() => {
      fn(...args)
    }, wait)
  }

  onBeforeUnmount(() => {
    cancel()
  })

  return { debounced, cancel }
}
