import {PaymentOption, Shipment} from './order';

export interface AdminProduct {
  id: number;
  name: string;
  description: string;
  fullDescription: string | null;
  nameEn: string | null;
  descriptionEn: string | null;
  fullDescriptionEn: string | null;
  categoryId: number;
  price: number;
  currency: string;
  image: string | null;
  slug: string;
}

export interface AdminProductRequest {
  name: string;
  description: string;
  fullDescription: string | null;
  nameEn: string | null;
  descriptionEn: string | null;
  fullDescriptionEn: string | null;
  categoryId: number | null;
  price: number | null;
  currency: string;
  image: string | null;
  slug: string;
}

export interface AdminCategory {
  id: number;
  name: string;
  description: string | null;
  nameEn: string | null;
  descriptionEn: string | null;
  slug: string;
}

export interface AdminCategoryRequest {
  name: string;
  description: string | null;
  nameEn: string | null;
  descriptionEn: string | null;
  slug: string;
}

export interface AdminOrderRow {
  id: number;
  orderId: number;
  product: AdminProduct | null;
  quantity: number;
  price: number;
  shipment: Shipment | null;
}

export interface AdminOrderLog {
  id: number;
  orderId: number;
  created: string;
  note: string;
}

export interface AdminOrderListRow {
  id: number;
  placeDate: string;
  orderStatus: string;
  grossValue: number;
}

export interface AdminOrder {
  id: number;
  placeDate: string;
  orderStatus: string;
  orderRows: AdminOrderRow[];
  grossValue: number;
  firstName: string;
  lastName: string;
  street: string;
  zipCode: string;
  city: string;
  email: string;
  phone: string;
  payment: PaymentOption | null;
  logs: AdminOrderLog[];
}

export interface AdminInitData {
  orderStatuses: Record<string, string>;
}

export interface AdminOrderStats {
  labels: number[];
  ordersValue: number[];
  ordersCount: number[];
}

export interface UploadResponse {
  fileName: string;
}
