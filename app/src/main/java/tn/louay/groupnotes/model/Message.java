package tn.louay.groupnotes.model;

public class Message {
    private String id;
    private String content;
    private String groupId;
    private String senderId;
    private String senderName;
    private long createdAt;

    public Message() {
        // Required empty constructor for Firestore
    }

    public Message(String id, String content, String groupId, String senderId, String senderName, long createdAt) {
        this.id = id;
        this.content = content;
        this.groupId = groupId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
} 