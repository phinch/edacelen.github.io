package Location;

import Audio.AudioAttributes;
import Audio.AudioSnap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by prateek on 2/18/15.
 */
public class LocationManager {

    public ArrayList<Location> locations;
    public ArrayList<Location> smoothLocations;
    int MINUTES = 10;


    //** Paramters for predictions **//
    float MAXDISTANCEINSIDE = 0.08f; //in kilometers (0.1km ~ 300 feet)
    float MAXDISTANCEWALKING = 0.098f; //in kilometers (1.2k ~ 3937 feet or 0.75 miles)
    float MAXDISTANCERUNNING = 0.36f; //in kilometers (1.8k ~ 5905 feet or 1.11 miles)
    int SMOOTHINDOORSSECONDS= 120;
    float SMOOTHINDOORSFACTOR = 0.70f;
    int SMOOTHTRANSPORTSECONDS = 80;
    float SMOOTHTRANSPORTFACTOR = 0.70f;

    /**
     *
     * Takes in the destination of a CSV file and imports the locations
     *
     * @param csvFile Destination of the CSV
     * @return ArrayList of Locations
     */
    public ArrayList<Location> importFromDestination(String csvFile){

        //String csvFile = "/Users/prateek/Dropbox/Apps/GPS Tracks.csv";
        ArrayList<Location> locations = new ArrayList<Location>();
        Location currentLocation;
        BufferedReader br = null;
        String line = "";
        String csvSplitBy = ",";
        int i = 0;

        try {

            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] locationInformation = line.split(csvSplitBy);
                currentLocation = new Location();

                if (i != 0) { //Only add locations if they are not headings, i.e. row 1
                    currentLocation.gmtDate = parseDate(locationInformation[0]);
                    currentLocation.localDate = parseDate(locationInformation[1]);
                    currentLocation.roughSecondsFromStart = locationInformation[2];
                    currentLocation.latitude = parseLatLong(locationInformation[3]);
                    currentLocation.longitude = parseLatLong(locationInformation[4]);
                    currentLocation.horizontalAccuracy = locationInformation[5];
                    currentLocation.altitude = locationInformation[6];
                    currentLocation.verticalAccuracy = locationInformation[7];
                    currentLocation.distanceFromOrigin = locationInformation[8];
                    currentLocation.speed = locationInformation[9];
                    currentLocation.averageSpeed = parseAverageSpeed(locationInformation[10]);
                    currentLocation.course = locationInformation[11];
                    currentLocation.trueHeading = locationInformation[12];
                    currentLocation.magneticHeading = locationInformation[13];
                    currentLocation.headingAccuracy = locationInformation[14];
                    currentLocation.glideRatio = locationInformation[15];
                }

                i++;
                locations.add(currentLocation);

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Done: Imported " + (locations.size() - 1) + " rows\n");

        this.locations = locations;

        return locations;

    }

    /**
     *
     * Helper function for import, used to Parse latitude and longitude to floats
     *
     * @param string inputted latitude/longitude
     * @return latitude/longitude as a float
     */
    float parseLatLong(String string){
        return Float.parseFloat(string);
    }
    Date parseDate(String inputDate){

        Date result = null;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.ENGLISH);
        try {
            result =  df.parse(inputDate);
        } catch (ParseException e) {
            System.out.println("Date could not be converted!" + e.getStackTrace());
        }

