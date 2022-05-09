import React, { useCallback, useEffect, useRef, useState } from "react";
import PropTypes from 'prop-types'
import SimplePeer, { Instance, SignalData } from "simple-peer";
import WebSocketClient from '../helpers/WebSocketClient';
import { Button, Container, Row, Col } from "react-bootstrap";
import SendCallForm from "./SendCallForm";
import { getUserId, getUsername, postCallAccept, postCallDecline, postCallDisconnect, postCallInitiate } from "../helpers/ApiQueries";

const CallPage = props => {
  const videoSelf = useRef(null);
  const videoCaller = useRef(null);
  const [connectionStatus, setConnectionStatus] = useState(null);
  const [callId, setCallId] = useState(null);
  const [callerName, setCallerName] = useState(null);
  const [handshakeInfo, setHandshakeInfo] = useState(null);
  const [simplePeer, setSimplePeer] = useState(null);
  const [wsMessages, setWsMessages] = useState([]);

  const stopVideo = useCallback(() => {
    if (videoSelf.current !== null) {
      let selfStream = videoSelf.current.srcObject;
      if (videoSelf.current.srcObject) {
        selfStream.getTracks().forEach(function(track) {
          track.stop();
        });
        videoSelf.current.srcObject = null;
      }
    }
    if (videoCaller.current !== null) {
      videoCaller.current.srcObject = null;
    }
  }, [videoSelf, videoCaller]);

  const resetPage = useCallback(() => {
    stopVideo();
    setCallId(null);
    setCallerName(null);
    setHandshakeInfo(null);
    setSimplePeer(null);
    setConnectionStatus(null);
  }, [videoSelf, videoCaller]);

  const initiateOrAcceptCall = useCallback(async (initiating, friendName) => {
    let receiverId;
    if (initiating) {
      receiverId = await getUserId(
        friendName,
        props.apiToken,
        props.apiHostname
      );
      if (receiverId === null || receiverId === undefined) {
        // No such user
        return;
      }
    }
    navigator.mediaDevices
      .getUserMedia({video: true, audio: true})
      .then((mediaStream) => {
        const video = videoSelf.current;
        video.srcObject = mediaStream;
        video.play();

        const sp = new SimplePeer({
          trickle: false,
          initiator: initiating,
          stream: mediaStream,
        });

        if (initiating) {
          sp.on("signal", async (data) => {
            const returnedCallId = await postCallInitiate(
              props.userId,
              receiverId,
              props.apiToken,
              props.apiHostname,
              JSON.stringify(data)
            );
            console.log("Returned call id: " + returnedCallId);
            if (returnedCallId === null || returnedCallId === undefined) {
              // Failed to initiate the call - clean up and reset
              resetPage();
              return;
            }
            // Call succeeded - update connection status
            setCallId(returnedCallId);
            setConnectionStatus("offering");
          });
        } else {
          sp.signal(JSON.parse(handshakeInfo));
          sp.on("signal", async (data) => {
            const responseStatus = await postCallAccept(
              callId,
              JSON.stringify(data),
              props.apiToken,
              props.apiHostname
            );
            if (responseStatus !== 200) {
              console.error("Got back non-200 status code from accept post call: " + responseStatus);
              resetPage();
            }
          });
        }
        sp.on("connect", () => setConnectionStatus("connected"));
        sp.on("stream", (stream) => {
          const video = videoCaller.current;
          video.srcObject = stream;
          video.play();
        });
        sp.on("close", () => {
          resetPage();
        });
        setSimplePeer(sp);
      });
  }, [props.userId, props.apiToken, props.apiHostname, callId, handshakeInfo]);

  const declineCall = useCallback(async () => {
    let responseStatus = await postCallDecline(
      callId,
      props.apiToken,
      props.apiHostname
    );
    if (responseStatus !== 200) {
      console.error("Non-200 status code from disconnect request: " + responseStatus);
    }
    resetPage();
  }, [callId, handshakeInfo]);

  const disconnectCall = useCallback(async () => {
    let responseStatus = await postCallDisconnect(
      callId,
      props.apiToken,
      props.apiHostname
    );
    if (responseStatus !== 200) {
      console.error("Non-200 status code from disconnect request: " + responseStatus);
    }
    resetPage();
  }, [props.apiToken, props.apiHostname, callId, simplePeer]);

  const messageHandler = useCallback(async () => {
    if (wsMessages.length > 0) {
      console.log("message handler");
      let envelope = wsMessages[0];
      if (envelope.topic[0] == "calls") {
        let message = JSON.parse(envelope.body);
        switch (message.status) {
          case "offering":
            if (connectionStatus == null) {
              setCallId(message.callId);
              setCallerName(await getUsername(message.userId, props.apiToken, props.apiHostname));
              setHandshakeInfo(message.handshakeInfo);
              setConnectionStatus("receiving");
            }
            break;
          case "accepted":
            simplePeer.signal(JSON.parse(message.handshakeInfo));
            break;
          case "disconnected":
            alert("Call disconnected.");
            resetPage();
            break;
          case "declined":
            alert("Call declined.");
            resetPage();
            break;
          default:
            break;
        }
      }

      // Pop the message from the queue
      setWsMessages(currentMessages => {
        return currentMessages.slice(1);
      });
    }
  }, [connectionStatus, wsMessages, simplePeer, props.apiToken, props.apiHostname])

  useEffect(() => {
    messageHandler();
  }, [messageHandler]);
  
  const messagePusher = useCallback((jsonMessage) => {
    console.log("message pusher");
    setWsMessages(currentMessages => {
      return [...currentMessages, jsonMessage]
    });
  }, []);

  useEffect(() => {
    console.log("useEffect");
    let wsClient = WebSocketClient.getInstance();
    wsClient.waitForSocketConnection(() => {
      wsClient.sendMessage(JSON.stringify({
        "topic": `calls.${props.userId}`,
        "action": "subscribe"
      }));
    });
    wsClient.addMessageHandler(messagePusher);
    return () => wsClient.removeMessageHandler(messagePusher);
  }, [messagePusher]);

  return (
    <Container>
      <Row>
        <Col>
          <video ref={videoSelf} muted="muted" className="video-block" />
        </Col>
        <Col>
          <video ref={videoCaller} className="video-block" />
        </Col>
      </Row>
      <Row>
        <Col>
          {connectionStatus === null && (
            <SendCallForm initiateCall={initiateOrAcceptCall}/>
          )}
          {(connectionStatus === "offering" || connectionStatus === "connected") && (
            <Button onClick={disconnectCall}>Disconnect</Button>
          )}
          {connectionStatus === "receiving" && (
            <div>
              <p>Receiving Call from {callerName}.</p>
              <Button variant="success" onClick={() => initiateOrAcceptCall(false)}>Answer</Button>
              <span>  </span>
              <Button variant="danger" onClick={declineCall}>Decline</Button>
            </div>
          )}
        </Col>
      </Row>
    </Container>
  )
}

CallPage.propTypes = {
  apiHostname: PropTypes.string.isRequired,
  userId: PropTypes.number.isRequired,
  apiToken: PropTypes.string.isRequired
}

export default CallPage