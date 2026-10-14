import { TestBed } from '@angular/core/testing';
import { StatusBadge } from './status-badge';

describe('StatusBadge', () => {
  it('renders a readable label with a status-specific class', async () => {
    const fixture = TestBed.createComponent(StatusBadge);
    fixture.componentRef.setInput('status', 'SHIPPED');
    await fixture.whenStable();

    const badge = (fixture.nativeElement as HTMLElement).querySelector('span');
    expect(badge?.textContent?.trim()).toBe('Shipped');
    expect(badge?.classList).toContain('s-shipped');
  });
});
