"""Runs a series of requests that test the CallMe backend services in an end-to-end fashion."""
import json
import os
import random
import string
import time
from typing import Any, Dict, List

import httpx

# HTTP client
base_url = os.environ.get("API_ADDRESS", "http://127.0.0.1:12345")
client = httpx.Client()

def generate_random_string(length: int=10) -> str:
    return "".join(random.choice(string.ascii_letters) for _ in range(length))

# Global constants
STARTUP_TIME = int(os.environ.get("STARTUP_TIME", "0"))
JSON_HEADER = {
    "Content-Type": "application/json"
}
BAD_ID = int(1e6)
BAD_NAME = generate_random_string()
BAD_PASS = generate_random_string()


def send_request(method: str, path: str, body: str=None, headers: Dict=None) -> httpx.Response:
    """Send a request to the backend and return the response object.

    Args:
        method (str): HTTP method (e.g. GET, POST, etc.)
        path (str): path to append to base url
        body (str, optional): body of the request. Defaults to None.
        headers (Dict, optional): headers for the request. Defaults to None.

    Returns:
        httpx.Response: response returned by the backend
    """
    request = httpx.Request(
        method=method,
        url=base_url + path,
        data=body,
        headers=httpx.Headers(headers=headers)
    )
    return client.send(request=request)

def json_content_matches_expected(body_str: str, expected_content: Dict) -> bool:
    body_json = json.loads(body_str)
    for key, value in expected_content.items():
        if key not in body_json or body_json[key] != value:
            return False
    return True

def list_content_matches(body_str:str, expected_content: List) -> bool:
    body_list = json.loads(body_str)
    for item in expected_content:
        found_match = False
        for i in range(len(body_list)):
            potential_match = body_list[i]
            is_match = True
            for key, value in item.items():
                if key not in potential_match or potential_match[key] != value:
                    is_match = False
                    break
            if is_match:
                body_list.pop(i)
                found_match = True
                break
        if not found_match:
            return False
    return len(body_list) == 0


def execute_test(test_description: str, expected_status: int, expected_content: Any=None, **kwargs) -> httpx.Response:
    """Execute the given test and print whether it succeeded or failed.

    Args:
        test_description (str): description of the test
        expected_status (int): expected HTTP status code for response
        expected_content (Any, optional): expected content for response. Defaults to None.
    """
    response = send_request(**kwargs)
    test_succeeded = True
    failure_reason = ""
    if response.status_code != expected_status:
        test_succeeded = False
        failure_reason += f", EXPECTED status code {expected_status}, GOT {response.status_code}"
    response_content = response.read().decode("utf8")
    if type(expected_content) is dict and not json_content_matches_expected(response_content, expected_content) \
        or type(expected_content) is list and not list_content_matches(response_content, expected_content) \
        or response_content != expected_content:
        failure_reason += f", EXPECTED content {expected_content}, GOT {response_content}"
    print(f"{test_description}: {('PASSED' if test_succeeded else 'FAILED' + failure_reason)}")
    return response

def evaluate_response(response: httpx.Response, test_description: str, expected_status: int=None, expected_content: Any=None) -> None:
    test_succeeded = True
    failure_reason = ""
    if expected_status is not None and response.status_code != expected_status:
        test_succeeded = False
        failure_reason += f", EXPECTED status code {expected_status}, GOT {response.status_code}"
    response_content = response.read().decode("utf8")
    if type(expected_content) is dict and not json_content_matches_expected(response_content, expected_content) \
        or type(expected_content) is list and not list_content_matches(response_content, expected_content) \
        or response_content != expected_content:
        failure_reason += f", EXPECTED content {expected_content}, GOT {response_content}"
    print(f"{test_description}: {('PASSED' if test_succeeded else 'FAILED' + failure_reason)}")

def print_header(content: str) -> None:
    length = len(content) + 4
    print()
    print("*" * length)
    print(f"* {content} *")
    print("*" * length)

def register_random_user() -> int:
    body = json.dumps({
        "username": generate_random_string(),
        "password": generate_random_string()
    })
    response = send_request(
        method="POST",
        path="/user/register",
        body=body,
        headers=JSON_HEADER
    )
    return response.json()

