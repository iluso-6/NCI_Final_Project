package shay.example.com.dart_master.models;

import java.io.Serializable;
/**
 * Created by Shay de Barra on 17,February,2018
 * Email:  x16115864@student.ncirl.ie
 */

public class JourneyObj implements Serializable {


    private String org_traincode;
    private String org_origin;
    private String org_destination;
    private String org_train_type;
    private String org_direction;
    private String org_scharrival;
    private String org_exparrival;
    private String org_duein;
    private String org_late;
    private String org_last_location;

    private String dest_traincode;
    private String dest_origin;
    private String dest_destination;
    private String dest_train_type;
    private String dest_direction;
    private String dest_scharrival;
    private String dest_exparrival;
    private String dest_duein;
    private String dest_late;
    private String dest_last_location;

    private String origin;

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    private String destination;

    public JourneyObj() {
    }

    // user details populated once before sending to firebase
    public JourneyObj(String user_name, String user_url, String user_phone, String ticket_image, String journey_info) {
        this.user_name = user_name;
        this.user_url = user_url;
        this.user_phone = user_phone;
        this.ticket_image = ticket_image;
        this.journey_info = journey_info;
    }

    private String user_name;
    private String user_url;
    private String user_phone;
    private String ticket_image;
    private String journey_info;


    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_url() {
        return user_url;
    }

    public void setUser_url(String user_url) {
        this.user_url = user_url;
    }

    public String getUser_phone() {
        return user_phone;
    }

    public void setUser_phone(String user_phone) {
        this.user_phone = user_phone;
    }

    public String getTicket_image() {
        return ticket_image;
    }

    public void setTicket_image(String ticket_image) {
        this.ticket_image = ticket_image;
    }

    public String getJourney_info() {
        return journey_info;
    }

    public void setJourney_info(String journey_info) {
        this.journey_info = journey_info;
    }

    // train details of both OriginTrain and DestTrain merged for sending to firebase
    public JourneyObj(String org_traincode, String org_origin, String org_destination, String org_train_type, String org_direction, String org_scharrival, String org_exparrival, String org_duein, String org_late, String org_last_location, String dest_traincode, String dest_origin, String dest_destination, String dest_train_type, String dest_direction, String dest_scharrival, String dest_exparrival, String dest_duein, String dest_late, String dest_last_location) {
        this.org_traincode = org_traincode;
        this.org_origin = org_origin;
        this.org_destination = org_destination;
        this.org_train_type = org_train_type;
        this.org_direction = org_direction;
        this.org_scharrival = org_scharrival;
        this.org_exparrival = org_exparrival;
        this.org_duein = org_duein;
        this.org_late = org_late;
        this.org_last_location = org_last_location;
        this.dest_traincode = dest_traincode;
        this.dest_origin = dest_origin;
        this.dest_destination = dest_destination;
        this.dest_train_type = dest_train_type;
        this.dest_direction = dest_direction;
        this.dest_scharrival = dest_scharrival;

        this.dest_exparrival = dest_exparrival;
        this.dest_duein = dest_duein;
        this.dest_late = dest_late;
        this.dest_last_location = dest_last_location;
    }
    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }

    private  boolean viewed =false;

    // Firebase reference to dynamic database location reference
    public String getFirebaseRef() {
        return firebaseRef;
    }

    public void setFirebaseRef(String firebaseRef) {
        this.firebaseRef = firebaseRef;
    }

    private String firebaseRef;


    public String getOrg_traincode() {
        return org_traincode;
    }

    public void setOrg_traincode(String org_traincode) {
        this.org_traincode = org_traincode;
    }

    public String getOrg_origin() {
        return org_origin;
    }

    public void setOrg_origin(String org_origin) {
        this.org_origin = org_origin;
    }

    public String getOrg_destination() {
        return org_destination;
    }

    public void setOrg_destination(String org_destination) {
        this.org_destination = org_destination;
    }

    public String getOrg_train_type() {
        return org_train_type;
    }

    public void setOrg_train_type(String org_train_type) {
        this.org_train_type = org_train_type;
    }

    public String getOrg_direction() {
        return org_direction;
    }

    public void setOrg_direction(String org_direction) {
        this.org_direction = org_direction;
    }

    public String getOrg_scharrival() {
        return org_scharrival;
    }

    public void setOrg_scharrival(String org_scharrival) {
        this.org_scharrival = org_scharrival;
    }

    public String getOrg_exparrival() {
        return org_exparrival;
    }

    public void setOrg_exparrival(String org_exparrival) {
        this.org_exparrival = org_exparrival;
    }

    public String getOrg_duein() {
        return org_duein;
    }

    public void setOrg_duein(String org_duein) {
        this.org_duein = org_duein;
    }

    public String getOrg_late() {
        return org_late;
    }

    public void setOrg_late(String org_late) {
        this.org_late = org_late;
    }

    public String getOrg_last_location() {
        return org_last_location;
    }

    public void setOrg_last_location(String org_last_location) {
        this.org_last_location = org_last_location;
    }

    public String getDest_traincode() {
        return dest_traincode;
    }

    public void setDest_traincode(String dest_traincode) {
        this.dest_traincode = dest_traincode;
    }

    public String getDest_origin() {
        return dest_origin;
    }

    public void setDest_origin(String dest_origin) {
        this.dest_origin = dest_origin;
    }

    public String getDest_destination() {
        return dest_destination;
    }

    public void setDest_destination(String dest_destination) {
        this.dest_destination = dest_destination;
    }

    public String getDest_train_type() {
        return dest_train_type;
    }

    public void setDest_train_type(String dest_train_type) {
        this.dest_train_type = dest_train_type;
    }

    public String getDest_direction() {
        return dest_direction;
    }

    public void setDest_direction(String dest_direction) {
        this.dest_direction = dest_direction;
    }

    public String getDest_scharrival() {
        return dest_scharrival;
    }

    public void setDest_scharrival(String dest_scharrival) {
        this.dest_scharrival = dest_scharrival;
    }

    public String getDest_exparrival() {
        return dest_exparrival;
    }

    public void setDest_exparrival(String dest_exparrival) {
        this.dest_exparrival = dest_exparrival;
    }

    public String getDest_duein() {
        return dest_duein;
    }

    public void setDest_duein(String dest_duein) {
        this.dest_duein = dest_duein;
    }

    public String getDest_late() {
        return dest_late;
    }

    public void setDest_late(String dest_late) {
        this.dest_late = dest_late;
    }

    public String getDest_last_location() {
        return dest_last_location;
    }

    public void setDest_last_location(String dest_last_location) {
        this.dest_last_location = dest_last_location;
    }




}
