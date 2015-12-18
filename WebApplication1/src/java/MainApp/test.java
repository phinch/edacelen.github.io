package MainApp;

import Location2.Location2;
import Location2.LocationManager2;
import Surrounding.SurroundingDatabase;
import Weather.WeatherDatabase;

import Weather.WeatherInfo;
import java.io.File;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.mongodb.DBCollection;

/**
 * Created by Jiahui on 06/10/15.
 */
public class test {
       
    public static LocationManager2 locationManager = new LocationManager2();
    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    //II. Get Sample coordinateS, Select Interesting Locations, and Get Weather Information of Interesting Locations
    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    // initialize mongodb for interesting locations(SurroundingDatabase) and weather(WeatherDatabase)
    public static SurroundingDatabase surroundingDatabase = new SurroundingDatabase();
    public static DBCollection surroundingTable = surroundingDatabase.initialize();
    // initialize mongodb for weather(WeatherDatabase)
    public static WeatherDatabase weatherDatabase = new WeatherDatabase();
    public static DBCollection weatherTable = weatherDatabase.initialize();

    
    public static ArrayList<ArrayList<Location2>> getTrips(String[] inputFile){
        LocationManager2 locationManager = new LocationManager2();
        ArrayList<ArrayList<Location2>> outputTrips = new ArrayList<ArrayList<Location2>>();
       
        try{
            //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
            //I. Location Manager
            //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=- =-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

            System.out.println("I. Location Manager");
            ArrayList<Location2> inputLocation = locationManager.generateLocationsFromFile(inputFile);
            outputTrips = locationManager.modePrediction(inputLocation);
            
        }catch (Exception ex){
            System.out.println("Exception: "+ex);
        }
        return outputTrips;
        
    }
    
    
    public static ArrayList<String> processTrip(ArrayList<Location2> eachTrip) throws ParseException{
    ArrayList<String> processedTrip = new ArrayList<String>();
    try{   

        // eachTrip represents a single trip
        
    	// select samples
    	// samplesFromOutputLocations.size() = 0 if tempOutputLocation is null or tempOutputLocation.size() == 0 
        ArrayList<Location2> samplesFromOutputLocations = locationManager.getSamplesFromOutputLocations(eachTrip);
        System.out.println("1");
        // get interesting locations' indices
        // interestingSampleIndicesFromOutputLocations.size() = 0 if samplesFromOutputLocations is null or samplesFromOutputLocations.size() == 0 
        ArrayList<Integer> interestingSampleIndicesFromOutputLocations  = surroundingDatabase.getInterestingSamplesIndex(samplesFromOutputLocations, surroundingTable);
        System.out.println("2");
        // get interesting locations intervals 
        // interestingIntervals.size() = 0 if interestingSampleIndicesFromOutputLocations is null or interestingSampleIndicesFromOutputLocations.size() == 0 
        ArrayList<Location2>  interestingIntervals = locationManager.getInterestingIntervals(interestingSampleIndicesFromOutputLocations, samplesFromOutputLocations, eachTrip);
        System.out.println("3");
	// store all weatherTags
        // weatherTags.size() = 0 if interestingIntervals is null or interestingIntervals.size() == 0 
        ArrayList<ArrayList<String>> weatherTags= weatherDatabase.getWeatherTag(interestingIntervals, weatherTable);	
        System.out.println("4");
        // System.out.println(weatherTags.toString());

        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        //III. Put Weather Tag and Mode together
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        // convert time to milliseconds for front end
        for(int i=0; i<weatherTags.size(); i++){
            String outputInfo = "";
            String tempTime = interestingIntervals.get(i).dateTime.toString();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date milliseconds =  dateFormat.parse(tempTime);
            outputInfo = interestingIntervals.get(i).latitude + "," + interestingIntervals.get(i).longitude + "," + String.valueOf(milliseconds.getTime()) + "," + interestingIntervals.get(i).mode + "," + weatherTags.get(i).toString();
            processedTrip.add(outputInfo);
            System.out.println(outputInfo);
        }
	    
    } catch (Exception ex){
        System.out.println("Exception: "+ex);
    }
    return processedTrip;
    }
}
    
