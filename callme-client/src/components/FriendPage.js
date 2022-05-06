import React from 'react'
import PropTypes from 'prop-types'
import FriendList from './FriendList'
import { Col, Container, Row } from 'react-bootstrap';
import IncomingInvitationList from './IncomingInvitationList';
import OutgoingInvitationList from './OutgoingInvitationList';
import SendInvitationForm from './SendInvitationForm';

const FriendPage = props => {
  return (
    <div>
      <Container>
        <Row>
          <Col>
            <Row>
              <FriendList apiHostname={props.apiHostname} apiToken={props.apiToken} userId={props.userId} />
            </Row>
          </Col>
          <Col>
            <Row>
              <SendInvitationForm apiHostname={props.apiHostname} apiToken={props.apiToken} userId={props.userId} />
            </Row>
            <Row>
              <Col>
                <IncomingInvitationList apiHostname={props.apiHostname} apiToken={props.apiToken} userId={props.userId} />
              </Col>
              <Col>
                <OutgoingInvitationList apiHostname={props.apiHostname} apiToken={props.apiToken} userId={props.userId} />
              </Col>
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