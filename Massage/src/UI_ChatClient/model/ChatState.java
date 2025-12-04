package UI_ChatClient.model;

/**
 * Lớp chứa trạng thái chat hiện tại
 */
public class ChatState {
    private String myUsername;
    private String myFullName;
    private String currentChatTarget;
    private boolean currentChatIsGroup;
    
    // Trạng thái cuộc gọi
    private boolean inCall;
    private boolean inVideoCall;
    private String callPartnerUsername;
    private boolean isRecording;
    
    public ChatState(String myUsername, String myFullName) {
        this.myUsername = myUsername;
        this.myFullName = myFullName;
        this.currentChatTarget = null;
        this.currentChatIsGroup = false;
        this.inCall = false;
        this.inVideoCall = false;
        this.callPartnerUsername = null;
        this.isRecording = false;
    }
    
    // Getters
    public String getMyUsername() { return myUsername; }
    public String getMyFullName() { return myFullName; }
    public String getCurrentChatTarget() { return currentChatTarget; }
    public boolean isCurrentChatIsGroup() { return currentChatIsGroup; }
    public boolean isInCall() { return inCall; }
    public boolean isInVideoCall() { return inVideoCall; }
    public String getCallPartnerUsername() { return callPartnerUsername; }
    public boolean isRecording() { return isRecording; }
    
    // Setters
    public void setMyUsername(String myUsername) { this.myUsername = myUsername; }
    public void setMyFullName(String myFullName) { this.myFullName = myFullName; }
    public void setCurrentChatTarget(String currentChatTarget) { this.currentChatTarget = currentChatTarget; }
    public void setCurrentChatIsGroup(boolean currentChatIsGroup) { this.currentChatIsGroup = currentChatIsGroup; }
    public void setInCall(boolean inCall) { this.inCall = inCall; }
    public void setInVideoCall(boolean inVideoCall) { this.inVideoCall = inVideoCall; }
    public void setCallPartnerUsername(String callPartnerUsername) { this.callPartnerUsername = callPartnerUsername; }
    public void setRecording(boolean recording) { isRecording = recording; }
}
