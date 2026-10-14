import { Product } from '../../core/models';

export interface DraftLine {
  productId: number | null;
  quantity: number;
}

/** Order total in cents-safe arithmetic, ignoring incomplete lines. */
export function draftTotal(lines: DraftLine[], products: ReadonlyMap<number, Product>): number {
  const cents = lines.reduce((sum, line) => {
    const product = line.productId != null ? products.get(line.productId) : undefined;
    if (!product || !(line.quantity > 0)) {
      return sum;
    }
    return sum + Math.round(product.price * 100) * line.quantity;
  }, 0);
  return cents / 100;
}

/** Products whose requested quantity (summed across duplicate lines) exceeds stock. */
export function overStockedProducts(
  lines: DraftLine[],
  products: ReadonlyMap<number, Product>,
): Product[] {
  const requested = new Map<number, number>();
  for (const line of lines) {
    if (line.productId != null && line.quantity > 0) {
      requested.set(line.productId, (requested.get(line.productId) ?? 0) + line.quantity);
    }
  }
  return [...requested.entries()]
    .map(([id, qty]) => ({ product: products.get(id), qty }))
    .filter(({ product, qty }) => product !== undefined && qty > product.stockQuantity)
    .map(({ product }) => product as Product);
}