def send_friend_invitation(user1_id: int, user2_id: int) -> Dict:
    body = json.dumps({
        "inviter": user1_id,
        "invitee": user2_id
    })
    response = send_request(
        method="POST",
        path="/friends/invitation",
        body=body,
        headers=JSON_HEADER
    )
    return response.json()

def accept_friend_invitation(invitation_id: int) -> httpx.Response:
    return send_request(
        method="POST",
        path=f"/friends/invitation/{invitation_id}/accept"
    )

def make_friends(user1_id: int, user2_id: int) -> None:
    invitation_id = send_friend_invitation(user1_id, user2_id)["id"]
    accept_friend_invitation(invitation_id)

def set_user_status(user_id: int, status: str) -> httpx.Response:
    body = json.dumps({
        "id": user_id,
        "status": status
    })
    return send_request(
        method="POST",
        path="/status",
        body=body,
        headers=JSON_HEADER
    )

def get_user_status(user_id: int) -> httpx.Response:
    return send_request(
        method="GET",
        path=f"/status/{user_id}",
    )

def initiate_call(caller: int, receiver: int, handshakeInfo: str=None) -> httpx.Response:
    body = json.dumps({
        "caller": caller,
        "receiver": receiver,
        "handshakeInfo": handshakeInfo if handshakeInfo is not None else generate_random_string()
    })
    return send_request(
        method="POST",
        path="/calls/initiate",
        body=body,
        headers=JSON_HEADER
    )

def accept_call(call_id: str, handshakeInfo: str=None) -> httpx.Response:
    return send_request(
        method="POST",
        path=f"/calls/{call_id}/accept",
        body=handshakeInfo if handshakeInfo is not None else generate_random_string()
    )

def disconnect_call(call_id: str, handshakeInfo: str=None) -> httpx.Response:
    return send_request(
        method="POST",
        path=f"/calls/{call_id}/disconnect",
        body=handshakeInfo if handshakeInfo is not None else generate_random_string()
    )

def decline_call(call_id: str) -> httpx.Response:
    return send_request(
        method="POST",
        path=f"/calls/{call_id}/decline",
    )

def get_calls(user_id: int) -> httpx.Response:
    return send_request(
        method="GET",
        path=f"/calls/by/userId/{user_id}",
    )

def test_user_service():
    """Tests the user service."""
    print_header("EXECUTING TESTS FOR USER SERVICE")
    ####################### REGISTER #######################
    # NORMAL
    user1_name = generate_random_string()
    user1_pass = generate_random_string()
    body = json.dumps({
        "username": user1_name,
        "password": user1_pass
    })
    expected_content = {
            "username": user1_name
    }
    response = execute_test(
        test_description="Register user (normal)",
        expected_status=201,
        expected_content=expected_content,
        method="POST",
        path="/user/register",
        body=body,
        headers=JSON_HEADER
    )
    user1_id = response.json()["id"]
    # DUPLICATE USERNAME
    execute_test(
        test_description="Register user (duplicate username)",
        expected_status=409,
        method="POST",
        path="/user/register",
        body=body,
        headers=JSON_HEADER
    )
    ####################### LOGIN #######################
    # NORMAL
    body = json.dumps({
        "username": user1_name,
        "password": user1_pass
    })
    response = execute_test(
        test_description="Login (normal)",
        expected_status=201,
        method="POST",
        path="/user/login",
        body=body,
        headers=JSON_HEADER
    )
    token = response.json()["accessToken"]
    # INVALID USERNAME
    body = json.dumps({
        "username": BAD_NAME,
        "password": user1_pass
    })
    execute_test(
        test_description="Login (invalid username)",
        expected_status=404,
        method="POST",
        path="/user/login",
        body=body,
        headers=JSON_HEADER
    )
    # INVALID PASSWORD
    body = json.dumps({
        "username": user1_name,
        "password": BAD_PASS
    })
    execute_test(
        test_description="Login (invalid password)",
        expected_status=401,
        method="POST",
        path="/user/login",
        body=body,
        headers=JSON_HEADER
    )
    ####################### AUTHENTICATE #######################
    # NORMAL
    headers = {"Authorization": f"Bearer {token}"}
    execute_test(
        test_description="Authenticate (normal)",
        expected_status=200,
        method="GET",
        path="/user/authenticate",
        headers=headers
    )
    # NO TOKEN
    execute_test(
        test_description="Authenticate (no token)",
        expected_status=401,
        method="GET",
        path="/user/authenticate"
    )
    ####################### GET USER #######################
    # BY ID - NORMAL
    expected_content = {
            "id": user1_id,
            "username": user1_name
    }
    execute_test(
        test_description="Get user by ID (normal)",
        expected_status=200,
        expected_content=expected_content,
        method="GET",
        path=f"/user/{user1_id}"
    )
    # BY ID - INVALID ID
    execute_test(
        test_description="Get user by ID (invalid ID)",
        expected_status=404,
        method="GET",
        path=f"/user/{BAD_ID}"
    )
    # BY USERNAME - NORMAL
    execute_test(
        test_description="Get user by username (normal)",
        expected_status=200,
        expected_content=expected_content,
        method="GET",
        path=f"/user/by/username/{user1_name}"
    )
    # BY USERNAME - INVALID USERNAME
    execute_test(
        test_description="Get user by username (invalid username)",
        expected_status=404,
        method="GET",
        path=f"/user/by/username{BAD_NAME}"
    )
    ####################### LOGOUT #######################
    # NORMAL
    execute_test(
        test_description="Log out (normal)",
        expected_status=200,
        method="POST",
        path=f"/user/{user1_id}/logout"
    )
    # INVALID ID
    execute_test(
        test_description="Log out (invalid ID)",
        expected_status=404,
        method="POST",
        path=f"/user/{BAD_ID}/logout"
    )


