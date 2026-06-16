import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BudgetProvider } from '@/context/BudgetContext';
import AppShell from '@/components/layout/AppShell';
import DashboardPage from '@/pages/DashboardPage';
import BudgetDetailPage from '@/pages/BudgetDetailPage';
import HistoryPage from '@/pages/HistoryPage';
import ExchangePage from '@/pages/ExchangePage';
import LimitsPage from '@/pages/LimitsPage';
import SimulationPage from '@/pages/SimulationPage';
import AnalysisPage from '@/pages/AnalysisPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000,
      retry: 1,
    },
  },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BudgetProvider>
        <BrowserRouter>
          <Routes>
            <Route element={<AppShell />}>
              <Route path="/" element={<DashboardPage />} />
              <Route path="/budgets/:id" element={<BudgetDetailPage />} />
              <Route path="/history" element={<HistoryPage />} />
              <Route path="/exchange" element={<ExchangePage />} />
              <Route path="/limits" element={<LimitsPage />} />
              <Route path="/simulation" element={<SimulationPage />} />
              <Route path="/analysis" element={<AnalysisPage />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </BudgetProvider>
    </QueryClientProvider>
  );
}
