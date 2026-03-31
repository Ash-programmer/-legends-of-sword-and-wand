package com.example.domain;

public class PvPInvite {

    private int id;
    private int fromUserId;
    private String fromUsername;
    private int fromPartyId;
    private int toUserId;
    private String toUsername;
    private Integer toPartyId;
    private String status;

    public PvPInvite(int id,
                     int fromUserId,
                     String fromUsername,
                     int fromPartyId,
                     int toUserId,
                     String toUsername,
                     Integer toPartyId,
                     String status) {
        this.id = id;
        this.fromUserId = fromUserId;
        this.fromUsername = fromUsername;
        this.fromPartyId = fromPartyId;
        this.toUserId = toUserId;
        this.toUsername = toUsername;
        this.toPartyId = toPartyId;
        this.status = status;
    }

    public PvPInvite(int fromUserId,
                     String fromUsername,
                     int fromPartyId,
                     int toUserId,
                     String toUsername) {
        this(0, fromUserId, fromUsername, fromPartyId, toUserId, toUsername, null, "PENDING");
    }

    public int getId() { return id; }
    public int getFromUserId() { return fromUserId; }
    public String getFromUsername() { return fromUsername; }
    public int getFromPartyId() { return fromPartyId; }
    public int getToUserId() { return toUserId; }
    public String getToUsername() { return toUsername; }
    public Integer getToPartyId() { return toPartyId; }
    public String getStatus() { return status; }

    public void setId(int id) { this.id = id; }
    public void setToPartyId(Integer toPartyId) { this.toPartyId = toPartyId; }
    public void setStatus(String status) { this.status = status; }

    public boolean isPending() { return "PENDING".equals(status); }
    public boolean isAccepted() { return "ACCEPTED".equals(status); }
}
