package shay.example.com.dart_client.models;

import java.io.Serializable;

/**
 * Created by Shay on 07/03/2018.
 */

public class User  implements Serializable {

    public User(String name, String photoUrl, String userID) {
        this.name = name;
        this.photoUrl = photoUrl;
        this.userID = userID;
    }
    public User(){

    }

    private String name;
    private String photoUrl;
    private String userID;
    private String phone_num;

    public User(String phone_num) {
        this.phone_num = phone_num;
    }

    public String getPhone_num() {
        return phone_num;
    }

    public void setPhone_num(String phone_num) {
        this.phone_num = phone_num;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }


}
