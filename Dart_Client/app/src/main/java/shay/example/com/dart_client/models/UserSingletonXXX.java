package shay.example.com.dart_client.models;

/**
 * Created by Shay de Barra on 10,April,2018
 * Email:  x16115864@student.ncirl.ie
 */
public class UserSingletonXXX {
    private static final UserSingletonXXX ourInstance = new UserSingletonXXX();

    public static UserSingletonXXX getInstance() {
        return ourInstance;
    }

    private UserSingletonXXX() {
    }
    private String name;
    private String photoUrl;
    private String userID;

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

    public String getPhone_num() {
        return phone_num;
    }

    public void setPhone_num(String phone_num) {
        this.phone_num = phone_num;
    }

    private String phone_num;
}
