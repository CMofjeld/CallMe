export default class Invitation {
    id;
    userId;
    username;

    constructor(id, userId) {
        this.id = id;
        this.userId = userId;
    }

    static fromApiResponse(jsonResponse, myUserId) {
        let userId = jsonResponse.inviter === myUserId ? jsonResponse.invitee : jsonResponse.inviter;
        return new Invitation(jsonResponse.id, userId);
    }
}