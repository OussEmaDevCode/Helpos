package helpos.helpos.models;

public class StoredUser {
    private String userName;
    private String userId;
    private String phoneNumber;
    private int karma;
    public StoredUser(String userName, String userId, String phoneNumber, int karma){
        this.userId = userId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.karma = karma;
    }
    public StoredUser(){}
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getKarma() {
        return karma;
    }

    public void setKarma(int karma) {
        this.karma = karma;
    }
}

