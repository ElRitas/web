import { type JSX } from "react";
import "./Header.css";

interface Props {
  className?: string;
}

export const Header = ({ className }: Props): JSX.Element => {
  return (
    <div className={`header ${className || ""}`}>
      <div className="header-text">Parking App</div>
    </div>
  );
};