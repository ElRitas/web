import { type JSX } from "react";
import "./AddGuestButton.css";

interface Props {
  loading: boolean;
  onClick?: () => void;
  className?: string;
}

export const AddGuestButton = ({ loading, onClick, className }: Props): JSX.Element => {
  return (
    <button 
      className={`add-guest-button ${className || ""}`}
      onClick={onClick}
      disabled={loading}
    >
      <div className="button-text">
        {loading ? "Добавление..." : "Добавить гостя"}
      </div>
    </button>
  );
};