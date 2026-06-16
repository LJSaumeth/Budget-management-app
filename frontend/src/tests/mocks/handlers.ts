import { http, HttpResponse } from 'msw';

export const handlers = [
  http.get('/api/budgets', () => {
    return HttpResponse.json([]);
  }),
  http.get('/api/categories', () => {
    return HttpResponse.json([]);
  }),
];
