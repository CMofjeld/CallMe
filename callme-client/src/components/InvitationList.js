import React, { useCallback, useEffect, useState } from 'react'
import PropTypes from 'prop-types'
import { ListGroup, ListGroupItem } from 'react-bootstrap';

const InvitationList = props => {
    const [invitations, setInvitations] = useState([]);
  
    const parseInvitationsDictToList = useCallback(() => {
      let list = [];
      for (let key in props.invitations) {
        list.push(props.invitations[key]);
      }
      list.sort((a,b) => (a.friendUsername > b.friendUsername) ? 1 : -1);
      setInvitations(list);
    }, [props.invitations]);
  
    useEffect(() => {parseInvitationsDictToList()}, [parseInvitationsDictToList]);

  return (
    <div>
      <h4>{props.listTitle}</h4>
      <ListGroup>
          {invitations.map(invitation => (
              <ListGroupItem key={invitation.relationshipId}>{invitation.friendUsername} - {invitation.relationshipStatus}</ListGroupItem>
          ))}
      </ListGroup>
    </div>
  )
}

InvitationList.propTypes = {
    invitations: PropTypes.object.isRequired,
    listTitle: PropTypes.string.isRequired,
}

export default InvitationList