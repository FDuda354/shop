/**
 * Formatuje datę w LOKALNEJ strefie jako ISO local date-time (bez sufiksu Z),
 * np. 2026-07-16T00:00:00 — dokładnie to, co backend parsuje jako LocalDate.
 * Date.toISOString() konwertuje na UTC i dla stref na wschód od UTC cofa
 * lokalną północ na poprzedni dzień.
 */
export function toLocalDateTime(date: Date): string {
  const pad = (value: number) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
    + `T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}
