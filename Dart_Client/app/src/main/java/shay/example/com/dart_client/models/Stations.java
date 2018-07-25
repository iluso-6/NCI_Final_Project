package shay.example.com.dart_client.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Shay on 23/01/2018.
 */

@IgnoreExtraProperties
// required because Class is synced with firebase and expecting additional fields
public class Stations {

    @Exclude

    private String code;
    private Double latitude;
    private Double longitude;
    private String name;
    private String id;
    private Boolean zone_active = false;// default to false instead of null !important
    private Boolean man_icon = false;// default to false
    private int zone_color;

    public int getZone_color() {
        return zone_color;
    }

    public void setZone_color(int zone_color) {
        this.zone_color = zone_color;
    }

    public int getZone_number() {
        return zone_number;
    }

    public void setZone_number(int zone_number) {
        this.zone_number = zone_number;
    }

    private int zone_number;

    public Stations( String code, Double latitude, Double longitude, String name, String id, Boolean zone_active, Boolean man_icon,int zone_color,int zone_number) {
        this.code = code;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.id = id;
        this.zone_active = zone_active;
        this.man_icon = man_icon;
        this.zone_color = zone_color;
        this.zone_number = zone_number;
    }

    public Stations() {
    }

    public Boolean getMan_icon() {
        return man_icon;
    }

    public void setMan_icon(Boolean man_icon) {
        this.man_icon = man_icon;
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
