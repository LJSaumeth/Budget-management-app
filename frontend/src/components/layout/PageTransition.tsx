import { useLocation } from 'react-router-dom';
import { useEffect, useRef, useState } from 'react';

export default function PageTransition({ children }: { children: React.ReactNode }) {
  const location = useLocation();
  const [displayChildren, setDisplayChildren] = useState(children);
  const [transitioning, setTransitioning] = useState(false);
  const depth = useRef(0);

  useEffect(() => {
    depth.current += 1;
    setTransitioning(true);
    const timer = setTimeout(() => {
      setDisplayChildren(children);
      setTransitioning(false);
    }, 150);
    return () => clearTimeout(timer);
  }, [location.pathname]);

  return (
    <div
      style={{
        perspective: '1000px',
      }}
    >
      <div
        className="transition-transform duration-300 ease-in-out"
        style={{
          transform: transitioning
            ? 'rotateY(8deg)'
            : 'rotateY(0deg)',
          opacity: transitioning ? 0.5 : 1,
          transformOrigin: 'left center',
        }}
      >
        {displayChildren}
      </div>
    </div>
  );
}
