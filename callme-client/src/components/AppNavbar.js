import React from 'react'
import PropTypes from 'prop-types'
import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';

const AppNavbar = props => {
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
            <Button variant='outline-primary' onClick={props.logout}>Sign Out</Button>
        </Container>
    </Navbar>
  )
}

AppNavbar.propTypes = {
  logout: PropTypes.func.isRequired,
}

export default AppNavbar