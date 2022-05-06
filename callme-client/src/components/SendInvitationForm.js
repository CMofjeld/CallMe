import React, { useCallback, useState } from 'react'
import PropTypes from 'prop-types'
import { Button, Container, Form } from 'react-bootstrap'
import ApiHelper from '../helpers/ApiHelper';

const SendInvitationForm = props => {
  const [username, setUsername] = useState("");

  const getUserIdFromUsername = useCallback(async (username) => {
    let apiHelper = new ApiHelper();
    let getUrl = "http://" + props.apiHostname + "/user/by/username/" + username;
    let userId = null;
    try {
      const response = await apiHelper.callApi(getUrl, "GET", {"token": props.apiToken});
      if (response.status === 404) {
        alert("Couldn't find a user with that username.");
      } else if (response.status === 200) {
        let userJson = await response.json();
        userId = userJson.id;
      } else {
        console.error(response.status);
        alert("Something went wrong.");
      }
    } catch (error) {
      console.error(error);
    }
    return userId;
  }, [props.apiHostname, props.apiToken, props.userId]);

  const handleSubmit = useCallback(async e => {
    e.preventDefault();
    // Get the user's ID
    let inviteeId = await getUserIdFromUsername(username);
    console.log(inviteeId);
    if (inviteeId !== null) {
      if (inviteeId === props.userId) {
        alert("Can't invite yourself.");
        return;
      }
      let apiHelper = new ApiHelper();
      let postUrl = "http://" + props.apiHostname + "/friends/invitation";
      let body = JSON.stringify({
        "inviter": props.userId,
        "invitee": inviteeId
      });
      const response = await apiHelper.callApi(postUrl, 'POST', {body: body, token: props.apiToken});
      if (response.status === 201) {
        alert("Invitation sent!");
      } else if (response.status === 409) {
        alert("Already in a relationship with that user.");
      } else if (response.status === 400) {
        alert("Already in a relationship with that user.");
      } else {
        console.error(response.status);
        alert("Something went wrong.");
      }
    }
  }, [props.apiHostname, props.apiToken, props.userId, username]);

  return (
    <Container>
      <Form onSubmit={handleSubmit}>
        <Form.Group className="mb-3" controlId="formBasicUsername">
          <Form.Label>Send Friend Invitation</Form.Label>
          <Form.Control
            autoFocus
            type="text"
            placeholder="Enter username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
        </Form.Group>
          <Button variant="primary" type="submit">
            Send
          </Button>
      </Form>
    </Container>
  )
}

SendInvitationForm.propTypes = {
  apiHostname: PropTypes.string.isRequired,
  userId: PropTypes.number.isRequired,
  apiToken: PropTypes.string.isRequired
}

export default SendInvitationForm