import React, { useCallback, useEffect, useState } from 'react'
import PropTypes from 'prop-types'
import FriendList from './FriendList'
import ApiHelper from '../helpers/ApiHelper';
import Relationship from '../helpers/Relationship';
import { getStatus, getUsername } from '../helpers/ApiQueries';
import { Col, Container, Row } from 'react-bootstrap';
import InvitationList from './InvitationList';

const FriendPage = props => {
  const [friends, setFriends] = useState({});
  const [outgoing, setOutgoing] = useState({});
  const [incoming, setIncoming] = useState({});

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

    // Divide relationships into friends, outgoing invitations, and incoming invitations
    let friendsCopy = {...friends};
    let incomingCopy = {...incoming};
    let outgoingCopy = {...outgoing};
    for (let response of jsonResponse) {
      // Parse the JSON to a relationship object
      let relationship = Relationship.fromApiResponse(response, props.userId);
      // Get their username
      relationship.friendUsername = await getUsername(relationship.friendId, props.apiToken, props.apiHostname);
      // Get their status
      relationship.status = await getStatus(relationship.friendId, props.apiToken, props.apiHostname);
      // Add to the appropriate collection
      switch (relationship.type) {
        case "friend":
          friendsCopy[relationship.relationshipId] = relationship;
          break;
        case "incoming":
          incomingCopy[relationship.relationshipId] = relationship;
          break;
        case "outgoing":
          outgoingCopy[relationship.relationshipId] = relationship;
          break;
        default:
          console.error("Invalid relationship type " + relationship.type);
          break;
      }
    }

    // Update state
    setFriends(friendsCopy);
    setIncoming(incomingCopy);
    setOutgoing(outgoingCopy);
  }, [props.apiHostname, props.userId, props.apiToken])

  useEffect(() => {loadFriends()}, [loadFriends]);
  
  return (
    <div>
      <Container>
        <Row>
          <Col>
            <FriendList friends={friends}/>
          </Col>
          <Col>
            <Row>
              <InvitationList invitations={incoming} listTitle="Incoming Invitations" />
            </Row>
            <Row>
              <InvitationList invitations={outgoing} listTitle="Outgoing Invitations" />
            </Row>
          </Col>
        </Row>
      </Container>
    </div>
  )
}

FriendPage.propTypes = {
  apiHostname: PropTypes.string.isRequired,
  userId: PropTypes.number.isRequired,
  apiToken: PropTypes.string.isRequired
}

export default FriendPage