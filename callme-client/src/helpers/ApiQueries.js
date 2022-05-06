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