def test_friend_service():
    """Tests the friend service.

    PRECONDITIONS:
    - User endpoints to register a user and get a user by ID are functioning as expected
    """
    print_header("EXECUTING TESTS FOR FRIEND SERVICE")
    ####################### INITIAL SETUP #######################
    # Create two users to exercise the following tests
    user1 = register_random_user()
    user1_id = user1["id"]
    user2 = register_random_user()
    user2_id = user2["id"]

    ####################### SEND INVITATION #######################
    # NORMAL
    body = {
        "inviter": user1_id,
        "invitee": user2_id
    }
    response = execute_test(
        test_description="Send invitation (normal)",
        expected_status=201,
        expected_content=body,
        method="POST",
        path="/friends/invitation",
        body=json.dumps(body),
        headers=JSON_HEADER
    )
    invitation = response.json()
    invitation_id = invitation["id"]
    # DUPLICATE INVITATION
    execute_test(
        test_description="Send invitation (duplicate invitation)",
        expected_status=409,
        method="POST",
        path="/friends/invitation",
        body=json.dumps(body),
        headers=JSON_HEADER
    )
    # INVALID USER ID
    body = json.dumps({
        "inviter": BAD_ID,
        "invitee": user2_id
    })
    execute_test(
        test_description="Send invitation (invalid user ID)",
        expected_status=404,
        method="POST",
        path="/friends/invitation",
        body=body,
        headers=JSON_HEADER
    )
    # SELF INVITATION
    body = json.dumps({
        "inviter": user1_id,
        "invitee": user1_id
    })
    execute_test(
        test_description="Send invitation (invitation to self)",
        expected_status=400,
        method="POST",
        path="/friends/invitation",
        body=body,
        headers=JSON_HEADER
    )

    ####################### GET INVITATIONS #######################
    # INCOMING - NORMAL
    expected_content = [invitation]
    execute_test(
        test_description="Get incoming invitations (normal)",
        expected_status=200,
        expected_content=expected_content,
        method="GET",
        path=f"/friends/invitation/user/{user2_id}/incoming"
    )
    # INCOMING - INVALID ID
    execute_test(
        test_description="Get incoming invitations (invalid user ID)",
        expected_status=404,
        method="GET",
        path=f"/friends/invitation/user/{BAD_ID}/incoming"
    )
    # OUTGOING - NORMAL
    expected_content = [invitation]
    execute_test(
        test_description="Get outgoing invitations (normal)",
        expected_status=200,
        expected_content=expected_content,
        method="GET",
        path=f"/friends/invitation/user/{user1_id}/outgoing"
    )
    # OUTGOING - INVALID ID
    execute_test(
        test_description="Get outgoing invitations (invalid user ID)",
        expected_status=404,
        method="GET",
        path=f"/friends/invitation/user/{BAD_ID}/outgoing"
    )

    ####################### ACCEPT INVITATION #######################
    # NORMAL
    execute_test(
        test_description="Accept invitation (normal)",
        expected_status=200,
        method="POST",
        path=f"/friends/invitation/{invitation_id}/accept"
    )
    # INVALID ID
    execute_test(
        test_description="Accept invitation (invalid invitation ID)",
        expected_status=404,
        method="POST",
        path=f"/friends/invitation/{BAD_ID}/accept"
    )

    ####################### DECLINE INVITATION #######################
    user3 = register_random_user()
    user3_id = user3["id"]
    invitation_id = send_friend_invitation(user1_id, user3_id)["id"]
    # NORMAL
    execute_test(
        test_description="Decline invitation (normal)",
        expected_status=200,
        method="POST",
        path=f"/friends/invitation/{invitation_id}/decline"
    )
    # INVALID ID
    execute_test(
        test_description="Decline invitation (invalid invitation ID)",
        expected_status=404,
        method="POST",
        path=f"/friends/invitation/{BAD_ID}/accept"
    )

    ####################### GET FRIENDS #######################
    # NORMAL
    expected_content = [user2]
    execute_test(
        test_description="Get friends (normal)",
        expected_status=200,
        expected_content=expected_content,
        method="GET",
        path=f"/friends/user/{user1_id}"
    )
    # INVALID ID
    execute_test(
        test_description="Get friends (invalid user ID)",
        expected_status=404,
        method="GET",
        path=f"/friends/user/{BAD_ID}"
    )

    ####################### ARE FRIENDS #######################
    # NORMAL - TRUE
    body = json.dumps({
        "user1": user1_id,
        "user2": user2_id
    })
    execute_test(
        test_description="Are friends - true (normal)",
        expected_status=200,
        method="POST",
        path=f"/friends/are_friends",
        body=body,
        headers=JSON_HEADER
    )
    # NORMAL - FALSE
    body = json.dumps({
        "user1": user1_id,
        "user2": user3_id
    })
    execute_test(
        test_description="Are friends - false (normal)",
        expected_status=400,
        method="POST",
        path=f"/friends/are_friends",
        body=body,
        headers=JSON_HEADER
    )
    # INVALID ID
    body = json.dumps({
        "user1": user1_id,
        "user2": BAD_ID
    })
    execute_test(
        test_description="Are friends (invalid user ID)",
        expected_status=404,
        method="POST",
        path=f"/friends/are_friends",
        body=body,
        headers=JSON_HEADER
    )


