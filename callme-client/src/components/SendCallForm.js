import React, {useCallback, useState} from 'react';
import PropTypes from 'prop-types';
import { Form, Button, Container, Row, Col } from 'react-bootstrap';

const SendCallForm = props => {
  const [friendname, setFriendname] = useState("");

  const handleSubmit = useCallback(e => {
    e.preventDefault();
    props.initiateCall(true, friendname);
  }, [props.initiateCall, friendname]);

  return (
    <Container>
      <Row className="justify-content-center">
        <Col xs lg='3'>
          <Form onSubmit={handleSubmit}>
            <Form.Group className="mb-3">
              <Form.Control
                autoFocus
                type="text"
                placeholder="Friend's username"
                value={friendname}
                onChange={(e) => setFriendname(e.target.value)}
              />
            </Form.Group>
            <Button variant="primary" type="submit">
              Call a friend
            </Button>
          </Form>
        </Col>
      </Row>
    </Container>
  )
}

SendCallForm.propTypes = {
    initiateCall: PropTypes.func.isRequired,
}

export default SendCallForm;