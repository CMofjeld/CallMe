import React, { useCallback, useEffect, useState } from 'react'
import PropTypes from 'prop-types'
import { ListGroup, ListGroupItem } from 'react-bootstrap';
import ApiHelper from '../helpers/ApiHelper';
import {getOutgoingInvitations, getUsername} from '../helpers/ApiQueries';
import Invitation from '../helpers/Invitation';

const OutgoingInvitationList = props => {
  const [invitations, setInvitations] = useState([]);

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

  return (
    <div>
      <h4>Outgoing Invitations</h4>
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