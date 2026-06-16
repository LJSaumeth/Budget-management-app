import { NavLink } from 'react-router-dom';

const links = [
  { to: '/', label: 'Budgets' },
  { to: '/history', label: 'History' },
  { to: '/exchange', label: 'Exchange' },
  { to: '/limits', label: 'Limits' },
  { to: '/simulation', label: 'Simulation' },
  { to: '/analysis', label: 'Analysis' },
];

export default function Sidebar() {
  return (
    <nav className="flex md:flex-col gap-2 p-4 bg-paper border-r border-brown/20 min-w-[160px]">
      {links.map((link) => (
        <NavLink
          key={link.to}
          to={link.to}
          className={({ isActive }) =>
            `font-hand text-lg px-3 py-1 rounded-sketch transition-colors ${
              isActive
                ? 'bg-sage text-paper underline decoration-brown decoration-2 underline-offset-4'
                : 'text-brown hover:bg-sage/30'
            }`
          }
        >
          {link.label}
        </NavLink>
      ))}
    </nav>
  );
}
