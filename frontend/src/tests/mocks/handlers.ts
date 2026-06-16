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

  http.get('/api/exchange/rates', ({ request }) => {
    const url = new URL(request.url);
    const base = url.searchParams.get('base') ?? 'USD';
    return HttpResponse.json({
      base,
      rates: { EUR: 0.92, GBP: 0.79, JPY: 150.5 },
      fetchedAt: '2026-06-15T12:00:00Z',
    });
  }),

  http.get('/api/exchange/convert', ({ request }) => {
    const url = new URL(request.url);
    const amount = Number(url.searchParams.get('amount'));
    const from = url.searchParams.get('from') ?? 'USD';
    const to = url.searchParams.get('to') ?? 'EUR';
    const rates: Record<string, number> = { EUR: 0.92, GBP: 0.79 };
    const rate = from.toUpperCase() === to.toUpperCase() ? 1 : (rates[to.toUpperCase()] ?? 1);

    return HttpResponse.json({
      from: from.toUpperCase(),
      to: to.toUpperCase(),
      amount,
      rate,
      result: Number((amount * rate).toFixed(2)),
      fetchedAt: '2026-06-15T12:00:00Z',
    });
  }),

  http.get('/api/expenses/history', ({ request }) => {
    const url = new URL(request.url);
    const budgetId = Number(url.searchParams.get('budgetId'));
    const expenses = sampleExpenses.filter((e) => e.budgetId === budgetId);
    return HttpResponse.json({
      content: expenses,
      totalElements: expenses.length,
      totalPages: Math.ceil(expenses.length / 20),
      page: Number(url.searchParams.get('page')) || 0,
      size: Number(url.searchParams.get('size')) || 20,
    });
  }),

  http.get('/api/expenses/summary', () => {
    return HttpResponse.json([
      { categoryId: 1, categoryName: 'Food', totalAmount: 250, expenseCount: 5, percentage: 62.5 },
      { categoryId: 2, categoryName: 'Transport', totalAmount: 100, expenseCount: 2, percentage: 25 },
      { categoryId: 3, categoryName: 'Entertainment', totalAmount: 50, expenseCount: 1, percentage: 12.5 },
    ]);
  }),

  http.get('/api/limits', ({ request }) => {
    const url = new URL(request.url);
    const budgetId = Number(url.searchParams.get('budgetId'));
    return HttpResponse.json([
      { id: 1, budgetId, amount: 1000, period: 'MONTHLY', warningThresholdPercent: 80, createdAt: '2026-06-01T00:00:00Z' },
    ]);
  }),

  http.get('/api/limits/:id/status', () => {
    return HttpResponse.json({
      limitId: 1,
      budgetId: 1,
      limitAmount: 1000,
      spentAmount: 600,
      remainingAmount: 400,
      percentageUsed: 60,
      status: 'OK',
      period: 'MONTHLY',
      periodStart: '2026-06-01',
      periodEnd: '2026-06-30',
    });
  }),

  http.post('/api/limits', async ({ request }) => {
    const body = await request.json() as Record<string, unknown>;
    return HttpResponse.json({
      id: Date.now(),
      budgetId: body.budgetId,
      amount: body.amount,
      period: body.period,
      warningThresholdPercent: body.warningThresholdPercent,
      createdAt: new Date().toISOString(),
    }, { status: 201 });
  }),

  http.put('/api/limits/:id', async ({ params, request }) => {
    const body = await request.json() as Record<string, unknown>;
    return HttpResponse.json({ id: Number(params.id), ...body });
  }),

  http.delete('/api/limits/:id', () => new HttpResponse(null, { status: 204 })),
];
