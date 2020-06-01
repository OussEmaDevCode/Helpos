package helpos.helpos.models;

import java.io.Serializable;
import java.util.List;

public class HelpRequest implements Serializable {
    private String title;
    private String description;
    private int price;
    private boolean isPay;
    private List<Double> latlong;
    private String uid;
    private String id;
    private String uName;
    private String personHelping;
    private boolean isOrg;

    public HelpRequest(String title, String description, int price, boolean isPay,
                       List<Double> latlong, String uid, String id, String uName, String personHelping, boolean isOrg){
        this.title = title;
        this.description = description;
        this.price = price;
        this.isPay = isPay;
        this.latlong = latlong;
        this.uid = uid;
        this.id = id;
        this.uName = uName;
        this.personHelping = personHelping;
        this.isOrg = isOrg;
    }
    public HelpRequest () {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean isPay() {
        return isPay;
    }

    public void setPay(boolean pay) {
        isPay = pay;
    }

    public List<Double> getLatlong() {
        return latlong;
    }

    public void setLatlong(List<Double> latlong) {
        this.latlong = latlong;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getPersonHelping() {
        return personHelping;
    }

    public void setPersonHelping(String personHelping) {
        this.personHelping = personHelping;
    }

    public boolean isOrg() {
        return isOrg;
    }

    public void setOrg(boolean org) {
        isOrg = org;
    }
}
