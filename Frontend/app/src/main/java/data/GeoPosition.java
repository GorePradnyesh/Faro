package data;

public class GeoPosition {
    public final double latitude;
    public final double longitude;

    // TODO: double accuracy?
    // TODO: altitude; ?

    public GeoPosition(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