def test_status_service():
    """Tests the status service.

    PRECONDITIONS:
    - User endpoints to register a user and get a user by ID are functioning as expected
    """
    print_header("EXECUTING TESTS FOR STATUS SERVICE")
    ####################### INITIAL SETUP #######################
    # Create user to exercise the following tests
    user_id = register_random_user()["id"]

    ####################### SET/GET STATUS #######################
    # NORMAL
    body = {
        "id": user_id,
        "status": "online"
    }
    execute_test(
        test_description="Set status returns 201 (normal)",
        expected_status=201,
        method="POST",
        path=f"/status",
        body=json.dumps(body),
        headers=JSON_HEADER
    )
    execute_test(
        test_description="Get status returns previously set status (normal)",
        expected_status=200,
        expected_content=body,
        method="GET",
        path=f"/status/{user_id}"
    )
    set_user_status(user_id, "offline")
    body = {
        "id": user_id,
        "status": "offline"
    }
    execute_test(
        test_description="Get status returns updated status after change (normal)",
        expected_status=200,
        expected_content=body,
        method="GET",
        path=f"/status/{user_id}"
    )
    # INVALID ID
    body = {
        "id": BAD_ID,
        "status": "online"
    }
    execute_test(
        test_description="Set status (invalid ID)",
        expected_status=404,
        method="POST",
        path=f"/status",
        body=json.dumps(body),
        headers=JSON_HEADER
    )
    execute_test(
        test_description="Get status (invalid ID)",
        expected_status=404,
        method="GET",
        path=f"/status/{BAD_ID}"
    )

