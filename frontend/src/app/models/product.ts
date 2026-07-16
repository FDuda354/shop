export interface ProductDto {
  id: number;
  name: string;
  description: string;
  nameEn: string | null;
  descriptionEn: string | null;
  price: number;
  currency: string;
  image: string | null;
  slug: string;
}

export interface Review {
  id: number;
  productId: number;
  userId: number | null;
  authorName: string;
  content: string;
}

export interface Product {
  id: number;
  name: string;
  categoryId: number;
  description: string;
  fullDescription: string | null;
  nameEn: string | null;
  descriptionEn: string | null;
  fullDescriptionEn: string | null;
  price: number;
  currency: string;
  image: string | null;
  slug: string;
  reviews: Review[];
}

export interface ReviewRequest {
  authorName: string;
  content: string;
  productId: number;
}
