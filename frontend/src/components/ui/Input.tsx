import { type JSX } from "react";
import "./Input.css";

interface Props {
  label: string;
  type?: string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  error?: string;
  required?: boolean;
  disabled?: boolean;
  className?: string;
}

export const Input = ({ 
  label, 
  type = "text", 
  value, 
  onChange, 
  placeholder = "", 
  error,
  required = false,
  disabled = false,
  className = ""
}: Props): JSX.Element => {
  return (
    <div className={`standart-textfield ${className}`}>
      <div className="textfield-wrapper">
        <input
          type={type}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          disabled={disabled}
          className="textfield-input"
        />
        <div className="rectangle"></div>
      </div>
      {label && (
        <label className="textfield-label">
          {label} {required && <span className="required-star">*</span>}
        </label>
      )}
      {error && <p className="textfield-error">{error}</p>}
    </div>
  );
};