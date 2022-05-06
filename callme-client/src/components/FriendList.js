import React, { useCallback, useEffect, useState } from 'react'
import PropTypes from 'prop-types'
import { ListGroup, ListGroupItem } from 'react-bootstrap';
import ApiHelper from '../helpers/ApiHelper';
import Friendship from '../helpers/Friendship';
import { getStatus, getUsername } from '../helpers/ApiQueries';

const FriendList = props => {
  const [friends, setFriends] = useState([]);

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
    }
    loadedFriends.sort((a,b) => (a.friendUsername > b.friendUsername) ? 1 : -1);

    // Update state
    setFriends(loadedFriends);
  }, [props.apiHostname, props.userId, props.apiToken])

  useEffect(() => {loadFriends()}, [loadFriends]);

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