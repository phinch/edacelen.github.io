package Weather;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

import Location2.Location2;

/**
 * Created by prateek on 3/4/15.
 */
public class WeatherDatabase {
    WeatherDBReader WDBReader; //Integrated Surface Data Reader
    /**
     * Constructing a database from a folder
     * @param folder
     */
    public WeatherDatabase(){
    	WDBReader = new WeatherDBReader();
    }
    
    /**
     * get weather info every 15 minutes
     * @param ReservedIntervals
     * @return
     */
    public ArrayList<ArrayList<String>> getWeatherTag(ArrayList<Location2> ReservedIntervals){
    	ArrayList<ArrayList<String>> weatherTags = new ArrayList<ArrayList<String>>();
    	// get weatherInfo <WeatherInfo>
    	ArrayList<WeatherInfo> temp = WDBReader.getWeatherInfo(ReservedIntervals);
    	// get weatherTags <String>
    	weatherTags = WDBReader.weatherInfoToTag(temp);
    	return weatherTags;
    }
	
    
    
}