        return result;
    }

    float parseAverageSpeed(String speed){

        return Float.parseFloat(speed);
    }

    public void analyzeLocations(){

        //I: Decide indoors versus outside
        analyzeIndoorsOrOutdoors();

        //II: Decide mode of transport
        analyzeModeOfTransport();
    }

    public void analyzeIndoorsOrOutdoors(){

        //Step 1: Fast forward the arrayList by MINUTES minutes
        int index = fastForwardMinutes(MINUTES);
        //System.out.println("Fast forwarded locations");

        //Step 2: Starting this index, look back MINUTES minutes before each location and get the previous location
        for(int i = index; i<locations.size(); i++){

            int previousIndex = goBackMinutes(i, MINUTES);

            //Step 3: Compute the distance between these two locations
            float lat1 = locations.get(previousIndex).latitude;
            float long1 = locations.get(previousIndex).longitude;
            float lat2 = locations.get(index).latitude;
            float long2 = locations.get(index).longitude;
            float distanceFromMinutesAgo = distanceFromLatLongPositions(lat1, long1, lat2, long2, 'K');

            /*if(i==90){
                System.out.println("kaboom!");
            }*/

            float averageSpeed = locations.get(i).averageSpeed;

            //Step 4: Decide whether indoorsOrOutdoors:
            // (if you've moved less than MAXDISTANCEINSIDE, you're indoors)
            if(distanceFromMinutesAgo > MAXDISTANCEINSIDE || locations.get(i).averageSpeed > 0.5){
                locations.get(i).indoors = IndoorsOutdoors.Outdoors; // indoors = false
            } else{
                locations.get(i).indoors = IndoorsOutdoors.Indoors; // indoors = true
            }

            //System.out.println(i+": " + locations.get(i).indoors);//locations.get(i).indoors);
        }


    }


    public void analyzeModeOfTransport(){

        //Step 1: Fast forward the arrayList by MINUTES minutes
        int index = fastForwardMinutes(2);
        //System.out.println("Fast forwarded locations");

        //Step 2: Starting this index, look back MINUTES minutes before each location and get the previous location
        for(int i = index; i<locations.size(); i++) {

            int previousIndex = goBackMinutes(i, 2);

            //Step 3: Compute the distance between these two locations
            float lat1 = locations.get(previousIndex).latitude;
            float long1 = locations.get(previousIndex).longitude;
            float lat2 = locations.get(index).latitude;
            float long2 = locations.get(index).longitude;
            float distanceFromMinutesAgo = distanceFromLatLongPositions(lat1, long1, lat2, long2, 'K');

            float averageSpeed = locations.get(i).averageSpeed;

            locations.get(i).temp = distanceFromMinutesAgo;

            //Step 4: Decide whether indoorsOrOutdoors:

            //-->Choice 1: Sitting
            if (distanceFromMinutesAgo < MAXDISTANCEINSIDE/5 && locations.get(i).averageSpeed < 0.5) {
                locations.get(i).transport = Transport.Sitting;
            }
            //-->Choice 2: Walking
            else if ((distanceFromMinutesAgo > MAXDISTANCEINSIDE/10 && distanceFromMinutesAgo < MAXDISTANCEWALKING) ||
                    (locations.get(i).averageSpeed > 0.5 && locations.get(i).averageSpeed < 2.4)) {
                locations.get(i).transport = Transport.Walking;
            }
            //-->Choice 2: Running
            else if ((distanceFromMinutesAgo > MAXDISTANCEWALKING && distanceFromMinutesAgo < MAXDISTANCERUNNING) ||
                    (locations.get(i).averageSpeed > 2.4 && locations.get(i).averageSpeed < 4.6)) {
                locations.get(i).transport = Transport.Running;
            }
            //-->Choice 3: Driving
            else if (distanceFromMinutesAgo > MAXDISTANCERUNNING || locations.get(i).averageSpeed> 4.6){
                locations.get(i).transport = Transport.Driving;
            }
            //-->Choice 4: Not Predictable
            else{
                locations.get(i).transport = Transport.Unsure;
            }


        }


    }



    /**
     *
     * Get the first index in locations when it has been atleast N minutes from the start
     *
     * @return
     */
    public int fastForwardMinutes(int n){

        Date date = locations.get(1).localDate;

        int result = -1;
        for(int i = 2; i<locations.size(); i++){

            //Get the date (and time) associated with the current date
            Date currDate = locations.get(i).localDate;

            //Get difference from first date and convert in minutes
            long differenceInMilliseconds = currDate.getTime() - date.getTime();
            long difference = TimeUnit.MILLISECONDS.toMinutes(differenceInMilliseconds);

            //If difference is more than MINUTES minutes, this is the index you want
            if(difference> n){
                result = i;
                break;
            }
        }

        return result;

    }

    /**
     *
     * Given an index in locations, look at the time and find the index of the latest location "minutes" minutes before
     * the location associated with the inputted index
     *
     * @param i inputted index
     * @return index associated a previous location which is MINUTES minutes before the location associated with the
     * inputted index
     */
    public int goBackMinutes(int i, int minutes){

        //This is the date and time for the inputted index
        Date date = locations.get(i).localDate;

        //Keep going backwards..
        while(i>0){

            //Another location "difference" minutes ago
            Date currDate = locations.get(i).localDate;
            long differenceInMilliseconds = date.getTime() - currDate.getTime();
            long difference = TimeUnit.MILLISECONDS.toMinutes(differenceInMilliseconds);

            //If this date is more than 10 minutes ago, return this date
            if(difference>minutes){
                return i;
            }

            i--;

        }

        //If there is no date that is 10 minutes before the inputted date, return the first location value
        return i;
    }

    /**
     *
     * Used to calculate the distance between two positions given by their latitudes and longitudes in miles or kilometers
     *
     * @param lat1 Latitude 1
     * @param lon1 Longitude 1
     * @param lat2 Latitude 2
     * @param lon2 Longitude 2
     * @param unit "K" for kilometers, "M" for miles
     * @return distance between the two points
     */
    public float distanceFromLatLongPositions(float lat1, float lon1, float lat2, float lon2, char unit) {

        double theta = lon1 - lon2;
        double dist = Math.sin(degreesToRadians(lat1)) * Math.sin(degreesToRadians(lat2)) + Math.cos(degreesToRadians(lat1)) * Math.cos(degreesToRadians(lat2)) * Math.cos(degreesToRadians(theta));
        dist = Math.acos(dist);
        dist = radiansToDegrees(dist);
        dist = dist * 60 * 1.1515;

        if (unit == 'K') {
            dist = dist * 1.609344;
        }
        return (float)dist;
    }

    /**
     * Helper function to convert degrees to radians
     *
     * @param degrees inputted degrees
     * @return degrees converted to radians
     */
    public double degreesToRadians(double degrees) {
        return (degrees * Math.PI / 180.0);
    }

    /**
     *
     * Helper function to convert radians to degrees
     *
     * @param radians inputted radians
     * @return radians converted to degrees
     */
    public double radiansToDegrees(double radians) {
        return (radians * 180 / Math.PI);
    }


    public void printPredictions(ArrayList<Location> locations){

        for(int i = 1; i<locations.size(); i++){

            System.out.println(i + "at " + locations.get(i).localDate + " with speed " + locations.get(i).averageSpeed + "and dist: "+ locations.get(i).temp +"==> " + locations.get(i).printPrediction());
        }

    }

    /**
     *
     * Smooths predictions based on analyzeLocations()
     *
     */
    public ArrayList<Location> smoothPredictions(){

        //Step 1 :Initialize Smooth Locations to predicted locations
        smoothLocations = locations;

        //Step 2: Smooth predictions (Noise Reduction Phase)
        smoothIndoorsOutdoors(smoothLocations);
        smoothTransport(smoothLocations);

        return smoothLocations;
    }


    /**
     *
     * Smooths location predictions about indoors/outdoors (Noise reduction in predictions)
     *
     */
    public void smoothIndoorsOutdoors(ArrayList<Location> locations){

        IndoorsOutdoors temp;
        temp = locations.get(1).indoors;

        for(int i = 1; i< locations.size(); i++){

            IndoorsOutdoors value = locations.get(i).indoors;
            //If indoors value for this is different than the last
            if(value!=temp){
                //if the change does not make sense
                if(!checkIndoorsOutdoorsChange(locations, i, value)){
                    //Smooth data and change the value back to temp (previous location's value)
                    locations.get(i).indoors = temp;
                }
            }

            //Update temp
            temp = locations.get(i).indoors;
        }

    }


    /**
     *
     * Check if atleast SMOOTHFACTOR of the locations in the next SMOOTHSECOND seconds have value "value"
     *
     * @param locations list with locations
     * @param index the index to start looking up from
     * @param value the value to compare/check
     *
     * @return boolean representing if the next locations have enough value
     *
     */
    public boolean checkIndoorsOutdoorsChange(ArrayList<Location> locations, int index, IndoorsOutdoors value){

        int success = 0; //number of posts with value value
        int totalLocations = 0; //total number of locations we encounter

        Date indexDate = locations.get(index).localDate;

        for(int i = index; i<locations.size(); i++) {


            Date currDate = locations.get(i).localDate;
            long differenceInMilliseconds = currDate.getTime() - indexDate.getTime();
            long difference = TimeUnit.MILLISECONDS.toSeconds(differenceInMilliseconds);

            //If it has been more than SMOOTHSECONDS seconds from the time of the index, stop!
            if(difference>SMOOTHINDOORSSECONDS){
                break;
            }

            //Else, keep counting:
            if(locations.get(i).indoors == value){
                success++;// Add a success if the value matches
            }
            totalLocations++;//Increase total locations you have seen anyways
        }

        float ratio = (float) success/totalLocations;

        return ratio>SMOOTHINDOORSFACTOR;
    }

    /**
     *
     * Smooths location predictions about transport (Noise reduction in predictions)
     *
     *
     */
    public void smoothTransport(ArrayList<Location> locations){

        Transport temp;
        temp = locations.get(1).transport;

        for(int i = 1; i< locations.size(); i++){

            Transport value = locations.get(i).transport;
            //If indoors value for this is different than the last
            if(value!=temp){
                //if the change does not make sense
                if(!checkTransportChange(locations, i, value)){
                    //Smooth data and change the value back to temp (previous location's value)
                    locations.get(i).transport = temp;
                }
            }

            //Update temp
            temp = locations.get(i).transport;
        }

    }


    /**
     *
     * Check if atleast SMOOTHFACTOR of the locations in the next SMOOTHSECOND seconds have value "value"
     *
     * @param locations list with locations
     * @param index the index to start looking up from
     * @param value the value to compare/check
     *
     * @return boolean representing if the next locations have enough value
     *
     */
    public boolean checkTransportChange(ArrayList<Location> locations, int index, Transport value){

        int success = 0; //number of posts with value value
        int totalLocations = 0; //total number of locations we encounter

        Date indexDate = locations.get(index).localDate;

        for(int i = index; i<locations.size(); i++) {


            Date currDate = locations.get(i).localDate;
            long differenceInMilliseconds = currDate.getTime() - indexDate.getTime();
            long difference = TimeUnit.MILLISECONDS.toSeconds(differenceInMilliseconds);

            //If it has been more than SMOOTHSECONDS seconds from the time of the index, stop!
            if(difference>SMOOTHTRANSPORTSECONDS){
                break;
            }

            //Else, keep counting:
            if(locations.get(i).transport == value){
                success++;// Add a success if the value matches
            }
            totalLocations++;//Increase total locations you have seen anyways
        }

        float ratio = (float) success/totalLocations;

        return ratio>SMOOTHTRANSPORTFACTOR;
    }


    /**
     *
     * This method goes through smoothedLocations and calculates the audio transitions and generates an ArrayList with
     * corresponding audioSnap element
     *
     * @return ArrayList of AudioSnaps
     */
    public ArrayList<AudioSnap> calculateTransitions(){

        ArrayList<AudioSnap> audioSnaps = new ArrayList<AudioSnap>();
        Transport currentTransport = smoothLocations.get(1).transport;
        IndoorsOutdoors currentIndoors = smoothLocations.get(1).indoors;
        AudioSnap audioSnap;

        //Go through the smoothLocations
        for(int i = 1; i<smoothLocations.size();i++){

            //If indoors or transport changes
            if(smoothLocations.get(i).indoors!=currentIndoors  || smoothLocations.get(i).transport !=currentTransport){

                // and if neither of the values are "Unsure"
                if(smoothLocations.get(i).indoors!=IndoorsOutdoors.Unsure && smoothLocations.get(i).transport!=Transport.Unsure){

                    //End the last Audio Snap if it is present (i.e. set its end date/time)
                    if(!audioSnaps.isEmpty()){
                        audioSnaps.get(audioSnaps.size()-1).setEnd(smoothLocations.get(i).localDate);
                    }

                    //And add a new audioSnap
                    AudioAttributes audioAttributes = new AudioAttributes(smoothLocations.get(i).indoors, smoothLocations.get(i).transport);
                    audioSnap = new AudioSnap(smoothLocations.get(i).localDate, audioAttributes);
                    audioSnaps.add(audioSnap);

                }

            }

            //Update currentTransport and currentIndoors
            currentIndoors = smoothLocations.get(i).indoors;
            currentTransport = smoothLocations.get(i).transport;

        }

        //End the last audioSnap
        audioSnaps.get(audioSnaps.size()-1).setEnd(smoothLocations.get(smoothLocations.size()-1).localDate);

        return audioSnaps;

    }

    /**
     *
     * Print all snaps for debugging purposes
     *
     * @param snaps Input snaps
     */
    public void printAudioSnaps(ArrayList<AudioSnap> snaps){

        for(AudioSnap snap: snaps){
            System.out.println(snap+"\n");
        }

    }

}
