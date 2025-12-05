import { type JSX } from "react";
import "./EnterButton.css";

interface Props {
  variant: "login" | "register";
  onClick?: () => void;
  className?: string;
}

export const EnterButton = ({ variant, onClick, className }: Props): JSX.Element => {
  return (
    <button 
      className={`enter-button ${variant} ${className || ""}`}
      onClick={onClick}
    >
      <div className="button-text">
        {variant === "login" ? "Войти" : "Зарегистрироваться"}
      </div>
    </button>
  );
};