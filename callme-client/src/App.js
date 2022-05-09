import './App.css';
import { Route, Routes } from "react-router-dom";
import { useCallback, useEffect, useState } from 'react';
import AppNavbar from './components/AppNavbar';
import LoginForm from './components/LoginForm';
import CallPage from './components/CallPage';
import FriendPage from './components/FriendPage';
import WebSocketClient from './helpers/WebSocketClient';
import RecentCallsPage from './components/RecentCallsPage';
import { postLogout } from './helpers/ApiQueries';

function App() {
  const API_HOSTNAME = process.env.REACT_APP_API_HOSTNAME

  const [apiToken, setApiToken] = useState();
  const [userId, setUserId] = useState();

  const logout = useCallback(async () => {
    const responseStatus = await postLogout(userId, apiToken, API_HOSTNAME);
    if (responseStatus !== 200) {
      console.error("Non-200 response status from logout request");
    }
    WebSocketClient.getInstance().shutdown();
    setUserId(null);
    setApiToken(null);
  }, [API_HOSTNAME, apiToken, userId]);

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
      <AppNavbar logout={logout} />
      <Routes>
        <Route path='/' element={<CallPage apiHostname={API_HOSTNAME} apiToken={apiToken} userId={userId} />} />
        <Route path='/friends' element={<FriendPage apiHostname={API_HOSTNAME} apiToken={apiToken} userId={userId} />} />
        <Route path='/recent' element={<RecentCallsPage apiHostname={API_HOSTNAME} apiToken={apiToken} userId={userId} />} />
      </Routes>
    </div>
  );
}

export default App;
