export default class CallRecord {
    friendId;
    friendUsername;
    timestamp;
    status;
    type;

    constructor(friendId, timestamp, type) {
        this.friendId = friendId;
        this.timestamp = timestamp;
        this.type = type;
    }

    static fromApiResponse(jsonResponse, userId) {
        let callRecord = new CallRecord();
        if (userId === jsonResponse.caller) {
            callRecord.type = "outbound";
            callRecord.friendId = jsonResponse.receiver;
        } else {
            callRecord.type = "incoming";
            callRecord.friendId = jsonResponse.caller;
        }
        callRecord.status = jsonResponse.status;
        callRecord.timestamp = new Date(jsonResponse.startedAt);
        return callRecord;
    }
}