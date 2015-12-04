package Location;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by prateek on 2/18/15.
 */
public class Location {

    public Date gmtDate; //TODO: Change to some date form
    public Date localDate; //TODO: Change to some date form
    public String roughSecondsFromStart;
    public float latitude;
    public float longitude;
    public String horizontalAccuracy;
    public String altitude;
    public String verticalAccuracy;
    public String distanceFromOrigin;
    public String speed;
    public float averageSpeed;
    public String course;
    public String trueHeading;
    public String magneticHeading;
    public String headingAccuracy;
    public String glideRatio;
    public float temp;

    //Predictions
    public IndoorsOutdoors indoors = IndoorsOutdoors.Unsure;
    public Transport transport = Transport.Unsure;

    @Override
    public String toString(){

        /*System.out.println("Location==> GMT Date: " + locationInformation[0]
                + " , Local Date =" + locationInformation[1] + "]"
                + " , Time(sec) =" + locationInformation[2] + "]"
                + " , Latitude =" + locationInformation[3] + "]"
                + " , Longitude =" + locationInformation[4] + "]"
                + " , Horizontal Accuracy =" + locationInformation[5] + "]"
                + " , Altitude(m) =" + locationInformation[6] + "]"
                + " , Vertical Accuracy(m) =" + locationInformation[7] + "]"
                + " , Distance(m) =" + locationInformation[8] + "]"
                + " , Speed(m/s) =" + locationInformation[9] + "]"
                + " , Average Speed(m/s) =" + locationInformation[10] + "]"
                + " , Course(deg) =" + locationInformation[11] + "]"
                + " , True Heading(deg) =" + locationInformation[12] + "]"
                + " , Magnetic Heading(deg) =" + locationInformation[13] + "]"
                + " , Heading Accuracy =" + locationInformation[14] + "]"
                + " , Glide Ratio =" + locationInformation[15] + "]");*/




        return "";

    }


    /**
     *
     * Get the date string (without the time i.e. minutes, hours etc.) in the format YYYY-MM-DD
     *
     * @return returns the date string (YYYY-MM-DD)
     */
    public String getDateString(){

        StringBuilder toReturn = new StringBuilder();
        Calendar cal = Calendar.getInstance();
        cal.setTime(localDate);

        String month = add0IfNeeded(Calendar.MONTH);
        String day = add0IfNeeded(Calendar.DAY_OF_MONTH);

        toReturn.append(cal.get(Calendar.YEAR));
        toReturn.append("-");
        toReturn.append(month);
        toReturn.append("-");
        toReturn.append(day);

        return toReturn.toString();
    }


    /**
     *
     * Helper function for getDateString to format single digits, i.e. place a zero before the digit
     *
     * @param n the number
     * @return Formatted string
     */
    String add0IfNeeded(int n){

        //If it has only one digit, add a zero before it
        if(n<10){
            return "0"+Integer.toString(n);
        } else{
            return Integer.toString(n);
        }

    }

    public String printPrediction(){

        return "Indoors: " + indoors + ", Transport: " + transport.name();
    }

}
