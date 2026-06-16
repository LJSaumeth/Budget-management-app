import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import NotebookButton from '@/components/ui/NotebookButton';

describe('NotebookButton', () => {
  it('renders children', () => {
    render(<NotebookButton>Click me</NotebookButton>);
    expect(screen.getByText('Click me')).toBeInTheDocument();
  });

  it('calls onClick when clicked', () => {
    const handleClick = vi.fn();
    render(<NotebookButton onClick={handleClick}>Click</NotebookButton>);
    fireEvent.click(screen.getByText('Click'));
    expect(handleClick).toHaveBeenCalledOnce();
  });

  it('shows loading state', () => {
    render(<NotebookButton loading>Save</NotebookButton>);
    expect(screen.getByText('...')).toBeInTheDocument();
    expect(screen.queryByText('Save')).not.toBeInTheDocument();
  });

  it('is disabled when loading', () => {
    render(<NotebookButton loading>Save</NotebookButton>);
    expect(screen.getByText('...')).toBeDisabled();
  });
});
