public class Reservation {
    private final String pnr;
    private final String passengerName;
    private final String trainNumber;
    private final String trainName;
    private final String classType;
    private final String journeyDate;
    private final String source;
    private final String destination;
    private final int fare;

    public Reservation(String pnr, String passengerName, String trainNumber,
                       String trainName, String classType, String journeyDate,
                       String source, String destination, int fare) {
        this.pnr = pnr;
        this.passengerName = passengerName;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.classType = classType;
        this.journeyDate = journeyDate;
        this.source = source;
        this.destination = destination;
        this.fare = fare;
    }

    public String getPnr() { return pnr; }
    public String getPassengerName() { return passengerName; }
    public String getTrainNumber() { return trainNumber; }
    public String getTrainName() { return trainName; }
    public String getClassType() { return classType; }
    public String getJourneyDate() { return journeyDate; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public int getFare() { return fare; }
}