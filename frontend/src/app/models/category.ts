import {Page} from './page';
import {ProductDto} from './product';

export interface Category {
  id: number;
  name: string;
  description: string | null;
  nameEn: string | null;
  descriptionEn: string | null;
  slug: string;
}

export interface CategoryProducts {
  category: Category;
  productsPage: Page<ProductDto>;
}
