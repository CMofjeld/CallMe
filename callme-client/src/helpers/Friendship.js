export default class Friendship {
    id;
    friendId;
    friendUsername;
    status;

    constructor(id, friendId, friendUsername) {
        this.id = id;
        this.friendId = friendId;
        this.friendUsername = friendUsername;
    }

    static fromApiResponse(jsonResponse) {
        let friendship = new Friendship();
        friendship.id = jsonResponse.id;
        friendship.friendId = jsonResponse.friendId;
        return friendship;
    }
}