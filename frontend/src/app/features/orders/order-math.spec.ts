import { Product } from '../../core/models';
import { draftTotal, overStockedProducts } from './order-math';

function product(id: number, price: number, stockQuantity: number): Product {
  return {
    id,
    sku: `SKU-${id}`,
    name: `Product ${id}`,
    description: null,
    price,
    stockQuantity,
    reorderLevel: 0,
    lowStock: false,
    updatedAt: '',
  };
}

const catalogue = new Map([
  [1, product(1, 19.99, 10)],
  [2, product(2, 0.1, 3)],
]);

describe('draftTotal', () => {
  it('sums complete lines without floating point drift', () => {
    const total = draftTotal(
      [
        { productId: 1, quantity: 3 },
        { productId: 2, quantity: 3 },
      ],
      catalogue,
    );

    expect(total).toBe(60.27); // 59.97 + 0.30
  });

  it('ignores lines without a product or with an invalid quantity', () => {
    const total = draftTotal(
      [
        { productId: null, quantity: 4 },
        { productId: 1, quantity: 0 },
        { productId: 99, quantity: 1 },
      ],
      catalogue,
    );

    expect(total).toBe(0);
  });
});

describe('overStockedProducts', () => {
  it('flags products whose combined quantity exceeds stock', () => {
    const flagged = overStockedProducts(
      [
        { productId: 2, quantity: 2 },
        { productId: 2, quantity: 2 },
        { productId: 1, quantity: 10 },
      ],
      catalogue,
    );

    expect(flagged.map((p) => p.id)).toEqual([2]);
  });
});
