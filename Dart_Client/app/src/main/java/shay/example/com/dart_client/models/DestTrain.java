package shay.example.com.dart_client.models;

/**
 * Created by Shay on 17/02/2018.
 */


// DestTrain destTrain =
// new DestTrain( traincode,  origin,  destination,  status,  direction,  scharrival,  exparrival,  duein,  late,  last_location);
public class DestTrain {

    private String traincode;
    private String origin;
    private String destination;
    private String train_type;
    private String direction;
    private String scharrival;
    private String exparrival;
    private String duein;
    private String late;
    private String last_location;

    OriginTrain originTrain;

    public DestTrain(String traincode, String origin, String destination, String train_type, String direction, String scharrival, String exparrival, String duein, String late, String last_location) {
        this.last_location = last_location;
        this.traincode = traincode;
        this.origin = origin;
        this.destination = destination;
        this.train_type = train_type;
        this.direction = direction;
        this.scharrival = scharrival;
        this.exparrival = exparrival;
        this.duein = duein;
        this.late = late;
    }

    public DestTrain() {
    }

    public int getSize() {
        return traincode.length();
    }

    public String getTraincode() {
        return traincode;
    }

    public void setTraincode(String traincode) {
        this.traincode = traincode;
    }

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

    public String getTrain_type() {
        return train_type;
    }

    public void setTrain_type(String train_type) {
        this.train_type = train_type;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getScharrival() {
        return scharrival;
    }

    public void setScharrival(String scharrival) {
        this.scharrival = scharrival;
    }

    public String getExparrival() {
        return exparrival;
    }

    public void setExparrival(String exparrival) {
        this.exparrival = exparrival;
    }

    public String getDuein() {
        return duein;
    }

    public void setDuein(String duein) {
        this.duein = duein;
    }

    public String getLate() {
        return late;
    }

    public void setLate(String late) {
        this.late = late;
    }

    public String getLast_location() {
        return last_location;
    }

    public void setLast_location(String last_location) {
        this.last_location = last_location;
    }
}
