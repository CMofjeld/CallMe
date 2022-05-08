import React, { useCallback, useEffect, useState } from 'react'
import PropTypes from 'prop-types'
import { ListGroup, ListGroupItem } from 'react-bootstrap';
import ApiHelper from '../helpers/ApiHelper';
import Friendship from '../helpers/Friendship';
import { getStatus, getUsername } from '../helpers/ApiQueries';
import WebSocketClient from '../helpers/WebSocketClient';

const FriendList = props => {
  const [friends, setFriends] = useState([]);
  const [wsMessages, setWsMessages] = useState([]);

  const loadFriends = useCallback(async () => {
    // Query the API to get a list of all relationships
    const apiHelper = new ApiHelper();
    let jsonResponse;
    let getUrl = "http://" + props.apiHostname + "/friends/user/" + props.userId;
    try {
      const result = await apiHelper.callApi(getUrl, "GET", {"token": props.apiToken});
      jsonResponse = await result.json();
    } catch (error) {
      console.log(error);
      return;
    }
    let loadedFriends = [];
    for (let response of jsonResponse) {
      // Parse the JSON to a relationship object
      let friendship = Friendship.fromApiResponse(response);
      // Get their username
      friendship.friendUsername = await getUsername(friendship.friendId, props.apiToken, props.apiHostname);
      // Get their status
      friendship.status = await getStatus(friendship.friendId, props.apiToken, props.apiHostname);
      // Add to the appropriate collection
      loadedFriends.push(friendship);
      // Subscribe to their status updates
      subscribeToStatus(friendship.friendId);
    }
    loadedFriends.sort((a,b) => (a.friendUsername > b.friendUsername) ? 1 : -1);

    // Update state
    setFriends(loadedFriends);
  }, [props.apiHostname, props.userId, props.apiToken])

  useEffect(() => {loadFriends()}, [loadFriends]);

  const subscribeToStatus = (friendId) => {
    WebSocketClient.getInstance().sendMessage(JSON.stringify({
      "topic": `status.${friendId}`,
      "action": "subscribe"
    }));
  };

  const messageHandler = useCallback(async () => {
    if (wsMessages.length > 0) {
      console.log("message handler");
      let envelope = wsMessages[0];
      if (envelope.topic[0] == "friends") {
        // Parse the JSON to a relationship object
        let friendship = Friendship.fromApiResponse(JSON.parse(envelope.body));
        // Get their username
        friendship.friendUsername = await getUsername(friendship.friendId, props.apiToken, props.apiHostname);
        // Get their status
        friendship.status = await getStatus(friendship.friendId, props.apiToken, props.apiHostname);
        // Add to the list of friends
        setFriends(currentFriends => [...currentFriends, friendship]);
        // Subscribe to their status updates
        subscribeToStatus(friendship.friendId);
      }
      else if (envelope.topic[0] == "status") {
        // Parse the message to get the friend's ID and new status
        let message = JSON.parse(envelope.body);
        let friendId = message.id;
        let status = message.status;
        // Update the friend's entry in the list
        let friendsCopy = [...friends]
        for (let i = 0; i < friendsCopy.length; i++) {
          if (friendsCopy[i].friendId === friendId) {
            friendsCopy[i].status = status;
            break;
          }
        }
        setFriends(friendsCopy);
      }

      // Pop the message from the queue
      setWsMessages(currentMessages => {
        return currentMessages.slice(1);
      });
    }
  }, [friends, wsMessages, props.apiToken, props.apiHostname]);

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
        "topic": `friends.${props.userId}`,
        "action": "subscribe"
      }));
    });
    wsClient.addMessageHandler(messagePusher);
    return () => wsClient.removeMessageHandler(messagePusher);
  }, [messagePusher]);

  return (
    <div>
      <h4>Friends</h4>
      <ListGroup>
          {friends.map(friend => (
              <ListGroupItem key={friend.id}>{friend.friendUsername} - {friend.status}</ListGroupItem>
          ))}
      </ListGroup>
    </div>
  )
}

FriendList.propTypes = {
  apiHostname: PropTypes.string.isRequired,
  userId: PropTypes.number.isRequired,
  apiToken: PropTypes.string.isRequired
}

export default FriendList