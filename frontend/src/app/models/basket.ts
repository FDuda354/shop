export interface BasketProductRequest {
  productId: number;
  quantity: number;
}

export interface BasketProductItem {
  id: number;
  name: string;
  nameEn: string | null;
  price: number;
  currency: string;
  image: string | null;
  slug: string;
}

export interface BasketItem {
  id: number;
  quantity: number;
  linePrice: number;
  product: BasketProductItem;
}

export interface BasketSummary {
  id: number;
  items: BasketItem[];
  summary: {grossValue: number};
}
