import React, { useCallback, useEffect, useState } from 'react'
import PropTypes from 'prop-types'
import { Button, ListGroup, ListGroupItem } from 'react-bootstrap';
import ApiHelper from '../helpers/ApiHelper';
import {getUsername} from '../helpers/ApiQueries';
import Invitation from '../helpers/Invitation';

const IncomingInvitationList = props => {
  const [invitations, setInvitations] = useState([]);

  const acceptInvitation = useCallback(async (invitationId) => {
    let apiHelper = new ApiHelper();
    let postUrl = "http://" + props.apiHostname + "/friends/invitation/" + invitationId + "/accept";
    try {
      await apiHelper.callApi(postUrl, "POST", {"token": props.apiToken});
    }  catch (error) {
      console.log(error);
    }
  }, [props.apiHostname, props.apiToken]);

  const declineInvitation = useCallback(async (invitationId) => {
    let apiHelper = new ApiHelper();
    let postUrl = "http://" + props.apiHostname + "/friends/invitation/" + invitationId + "/decline";
    try {
      await apiHelper.callApi(postUrl, "POST", {"token": props.apiToken});
    }  catch (error) {
      console.log(error);
    }
  }, [props.apiHostname, props.apiToken]);

  const loadInvitations = useCallback(async () => {
    // Query the API to get a list of all relationships
    const apiHelper = new ApiHelper();
    let jsonResponse;
    let getUrl = "http://" + props.apiHostname + "/friends/invitation/user/" + props.userId + "/incoming";
    try {
      const result = await apiHelper.callApi(getUrl, "GET", {"token": props.apiToken});
      jsonResponse = await result.json();
    } catch (error) {
      console.log(error);
      return;
    }
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
      <h4>Incoming Invitations</h4>
      <ListGroup>
          {invitations.map(invitation => (
              <ListGroupItem key={invitation.id}>{invitation.username}<br></br>
              <Button variant='success' value={invitation.id} onClick={e => acceptInvitation(e.target.value)}>Accept</Button>
              <Button variant='danger' value={invitation.id} onClick={e => declineInvitation(e.target.value)}>Decline</Button>
              </ListGroupItem>
          ))}
      </ListGroup>
    </div>
  )
}

IncomingInvitationList.propTypes = {
  apiHostname: PropTypes.string.isRequired,
  userId: PropTypes.number.isRequired,
  apiToken: PropTypes.string.isRequired
}

export default IncomingInvitationList