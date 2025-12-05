import { type JSX } from "react";
import "./DashboardButton.css";

interface Props {
  title: string;
  onClick?: () => void;
  disabled?: boolean;
  className?: string;
}

export const DashboardButton = ({ 
  title, 
  onClick, 
  disabled = false,
  className = "" 
}: Props): JSX.Element => {
  return (
    <button 
      className={`dashboard-button ${className}`}
      onClick={onClick}
      disabled={disabled}
    >
      <div className="button-content">
        <span className="button-title">{title}</span>
      </div>
      <div className="button-background"></div>
    </button>
  );
};