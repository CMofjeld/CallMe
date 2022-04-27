import React from 'react'
import { Navbar, Nav, Container } from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';

const AppNavbar = () => {
  return (
    <Navbar bg='light' expand='lg' className='justify-content-center'>
        <Container>
            <LinkContainer to="/">
                <Navbar.Brand>CallMe</Navbar.Brand>
            </LinkContainer>
            <Navbar.Toggle aria-controls="basic-navbar-nav" />
            <Navbar.Collapse id="basic-navbar-nav">
                <Nav className='justify-content-center'>
                    <LinkContainer to={'/'}>
                        <Nav.Link>Login</Nav.Link>
                    </LinkContainer>
                    <LinkContainer to={'/signup'}>
                        <Nav.Link>Sign Up</Nav.Link>
                    </LinkContainer>
                </Nav>
            </Navbar.Collapse>
        </Container>
    </Navbar>
  )
}

export default AppNavbar