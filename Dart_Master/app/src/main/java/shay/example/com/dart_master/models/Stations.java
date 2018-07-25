package shay.example.com.dart_master.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Shay de Barra on 23,January,2018
 * Email:  x16115864@student.ncirl.ie
 */

@IgnoreExtraProperties
// required because Class is synced with firebase and expecting additional fields
public class Stations {

    @Exclude
    private String attending;
    private String code;
    private Double latitude;
    private Double longitude;
    private String name;
    private String id;
    private Boolean zone_active;

    public Stations() {
    }

    public Stations(String name, String id, Boolean zone_active, String attending, String code, Double latitude, Double longitude) {
        this.name = name;
        this.id = id;
        this.zone_active = zone_active;
        this.attending = attending;
        this.code = code;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Boolean getZone_active() {
        return zone_active;
    }

    public void setZone_active(Boolean zone_active) {
        this.zone_active = zone_active;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
