import React, { useCallback, useEffect, useState } from 'react'
import PropTypes from 'prop-types'
import { ListGroup, ListGroupItem } from 'react-bootstrap';

const FriendList = props => {
  const [friends, setFriends] = useState([]);

  const parseFriendsDictToList = useCallback(() => {
    let list = [];
    for (let key in props.friends) {
      list.push(props.friends[key]);
    }
    list.sort((a,b) => (a.friendUsername > b.friendUsername) ? 1 : -1);
    setFriends(list);
  }, [props.friends]);

  useEffect(() => {parseFriendsDictToList()}, [parseFriendsDictToList]);

  return (
    <div>
      <h4>Friends</h4>
      <ListGroup>
          {friends.map(friend => (
              <ListGroupItem key={friend.relationshipId}>{friend.friendUsername} - {friend.status}</ListGroupItem>
          ))}
      </ListGroup>
    </div>
  )
}

FriendList.propTypes = {
  friends: PropTypes.object.isRequired,
}

export default FriendList