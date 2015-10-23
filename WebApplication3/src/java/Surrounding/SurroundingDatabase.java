package Surrounding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

import Location2.Location2;


/**
 * Created by Jiahui on 6/11/15.
 */

public class SurroundingDatabase {
	public SurroundingFileReader SurReader;
    //Float matchScale = 0.5f;
	
    /**
     * Constructing a database from a folder
     * @param folder
     */
    public SurroundingDatabase(){
        this.SurReader = new SurroundingFileReader();
    }
  
    /**
     * initialize SorroundingDatabase
     * @return table
     */
    public DBCollection initialize(){
    	Mongo mongo = null;
        DB db = null;
        DBCollection table = null;		
        try{
            mongo = new Mongo("localhost", 27017);
        } catch (Exception e){
            // TODO ...
            e.printStackTrace();
        }		
        db = mongo.getDB("Rewind_Surroudings");
        table = db.getCollection("location");
        return table;
    }
    
    /**
     * if a single sample has one or more specific surrounding object (in the surrounding database), keep this sample
	 * pick one single sample to test every two samples (^*^*^*^ ----> pick ^ to test, two adjacent * are bounds of effective intervals)
     * @param samplesFromOutputLocations
     * @return index of interesting samples
     */
	public ArrayList<Integer> getInterestingSamplesIndex(ArrayList<Location2> samplesFromOutputLocations, DBCollection table ){
            ArrayList<Integer> interestingSamplesIndex = new ArrayList<Integer>();
            if (samplesFromOutputLocations == null || samplesFromOutputLocations.size() == 0) {
                return interestingSamplesIndex;
            }
            for(int i = 0; i < samplesFromOutputLocations.size(); i += 2){			
                String sLng = samplesFromOutputLocations.get(i).longitude;	Double dLng = new Double(sLng);
                String sLat = samplesFromOutputLocations.get(i).latitude;	Double dLat = new Double(sLat);			
                double distance = 0;
                //System.out.println(samplesFromOutputLocations.get(i).mode);
                if(!samplesFromOutputLocations.get(i).mode.equals("still")){
                    if(samplesFromOutputLocations.get(i).mode.equals("walk") || samplesFromOutputLocations.get(i).mode.equals("run")){
                        distance = 0.1/111.12;
                    }else if(samplesFromOutputLocations.get(i).mode.equals("vehicle")){
                        distance = 0.5/111.12;
                    }else {
                        distance = 2/111.12;
                    }				
                    String sDistance = ""+distance;				
                    DBCursor cursor = table.find(new BasicDBObject("loc",JSON.parse("{$near : [ " + dLng + "," + dLat + "] , $maxDistance : " + sDistance + "}")));				
                    // has result
                    if (cursor.hasNext()) {
                        interestingSamplesIndex.add(i);
                    }				
                }
            }
            return interestingSamplesIndex;
	}

}
