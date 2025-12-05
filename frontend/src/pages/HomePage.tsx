import { type JSX } from "react";
import { useNavigate } from "react-router-dom";
import { Header } from "../components/layout/Header";
import { EnterButton } from "../components/ui/EnterButton";
import "./HomePage.css";

export const HomePage = (): JSX.Element => {
  const navigate = useNavigate();
  return (
    <div className="home-page">
      <Header />
      <div className="welcome-content">
        <h1 className="welcome-title">Welcome!</h1>
        <div className="buttons-container">
          <EnterButton 
            variant="login" 
            onClick={() => navigate('/login')}
            className="welcome-button"
          />
          <EnterButton 
            variant="register" 
            onClick={() => navigate('/register')}
            className="welcome-button"
          />
        </div>
      </div>
    </div>
  );
};