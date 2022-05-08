export default class WebSocketClient {
  static instance = null;
  handlers = new Set();
  path;

  static getInstance() {
    if (!WebSocketClient.instance) WebSocketClient.instance = new WebSocketClient();
    return WebSocketClient.instance;
  }

  constructor() {
    this.socketRef = null;
  }

  addMessageHandler = (handler) => {
    this.handlers.add(handler);
  }

  removeMessageHandler = (handler) => {
    this.handlers.delete(handler);
  }

  connect = () => {
    this.socketRef = new WebSocket(this.path);
    this.socketRef.onopen = () => {
      console.log('WebSocket open');
    };

    this.socketRef.onmessage = e => {
      this.handleMessage(e.data);
    };

    this.socketRef.onerror = e => {
      console.log(e.message);
    };

    this.socketRef.onclose = () => {
      console.log("WebSocket closed let's reopen");
      this.connect();
    };
  }

  handleMessage = (message) => {
    // Convert to JSON and extract subtopic for ease of use
    let jsonMessage = JSON.parse(message);
    console.log(jsonMessage);
    jsonMessage.topic = jsonMessage.topic.split(".");
    console.log(jsonMessage);
    this.handlers.forEach((handler) => handler(jsonMessage));
  }

  sendMessage = (message) => {
    if (this.socketRef !== null && this.socketRef.readyState === 1) {
      this.socketRef.send(message);
    } else {
      console.error("Can't send message - websocket not connected.");
    }
  }

  waitForSocketConnection = (callback) => {
    const socket = this.socketRef;
    const recursion = this.waitForSocketConnection;
    setTimeout(
      () => {
        if (socket && socket.readyState === 1) {
          console.log("Connection is made")
          if (callback != null) {
            callback();
          }
          return;
        } else {
          console.log("wait for connection...")
          recursion(callback);
        }
      },
    1);
  }
}