def test_call_service():
    """Tests the call service.

    PRECONDITIONS:
    - User endpoints to register a user and get a user by ID are functioning as expected
    - Friend endpoint to check if users are friends is functioning as expected
    - Status endpoints to get/set user status are functioning as expected
    """
    print_header("EXECUTING TESTS FOR CALL SERVICE")
    ####################### INITIAL SETUP #######################
    # Create 3 users
    user1_id = register_random_user()["id"]
    user2_id = register_random_user()["id"]
    user3_id = register_random_user()["id"]
    user4_id = register_random_user()["id"]
    # Set initial statuses
    set_user_status(user1_id, "online")
    set_user_status(user2_id, "online")
    set_user_status(user3_id, "online")
    set_user_status(user4_id, "online")
    # Establish friend relationship
    make_friends(user1_id, user2_id)

    ####################### INITIATE #######################
    # NORMAL
    response = initiate_call(user1_id, user2_id)
    evaluate_response(
        response=response,
        test_description="Initiate call returns 200 and call ID (normal)",
        expected_status=200
    )
    call_id = response.read().decode("utf8")
    evaluate_response(
        response=get_user_status(user1_id),
        test_description="User busy after successful initiate (normal)",
        expected_status=200,
        expected_content={
            "id": user1_id,
            "status": "busy"
        }
    )
    # INVALID USER ID
    evaluate_response(
        response=initiate_call(user3_id, BAD_ID),
        test_description="Initiate call (invalid user ID)",
        expected_status=404
    )
    # RECEIVER NOT FRIEND
    evaluate_response(
        response=initiate_call(user3_id, user4_id),
        test_description="Initiate call (receiver not a friend)",
        expected_status=412
    )
    # RECEIVER NOT ONLINE
    make_friends(user3_id, user4_id)
    set_user_status(user4_id, "offline")
    evaluate_response(
        response=initiate_call(user3_id, user4_id),
        test_description="Initiate call (receiver not online)",
        expected_status=412
    )

    ####################### ACCEPT #######################
    # NORMAL
    evaluate_response(
        response=accept_call(call_id),
        test_description="Accept call (normal)",
        expected_status=200
    )
    # CALL NOT PENDING
    evaluate_response(
        response=accept_call(call_id),
        test_description="Accept call (call not pending)",
        expected_status=400
    )
    # INVALID CALL ID
    evaluate_response(
        response=accept_call(BAD_NAME),
        test_description="Accept call (invalid call ID)",
        expected_status=404
    )

    ####################### DISCONNECT #######################
    # NORMAL
    evaluate_response(
        response=disconnect_call(call_id),
        test_description="Disconnect call returns 200 (normal)",
        expected_status=200
    )
    evaluate_response(
        response=get_user_status(user1_id),
        test_description="User online after successful disconnect (normal)",
        expected_content={
            "id": user1_id,
            "status": "online"
        }
    )
    # INVALID CALL ID
    evaluate_response(
        response=disconnect_call(BAD_NAME),
        test_description="Disconnect call (invalid call ID)",
        expected_status=404
    )

    ####################### DECLINE #######################
    # NORMAL
    make_friends(user2_id, user3_id)
    decline_id = initiate_call(user2_id, user3_id).read().decode("utf8")
    evaluate_response(
        response=decline_call(decline_id),
        test_description="Decline call returns 200 (normal)",
        expected_status=200
    )
    evaluate_response(
        response=get_user_status(user1_id),
        test_description="User online after successful decline (normal)",
        expected_content={
            "id": user3_id,
            "status": "online"
        }
    )
    # INVALID CALL ID
    evaluate_response(
        response=decline_call(BAD_NAME),
        test_description="Decline call (invalid call ID)",
        expected_status=404
    )

    ####################### GET CALLS #######################
    # NORMAL
    expected_content = [{
        "caller": user1_id,
        "receiver": user2_id
    }]
    evaluate_response(
        response=get_calls(user1_id),
        test_description="Get calls (normal)",
        expected_status=200,
        expected_content=expected_content
    )
    # INVALID USER ID
    evaluate_response(
        response=get_calls(BAD_ID),
        test_description="Get calls (invalid user ID)",
        expected_status=404
    )


def main():
    """Test the backend API in an end-to-end fashion."""
    # Wait specified time before executing tests
    time.sleep(STARTUP_TIME)

    # Execute the tests
    test_user_service()
    test_friend_service()
    test_status_service()
    test_call_service()


if __name__ == "__main__":
    main()