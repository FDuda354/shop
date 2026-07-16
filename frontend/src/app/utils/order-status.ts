/** Cykl życia zamówienia — kolejność opcji w selectach statusu. */
export const ORDER_STATUS_LIFECYCLE = [
  'NEW',
  'PAID',
  'PROCESSING',
  'WAITING_FOR_DELIVERY',
  'COMPLETED',
  'CANCELLED',
  'REFUND',
];

/**
 * Backend zwraca statusy jako mapę (HashMap — kolejność przypadkowa);
 * porządkujemy je po cyklu życia zamówienia, nieznane trafiają na koniec.
 * Etykiety tłumaczy wołający (klucz i18n: orderStatus.<NAZWA>).
 */
export function statusesInLifecycleOrder(statuses: Record<string, string>): string[] {
  return Object.keys(statuses)
    .sort((a, b) => {
      const ia = ORDER_STATUS_LIFECYCLE.indexOf(a);
      const ib = ORDER_STATUS_LIFECYCLE.indexOf(b);
      return (ia === -1 ? ORDER_STATUS_LIFECYCLE.length : ia) - (ib === -1 ? ORDER_STATUS_LIFECYCLE.length : ib);
    });
}

export function orderStatusSeverity(status: string): 'success' | 'info' | 'warn' | 'danger' | 'secondary' {
  switch (status) {
    case 'PAID':
    case 'COMPLETED':
      return 'success';
    case 'PROCESSING':
    case 'WAITING_FOR_DELIVERY':
      return 'warn';
    case 'CANCELLED':
      return 'danger';
    case 'REFUND':
      return 'secondary';
    default:
      return 'info';
  }
}
