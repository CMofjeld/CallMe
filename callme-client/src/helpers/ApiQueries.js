import ApiHelper from "./ApiHelper";

export async function getUsername(id, token, apiHostname) {
  let apiHelper = new ApiHelper();
  let jsonResponse;
  let getUrl = "http://" + apiHostname + "/user/" + id;
  try {
    const result = await apiHelper.callApi(getUrl, "GET", {"token": token});
    jsonResponse = await result.json();
  } catch (error) {
    console.log(error);
    return null;
  }
  return jsonResponse.username;
}

export async function getUserId(username, token, apiHostname) {
  let apiHelper = new ApiHelper();
  let getUrl = "http://" + apiHostname + "/user/by/username/" + username;
  try {
    const result = await apiHelper.callApi(getUrl, "GET", {"token": token});
    if (result.status === 404) {
      alert("Couldn't find anyone with that username");
      return null;
    }
    const jsonResponse = await result.json();
    return jsonResponse.id;
  } catch (error) {
    console.log(error);
    return null;
  }
}

export async function getStatus(id, token, apiHostname) {
  let apiHelper = new ApiHelper();
  let jsonResponse;
  let getUrl = "http://" + apiHostname + "/status/" + id;
  try {
    const result = await apiHelper.callApi(getUrl, "GET", {"token": token});
    jsonResponse = await result.json();
  } catch (error) {
    console.log(error);
    return null;
  }
  return jsonResponse.status;
}

export async function getOutgoingInvitations(id, token, apiHostname) {
  const apiHelper = new ApiHelper();
  let jsonResponse;
  let getUrl = "http://" + apiHostname + "/friends/invitation/user/" + id + "/outgoing";
  try {
    const result = await apiHelper.callApi(getUrl, "GET", {"token": token});
    jsonResponse = await result.json();
  } catch (error) {
    console.log(error);
    return null;
  }
  return jsonResponse;
}

export async function postCallInitiate(userId, receiverId, apiToken, apiHostname, handshakeInfo) {
  const apiHelper = new ApiHelper();
  let callId = null;
  let postUrl = "http://" + apiHostname + "/calls/initiate";
  let body = JSON.stringify({
    "caller": userId,
    "receiver": receiverId,
    "handshakeInfo": handshakeInfo
  });
  console.log(body);
  console.log(receiverId);
  const response = await apiHelper.callApi(postUrl, 'POST', {body: body, token: apiToken});
  if (response.status === 200) {
    callId = await response.text();
  } else {
    console.error(response.status);
    let responseJson = await response.json();
    let errorMessage = responseJson.message;
    alert(errorMessage);
  }
  return callId;
}

export async function postCallDisconnect(callId, apiToken, apiHostname) {
  const apiHelper = new ApiHelper();
  let postUrl = "http://" + apiHostname + "/calls/" + callId + "/disconnect";
  const response = await apiHelper.callApi(postUrl, 'POST', {token: apiToken});
  return response.status;
}

export async function postCallDecline(callId, apiToken, apiHostname) {
  const apiHelper = new ApiHelper();
  let postUrl = "http://" + apiHostname + "/calls/" + callId + "/decline";
  const response = await apiHelper.callApi(postUrl, 'POST', {token: apiToken});
  return response.status;
}

export async function postCallAccept(callId, handshakeInfo, apiToken, apiHostname) {
  const apiHelper = new ApiHelper();
  let postUrl = "http://" + apiHostname + "/calls/" + callId +"/accept";
  const response = await apiHelper.callApi(postUrl, 'POST', {body: handshakeInfo, token: apiToken});
  return response.status;
}

export async function getCallRecords(userId, apiToken, apiHostname) {
  const apiHelper = new ApiHelper();
  let jsonResponse = [];
  let getUrl = "http://" + apiHostname + "/calls/by/userId/" + userId;
  try {
    const result = await apiHelper.callApi(getUrl, "GET", {token: apiToken});
    jsonResponse = await result.json();
  } catch (error) {
    console.log(error);
  }
  return jsonResponse;
}

export async function postLogout(userId, apiToken, apiHostname) {
  const apiHelper = new ApiHelper();
  let postUrl = "http://" + apiHostname + "/user/" + userId +"/logout";
  const response = await apiHelper.callApi(postUrl, 'POST', {token: apiToken});
  return response.status;
}