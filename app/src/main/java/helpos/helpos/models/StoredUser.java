package helpos.helpos.models;

public class StoredUser {
    private String userName;
    private String userId;
    private String phoneNumber;
    private Integer karma;
    private boolean isOrg;
    public StoredUser(String userName, String userId, String phoneNumber, Integer karma, boolean isOrg){
        this.userId = userId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.karma = karma;
        this.isOrg = isOrg;
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

    public Integer getKarma() {
        return karma;
    }

    public void setKarma(Integer karma) {
        this.karma = karma;
    }

    public boolean isOrg() {
        return this.isOrg;
    }

    public void setOrg(boolean isOrg) {
        this.isOrg = isOrg;
    }
}

