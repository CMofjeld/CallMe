import React, { useCallback, useEffect, useState } from 'react'
import PropTypes from 'prop-types'
import { ListGroup, ListGroupItem } from 'react-bootstrap';
import {getOutgoingInvitations, getUsername} from '../helpers/ApiQueries';
import Invitation from '../helpers/Invitation';
import WebSocketClient from '../helpers/WebSocketClient';

const OutgoingInvitationList = props => {
  const [invitations, setInvitations] = useState([]);
  const [wsMessages, setWsMessages] = useState([]);

  const loadInvitations = useCallback(async () => {
    // Query the API to get a list of all relationships
    let jsonResponse = await getOutgoingInvitations(props.userId, props.apiToken, props.apiHostname);
    let loadedInvitations = [];
    for (let response of jsonResponse) {
      // Parse the JSON to a relationship object
      let invitation = Invitation.fromApiResponse(response, props.userId);
      // Get their username
      invitation.username = await getUsername(invitation.userId, props.apiToken, props.apiHostname);
      // Add to the appropriate collection
      loadedInvitations.push(invitation);
    }
    loadedInvitations.sort((a,b) => (a.username > b.username) ? 1 : -1);

    // Update state
    setInvitations(loadedInvitations);
  }, [props.apiHostname, props.userId, props.apiToken])

  useEffect(() => {loadInvitations()}, [loadInvitations]);

  const messageHandler = useCallback(async () => {
    if (wsMessages.length > 0) {
      console.log("message handler");
      let envelope = wsMessages[0];
      if (envelope.topic[0] == "invitations") {
        // Parse the JSON to a relationship object
        let message = JSON.parse(envelope.body);
        if (parseInt(message.inviter) === props.userId) {
          if (message.status === "pending") {
            // New incoming invitation - add it to the list
            let invitation = Invitation.fromApiResponse(message, props.userId);
            // Get their username
            invitation.username = await getUsername(invitation.userId, props.apiToken, props.apiHostname);
            setInvitations(currentInvitations => [...currentInvitations, invitation]);
          } else {
            // Invitations is no longer pending - remove it from the list
            setInvitations(currentInvitations => currentInvitations.filter(inv => inv.id !== message.id));
          }
        }
      }

      // Pop the message from the queue
      setWsMessages(currentMessages => {
        return currentMessages.slice(1);
      });
    }
  }, [wsMessages, props.apiToken, props.apiHostname, props.userId]);

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
        "topic": `invitations.${props.userId}`,
        "action": "subscribe"
      }));
    });
    wsClient.addMessageHandler(messagePusher);
    return () => wsClient.removeMessageHandler(messagePusher);
  }, [messagePusher]);

  return (
    <div>
      <h4>Outbound Invitations</h4>
      <ListGroup>
          {invitations.map(invitation => (
              <ListGroupItem key={invitation.id}>{invitation.username}</ListGroupItem>
          ))}
      </ListGroup>
    </div>
  )
}

OutgoingInvitationList.propTypes = {
  apiHostname: PropTypes.string.isRequired,
  userId: PropTypes.number.isRequired,
  apiToken: PropTypes.string.isRequired
}

export default OutgoingInvitationList