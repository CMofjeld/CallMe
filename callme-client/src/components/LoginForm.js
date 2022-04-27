import React, {useState} from 'react';
import PropTypes from 'prop-types';
import { Form, Button, Container, Row, Col } from 'react-bootstrap';
import ApiHelper from '../helpers/ApiHelper';

const LoginForm = props => {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [buttonPressed, setButtonPressed] = useState("login");

    const loginUser = async () => {
        var body = JSON.stringify({
            'username': username,
            'password': password,
        });
        let loginUrl = 'http://' + props.apiHostname + '/user/login';
        const apiHelper = new ApiHelper();
        try {
            const result = await apiHelper.callApi(loginUrl, 'POST', {body: body});
            if (result.status === 401 || result.status === 404) {
                alert("Invalid username or password.");
                return;
            }
            const resp_json = await result.json();
            props.setApiToken(resp_json.accessToken);
            props.setUserId(resp_json.user.id);
        } catch (e) {
            console.log(e);
        }
    };

    const registerUser = async () => {
        var body = JSON.stringify({
            'username': username,
            'password': password,
        });
        let registerURL = 'http://' + props.apiHostname + '/user/register';
        const apiHelper = new ApiHelper();
        try {
            const result = await apiHelper.callApi(registerURL, 'POST', {body: body});
            if (result.status === 409) {
                alert("Username already taken!");
                return;
            } else if (result.status !== 201) {
                alert("Something went wrong!");
            } else {
                alert("Successfully registered!");
            }
        } catch (e) {
            console.log(e);
        }
    }

    const handleSubmit = async e => {
        e.preventDefault()
        if (buttonPressed === "login") {
            await loginUser();
        } else if (buttonPressed === "register") {
            await registerUser();
        }
        console.log(buttonPressed);
    };

    return (
        <Container>
            <Row className="justify-content-center">
                <Col xs lg='3'>
                    <Form onSubmit={handleSubmit}>
                        <Form.Group className="mb-3" controlId="formBasicUsername">
                            <Form.Label>Username</Form.Label>
                            <Form.Control
                                autoFocus
                                type="text"
                                placeholder="Enter username"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                            />
                        </Form.Group>

                        <Form.Group className="mb-3" controlId="formBasicPassword">
                            <Form.Label>Password</Form.Label>
                            <Form.Control
                                type="password"
                                placeholder="Password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                            />
                        </Form.Group>
                        <Button variant="primary" type="submit" onClick={() => (setButtonPressed("register"))}>
                            Register
                        </Button>
                        <span> </span>
                        <Button variant="primary" type="submit" onClick={() => (setButtonPressed("login"))}>
                            Log in
                        </Button>
                    </Form>
                </Col>
            </Row>
        </Container>
    );
};

LoginForm.propTypes = {
    apiHostname: PropTypes.string.isRequired,
    setApiToken: PropTypes.func.isRequired,
    setUserId: PropTypes.func.isRequired
};

export default LoginForm;