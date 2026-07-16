export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  numberOfElements: number;
  number: number;
  size: number;
}

export interface PageRequest {
  page: number;
  size: number;
}

export function deepEqual(a: any, b: any): boolean {
  return JSON.stringify(a) === JSON.stringify(b);
}

export function emptyPage<T>(size: number): Page<T> {
  return {content: [], totalElements: 0, totalPages: 0, numberOfElements: 0, number: 0, size};
}
