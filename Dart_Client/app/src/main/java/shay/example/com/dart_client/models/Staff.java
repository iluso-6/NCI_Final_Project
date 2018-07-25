package shay.example.com.dart_client.models;

import java.io.Serializable;

/**
 * Created by Shay on 20/11/2017.
 */

public class Staff  implements Serializable {

    private String name;
    private String email;
    private String url;

    public String getStaffID() {
        return staffID;
    }

    public void setStaffID(String staffID) {
        this.staffID = staffID;
    }

    private String staffID;



    public Staff(String name, String email, String url, String staffID) {
        this.name = name;
        this.email = email;
        this.url = url;
        this.staffID = staffID;
    }

    public Staff() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


}
