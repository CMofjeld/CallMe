import './App.css';
import { Route, Routes } from "react-router-dom";
import { useState } from 'react';
import AppNavbar from './components/AppNavbar';
import LoginForm from './components/LoginForm';
import CallPage from './components/CallPage';
import FriendPage from './components/FriendPage';

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
      <AppNavbar />
      <Routes>
        <Route path='/' element={<CallPage apiHostname={API_HOSTNAME} apiToken={apiToken} userId={userId} />} />
        <Route path='/friends' element={<FriendPage apiHostname={API_HOSTNAME} apiToken={apiToken} userId={userId} />} />
      </Routes>
    </div>
  );
}

export default App;
