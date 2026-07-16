export interface Shipment {
  id: number;
  name: string;
  price: number;
  type: 'COURIER' | 'PICKUP';
  defaultShipment: boolean;
}

export interface PaymentOption {
  id: number;
  name: string;
  type: string;
  defaultPayment: boolean;
  note: string | null;
}

export interface InitOrder {
  shipments: Shipment[];
  payments: PaymentOption[];
}

export interface OrderRequest {
  firstName: string;
  lastName: string;
  street: string;
  zipCode: string;
  city: string;
  email: string;
  phone: string;
  basketId: number;
  shipmentId: number;
  paymentId: number;
}

export interface OrderSummary {
  id: number;
  placeDate: string;
  status: string;
  grossValue: number;
  payment: PaymentOption;
}

export interface OrderForUser {
  id: number;
  placeDate: string;
  status: string;
  grossValue: number;
  paymentName: string;
}
