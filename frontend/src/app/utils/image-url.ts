import {environment} from '../../environments/environment';

/**
 * Zdjęcia produktów/profili są serwowane przez backend z bazy (bytea) pod
 * /data/productImage/{fileName}. Brak nazwy = brak zdjęcia — szablony
 * pokazują wtedy statyczny placeholder (ikona), nie animowany gif.
 */
export function imageUrl(fileName: string | null | undefined): string | null {
  return fileName ? `${environment.api.baseUrl}/data/productImage/${fileName}` : null;
}
