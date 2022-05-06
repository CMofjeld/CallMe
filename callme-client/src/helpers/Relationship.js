export default class Relationship {
    relationshipId;
    friendId;
    friendUsername;
    relationshipStatus;
    type;

    constructor(relationshipId, friendId, friendUsername, relationshipStatus, type) {
        this.relationshipId = relationshipId;
        this.friendId = friendId;
        this.friendUsername = friendUsername;
        this.relationshipStatus = relationshipStatus;
        this.type = type;
    }

    static fromApiResponse(jsonResponse, userId) {
        let relationship = new Relationship();
        relationship.relationshipStatus = jsonResponse.status;
        relationship.relationshipId = jsonResponse.id;
        if (userId === jsonResponse.inviter) {
            relationship.friendId = jsonResponse.invitee;
            relationship.type = "outgoing";
        } else {
            relationship.friendId = jsonResponse.inviter;
            relationship.type = "incoming";
        }
        if (jsonResponse.status.toLowerCase() === "accepted") {
            relationship.type = "friend";
        }
        return relationship;
    }
}