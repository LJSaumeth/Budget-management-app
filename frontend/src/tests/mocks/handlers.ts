import { http, HttpResponse } from 'msw';

const sampleBudgets = [
  {
    id: 1,
    name: 'Monthly Budget',
    totalAmount: 5000,
    currency: 'USD',
    spentAmount: 1500,
    remainingAmount: 3500,
    createdAt: '2026-06-01T00:00:00Z',
  },
];

const sampleCategories = [
  { id: 1, name: 'Food' },
  { id: 2, name: 'Transport' },
  { id: 3, name: 'Entertainment' },
];

const sampleExpenses = [
  {
    id: 1,
    budgetId: 1,
    categoryId: 1,
    categoryName: 'Food',
    amount: 50,
    description: 'Grocery shopping',
    date: '2026-06-15',
  },
  {
    id: 2,
    budgetId: 1,
    categoryId: 2,
    categoryName: 'Transport',
    amount: 30,
    description: 'Bus pass',
    date: '2026-06-14',
  },
];

export const handlers = [
  http.get('/api/budgets', () => HttpResponse.json(sampleBudgets)),
  http.get('/api/budgets/:id', ({ params }) => {
    const budget = sampleBudgets.find((b) => b.id === Number(params.id));
    if (!budget) return new HttpResponse(null, { status: 404 });
    return HttpResponse.json(budget);
  }),
  http.post('/api/budgets', async ({ request }) => {
    const body = await request.json() as Record<string, unknown>;
    const budget = {
      id: Date.now(),
      name: body.name as string,
      totalAmount: body.totalAmount as number,
      currency: body.currency as string,
      spentAmount: 0,
      remainingAmount: body.totalAmount as number,
      createdAt: new Date().toISOString(),
    };
    return HttpResponse.json(budget, { status: 201 });
  }),
  http.put('/api/budgets/:id', async ({ params, request }) => {
    const body = await request.json() as Record<string, unknown>;
    const budget = sampleBudgets.find((b) => b.id === Number(params.id));
    if (!budget) return new HttpResponse(null, { status: 404 });
    return HttpResponse.json({ ...budget, ...body });
  }),
  http.delete('/api/budgets/:id', () => new HttpResponse(null, { status: 204 })),

  http.get('/api/categories', () => HttpResponse.json(sampleCategories)),
  http.post('/api/categories', async ({ request }) => {
    const body = await request.json() as Record<string, unknown>;
    return HttpResponse.json(
      { id: Date.now(), name: body.name },
      { status: 201 },
    );
  }),

  http.get('/api/expenses', ({ request }) => {
    const url = new URL(request.url);
    const budgetId = url.searchParams.get('budgetId');
    if (!budgetId) return HttpResponse.json([]);
    return HttpResponse.json(
      sampleExpenses.filter((e) => e.budgetId === Number(budgetId)),
    );
  }),
  http.post('/api/expenses', async ({ request }) => {
    const body = await request.json() as Record<string, unknown>;
    const expense = {
      id: Date.now(),
      budgetId: body.budgetId as number,
      categoryId: body.categoryId as number,
      categoryName: sampleCategories.find((c) => c.id === body.categoryId)?.name ?? 'Other',
      amount: body.amount as number,
      description: body.description as string,
      date: body.date as string,
    };
    return HttpResponse.json(expense, { status: 201 });
  }),
  http.put('/api/expenses/:id', async ({ params, request }) => {
    const body = await request.json() as Record<string, unknown>;
    return HttpResponse.json({ id: Number(params.id), ...body });
  }),
  http.delete('/api/expenses/:id', () => new HttpResponse(null, { status: 204 })),
];
