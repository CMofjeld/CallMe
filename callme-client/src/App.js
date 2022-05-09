import './App.css';
import { Route, Routes } from "react-router-dom";
import { useEffect, useState } from 'react';
import AppNavbar from './components/AppNavbar';
import LoginForm from './components/LoginForm';
import CallPage from './components/CallPage';
import FriendPage from './components/FriendPage';
import WebSocketClient from './helpers/WebSocketClient';
import RecentCallsPage from './components/RecentCallsPage';

function App() {
  const API_HOSTNAME = process.env.REACT_APP_API_HOSTNAME

  const [apiToken, setApiToken] = useState();
  const [userId, setUserId] = useState();

  // Connect to WebSocket only when user ID changes
  useEffect(() => {
    async function connectWS() {
      if (userId) {
        // Connect
        const wsClient = await WebSocketClient.getInstance();
        wsClient.path = `ws://${API_HOSTNAME}/websocket/${userId}`;
        wsClient.connect();
      }
    }
    connectWS();
  }, [userId]);

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
        <Route path='/recent' element={<RecentCallsPage apiHostname={API_HOSTNAME} apiToken={apiToken} userId={userId} />} />
      </Routes>
    </div>
  );
}

export default App;
