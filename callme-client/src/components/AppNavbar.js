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
                        <Nav.Link>Home</Nav.Link>
                    </LinkContainer>
                    <LinkContainer to={'/friends'}>
                        <Nav.Link>Friends</Nav.Link>
                    </LinkContainer>
                    <LinkContainer to={'/recent'}>
                        <Nav.Link>Recent Calls</Nav.Link>
                    </LinkContainer>
                </Nav>
            </Navbar.Collapse>
        </Container>
    </Navbar>
  )
}

export default AppNavbar