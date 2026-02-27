export const vnDayLabelFromIso = (isoDay: number) => {
  if (isoDay === 7) return 'Chủ nhật'
  return `Thứ ${isoDay + 1}`
}

export const vnDayShortFromIso = (isoDay: number) => {
  if (isoDay === 7) return 'CN'
  return `T${isoDay + 1}`
}

export const formatTimeSlotsVn = (
  slots?: Array<{ dayOfWeek: number; startTime: string; endTime: string }> | null
) => {
  if (!slots?.length) return '-'
  return slots.map((s) => `${vnDayShortFromIso(s.dayOfWeek)} ${s.startTime.slice(0, 5)}-${s.endTime.slice(0, 5)}`).join(', ')
}

