import logo from './logo.svg';
import './App.css';
import { Route, Routes } from "react-router-dom";
import { useState } from 'react';
import AppNavbar from './components/AppNavbar';
import LoginForm from './components/LoginForm';

function App() {
  const API_HOSTNAME = process.env.REACT_APP_API_HOSTNAME

  const [apiToken, setApiToken] = useState();
  const [userId, setUserId] = useState();

  if (!apiToken || !userId) {
    return (
      <div className="App">
        <LoginForm apiHostname={API_HOSTNAME} setApiToken={setApiToken} setUserId={setUserId} />
      </div>
    )
  }
  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <p>
          Edit <code>src/App.js</code> and save to reload.
        </p>
        <a
          className="App-link"
          href="https://reactjs.org"
          target="_blank"
          rel="noopener noreferrer"
        >
          Learn React
        </a>
      </header>
    </div>
  );
}

export default App;
