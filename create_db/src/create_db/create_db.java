package create_db;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

public class create_db {

	public static void main(String[] args){
		
		Mongo mongo = null;
		DB db = null;
		DBCollection table = null;
		
		//*******************************************************************************
		//------------------------connectin to the MongoDB-Server------------------------
		//*******************************************************************************
		try{
			mongo = new Mongo("localhost", 27017);
		} catch (Exception e){
			// TODO ...
			e.printStackTrace();
		}
		
		/**===============================================================================
		 * --------------------------------Surroundings-----------------------------------
		 *================================================================================*/
		
		
		//*******************************************************************************
		//------------------------------get the connections------------------------------
		//--save "surroundingsExtraced in db: Rewind_Surroudings, Collection: location"-
		//*******************************************************************************
		db = mongo.getDB("Rewind_Surroudings");
		System.out.println(db.collectionExists("Rewind_Surroudings"));
		System.out.println(db.getCollectionNames());
		table = db.getCollection("location");
		System.out.println(table.getCount());
		//table.drop();
		
		
		//*******************************************************************************
		//-------------------insert data(location)/ read from file-----------------------
		//*******************************************************************************
		BufferedReader br = null;
		BasicDBObject doc = new BasicDBObject();
		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader("/opt/dbfiles/location.txt"));

			while ((sCurrentLine = br.readLine()) != null) {
				String[] s = sCurrentLine.split(" ");
				// 0-lng, 1-lat, 2-id
				doc = new BasicDBObject("loc", new BasicDBObject("lng", Double.parseDouble(s[0])).append("lat", Double.parseDouble(s[1])))
		        .append("geonameid", s[2]);
				table.insert(doc);
				//System.out.println(doc);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		
		//*******************************************************************************
		//---------------------------create geospatial index-----------------------------
		//*******************************************************************************
		table.ensureIndex(new BasicDBObject("loc", "2d"));
		
		
		
		//*******************************************************************************
		//-----------------------------------do queries----------------------------------
		//*******************************************************************************
/*		String sLng = "44.7261165";	Double dLng = new Double(sLng);
		String sLat = "-71.4362007";	Double dLat = new Double(sLat);
		double distance = 1/111.12;
		String sDistance = ""+distance;
		DBCursor cursor = table.find(new BasicDBObject("loc",JSON.parse("{$near : [ " + dLng + "," + dLat + "] , $maxDistance : " + sDistance + "}"))).limit(10);
		
		while (cursor.hasNext()) {
	        System.out.println(cursor.next());
	    }
*/		
		
		
		/**===============================================================================
		 * ------------------------------------Weather------------------------------------
		 *==============================================================================*/
		
		
		//*******************************************************************************
		//------------------------------get the connections------------------------------
		//--save "Weather" in db: Rewind_Weather_us, Collection: weather_us"-
		//--save "Weather"(only weather data in US) in db: Rewind_Weather_us, Collection: weather_us"-
		//*******************************************************************************
/*		db = mongo.getDB("Rewind_Weather_us");
		System.out.println(db.collectionExists("Rewind_Weather_us"));
		System.out.println(db.getCollectionNames());
		table = db.getCollection("weather_us");
		System.out.println(table.getCount());
*/		
		
		//*******************************************************************************
		//-------------------insert data(location)/ read from file-----------------------
		//*******************************************************************************
/*		BufferedReader br = null;
		BasicDBObject doc = new BasicDBObject();
		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader("/Users/jiahui/Desktop/mongodb/USWeather.txt"));

			while ((sCurrentLine = br.readLine()) != null) {
				String[] s = sCurrentLine.split(" ");
				// 0-lng, 1-lat, 2-dateTime, 3-stringTemperature, 4-stringSpeed, 5-precipitation
				doc = new BasicDBObject("loc", new BasicDBObject("lng", Double.valueOf(s[0])).append("lat", Double.valueOf(s[1])))
		        .append("dateTime", s[2])
				.append("stringTemperature", s[3])
				.append("stringSpeed", s[4])
				.append("precipitation", s[5]);
				table.insert(doc);
				//System.out.println(doc);
				
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
*/		
		
		
		//*******************************************************************************
		//---------------------------create geospatial index-----------------------------
		//*******************************************************************************	
//		table.dropIndexes();
//		table.ensureIndex(new BasicDBObject("loc", "2d").append("precipitation", 1).append("dateTime", 1));


		
		
		//*******************************************************************************
		//-----------------------------------do queries----------------------------------
		//*******************************************************************************
		// do query
		// weather
		//db.location.find({geonameid: "7866203", loc: {$near:[4.35,29.98333]}}).limit(2)	
		
		/*
		 * -77.4415837    39.4151122
			NO
			-77.4687986    39.3385564
			NO
			-77.3823807    38.9526083
			NO
			-77.3550957    38.9518674
			NO
			-77.323381    38.9446366
		 */
		
/*		
		String sLng = "-77.0475306";	Double dLng = new Double(sLng);
		String sLat = "38.896329";		Double dLat = new Double(sLat);
		double distance = 20/111.12;
		String sDistance = ""+distance;
		// time format: YYYYMMDDhh
		//DBCursor cursor = table.find(new BasicDBObject("dateTime", "2015012015").append("precipitation", new BasicDBObject("$ne","null")).append("loc",JSON.parse("{$near : [ " + dLng + "," + dLat + "] , $maxDistance : " + sDistance + "}")));
		DBCursor cursor = table.find(new BasicDBObject("dateTime", "2015032713").append("loc",JSON.parse("{$near : [ " + dLng + "," + dLat + "] , $maxDistance : " + sDistance + "}")));
		BasicDBObject d = new BasicDBObject();
		while(cursor.hasNext()){
			d = (BasicDBObject) cursor.next();
			if(!d.get("precipitation").equals("null")){
				break;
			}
			//System.out.println(cursor.next());
		}
		System.out.println(d);
*/		
		
/*		while(cursor.hasNext()){
			//System.out.println("YES!");
			DBObject r = cursor.next();
	    	if(!cursor.next().get("precipitation").equals("null")){
	    		r = cursor.next();
	    	}
	    	System.out.println(cursor.next());
		}
		
*/		
		
		
	}
		
		
	
	
}
