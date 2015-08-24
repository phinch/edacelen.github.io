package MainApp;

import Location2.Location2;
import Location2.LocationManager2;
import Surrounding.SurroundingDatabase;
import Weather.WeatherDatabase;
import java.io.File;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;



/**
 * Created by Jiahui on 06/10/15.
 */
public class test {
    
    public static String add(String[] inputFile){
        String s = "it doesn't work!!!!!";
        System.out.println(s);
        return s;
        
    }
    
    public static ArrayList<ArrayList<Location2>> getTrips(String[] inputFile){
        LocationManager2 locationManager = new LocationManager2();
        ArrayList<ArrayList<Location2>> outputLocation = new ArrayList<ArrayList<Location2>>();
       
        try{
            //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
            //I. Location Manager
            //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=- =-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

            System.out.println("I. Location Manager");
            ArrayList<Location2> inputLocation = locationManager.generateInputLocatiossFromFile(inputFile);

            // output has mode for each coordinates, except for "still" ones
            outputLocation = locationManager.modePrediction(inputLocation);
             /*for(ArrayList<Location2> l22: outputLocation){
                for(Location2 l2: l22){
                        System.out.println(l2);
                }
                System.out.println();
                System.out.println();
                System.out.println();
            }*/
            
        }catch (Exception ex){
            System.out.println("Exception: "+ex);
        }
        return outputLocation;
        
    }
    
    public static ArrayList<String> processTrip(ArrayList<Location2> tempOutputLocation) throws ParseException{
	LocationManager2 locationManager = new LocationManager2();
        
        ArrayList<String> temp = new ArrayList<String>();
        
        try{
            //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
            //II. Select and Filter Sample coordinates
            //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
            System.out.println("II. Select and Filter Sample coordinates");

            SurroundingDatabase surroundingDatabase = new SurroundingDatabase();

             // select samples
            ArrayList<Location2> samplesFromOutputLocations = locationManager.getSamplesFromOutputLocations(tempOutputLocation);

            // get reserved samples' indices
            ArrayList<Integer> reservedSampleIndicesFromOutputLocations  = surroundingDatabase.getReservedSamplesIndex(samplesFromOutputLocations);

            // get final reserved intervals 
            ArrayList<Location2>  ReservedIntervals = locationManager.getReservedIntervals(reservedSampleIndicesFromOutputLocations, samplesFromOutputLocations, tempOutputLocation);
            /*for(Location2 l22: ReservedIntervals){
                System.out.println(l22);
            }*/


            //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
            //III. Find the weather file with information about the weather
            //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
            System.out.println("III. Find the weather file with information about the weather");
            WeatherDatabase db = new WeatherDatabase();
            /**
            * test data:
            * Brown University, 89 Waterman Street, Providence, RI 02912, USA
            * Latitude: 41.826747 | Longitude: -71.401638    
            */

            // to store all weatherTags
            ArrayList<ArrayList<String>> weatherTags= db.getWeatherTag(ReservedIntervals);	
            /*for(ArrayList<String> l22: weatherTags){
                System.out.println(l22);
            }*/
            //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!");

            //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
            //IV. Put Weather Tag and Mode together
            //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

            System.out.println("IV. Put Weather Tag and Mode together");
            // convert time to milliseconds
            for(int i=0; i<weatherTags.size(); i++){
                String outputInfo = "";
                String tempTime = ReservedIntervals.get(i).dateTime.toString();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Date milliseconds =  dateFormat.parse(tempTime);

                outputInfo = ReservedIntervals.get(i).latitude+","+ReservedIntervals.get(i).longitude+","+String.valueOf(milliseconds.getTime())+","+ReservedIntervals.get(i).mode+","+weatherTags.get(i).toString();
                temp.add(outputInfo);
                System.out.println(outputInfo);
            }
            
        }catch (Exception ex){
            System.out.println("Exception: "+ex);
        }
    
        return temp;
    }
    
}
