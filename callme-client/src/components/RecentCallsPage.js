import React, { useCallback, useEffect, useState } from 'react'
import PropTypes from 'prop-types'
import { Container, ListGroup, ListGroupItem, Row } from 'react-bootstrap'
import CallRecord from '../helpers/CallRecord'
import { getCallRecords, getUsername } from '../helpers/ApiQueries'
import incomingCompleted from '../assets/incoming-completed.png';
import incomingDeclined from '../assets/incoming-declined.png';
import outgoingCompleted from '../assets/outgoing-completed.png';
import outgoingDeclined from '../assets/outgoing-declined.png';
import '../App.css';

const imgSrcFromCall = (call) => {
  if (call.type === "incoming") {
    return call.status === "completed" ? incomingCompleted : incomingDeclined;
  } else {
    return call.status === "completed" ? outgoingCompleted : outgoingDeclined;
  }
}

const RecentCallsPage = props => {
  const [calls, setCalls] = useState([]);

  const loadCalls = useCallback(async () => {
    // Query the API to get a list of all relationships
    let jsonResponse = await getCallRecords(props.userId, props.apiToken, props.apiHostname);
    let loadedCalls = jsonResponse.map(element => {
      return CallRecord.fromApiResponse(element, props.userId);
    });
    // Get usernames
    for (let i = 0; i < loadedCalls.length; i++) {
      loadedCalls[i].friendUsername = await getUsername(loadedCalls[i].friendId, props.apiToken, props.apiHostname);
    }
    console.log(loadedCalls);

    // Update state
    setCalls(loadedCalls);
  }, [props.apiHostname, props.userId, props.apiToken])

  useEffect(() => {loadCalls()}, [loadCalls]);

  return (
    <Container>
    <Row xs lg='3' className="justify-content-md-center"><h4>Recent Calls</h4></Row>
    <Row xs lg='3' className="justify-content-md-center">
        <ListGroup>
          {calls.map(call => (
              <ListGroupItem key={call.id}>
                <img className='call-status-indicator' src={imgSrcFromCall(call)}/>
                <span className='call-username'>{call.friendUsername}</span> - {call.timestamp.toLocaleString()}
                </ListGroupItem>
          ))}
        </ListGroup>
    </Row>
    </Container>
  )
}

RecentCallsPage.propTypes = {
  apiHostname: PropTypes.string.isRequired,
  userId: PropTypes.number.isRequired,
  apiToken: PropTypes.string.isRequired
}

export default RecentCallsPage