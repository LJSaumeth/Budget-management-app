import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import App from '@/App';

describe('App', () => {
  it('renders navigation links', () => {
    render(<App />);
    const navLinks = screen.getAllByRole('link');
    const linkTexts = navLinks.map((l) => l.textContent);
    expect(linkTexts).toContain('Budgets');
    expect(linkTexts).toContain('History');
    expect(linkTexts).toContain('Exchange');
  });
});
