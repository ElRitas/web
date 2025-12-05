import React from 'react';
import { useLocation } from 'react-router-dom';

export const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const location = useLocation();

  if (location.pathname === '/') {
    return <>{children}</>;
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <nav className="bg-[#0e2441] shadow-lg">
        <div className="container px-4 max-w-7xl">
          <div className="flex justify-between items-center py-4">
          </div>
        </div>
      </nav>

      <main>{children}</main>
    </div>
  );
};