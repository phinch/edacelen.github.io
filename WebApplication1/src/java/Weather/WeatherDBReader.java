package Weather;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

import Location2.Location2;

public class WeatherDBReader {
    
	public ArrayList<WeatherInfo> getWeatherInfo(ArrayList<Location2> interestingIntervals, DBCollection weatherTable){
    	ArrayList<WeatherInfo> weatherInfoList = new ArrayList<WeatherInfo>();
	// get weatherInfo every 15 minutes
    	String begin = interestingIntervals.get(0).dateTime;
        
        for(int i = 0; i<interestingIntervals.size(); i++){
            double period = getPeriod(begin, interestingIntervals.get(i).dateTime);
            if(i == 0 || period >= 900){
                begin = interestingIntervals.get(i).dateTime;

                String sLng = interestingIntervals.get(i).longitude;	Double dLng = new Double(sLng);
                String sLat = interestingIntervals.get(i).latitude;	Double dLat = new Double(sLat);
                String time = interestingIntervals.get(i).dateTime;
                String dateTime = timeConvertion(time);

                double distance = 30/111.12;
                String sDistance = ""+distance;

                DBCursor cursor = weatherTable.find(new BasicDBObject("dateTime", dateTime).append("loc",JSON.parse("{$near : [ " + dLng + "," + dLat + "] , $maxDistance : " + sDistance + "}")));

                // result: try not to include "+9999" in "stringTemperature" and "9999" in "stringSpeed" and "null" in "precipitation"
                BasicDBObject result = new BasicDBObject();
                BasicDBObject tempBasicDBObject = new BasicDBObject();
                while(cursor.hasNext()){
                    tempBasicDBObject = (BasicDBObject) cursor.next();
                    if(!tempBasicDBObject.get("stringTemperature").equals("+9999") && !tempBasicDBObject.get("stringSpeed").equals("9999")){
                        result = tempBasicDBObject;
                    }
                    if(!tempBasicDBObject.get("precipitation").equals("null")){
                        result = tempBasicDBObject;
                        break;
                    }
                }
                // if no match is found, reuslt is empty
                if(result.isEmpty()){
                    result = tempBasicDBObject;
                }
                // System.out.println(result);
                // if result is empty, w has default value "", "", "" for windSpeed, temperature, and precipitation
                WeatherInfo w = new WeatherInfo();
                if(!result.isEmpty()){
                    System.out.println("result is not empty");
                    String temp = result.get("stringTemperature").toString();
                    
                    // get temperature
                    if(!temp.equals("9999")){
                        w.temperature = getTemperatureFromRecord(result.get("stringTemperature").toString());
                    }else{
                            w.temperature = "";
                    }
                    // get wind
                    String wind = result.get("stringSpeed").toString();
                    if(!wind.equals("9999")){
                        w.windSpeed = getWindSpeedFromRecord(result.get("stringSpeed").toString());
                    }else{
                            w.windSpeed = "";
                    }
                    // get precipitation
                    String prec = result.get("precipitation").toString();
                    
                    if(!prec.equals("null")){
                            w.precipitation = getPrecipitationFromRecord(result.get("precipitation").toString());
                    }else{
                            w.precipitation = "";
                    }
                }
                weatherInfoList.add(w);
            } else {
                // if this moment is in the range of 15 minutes from last checked moment, let it be the same as last checked weather info
                weatherInfoList.add(new WeatherInfo(weatherInfoList.get(weatherInfoList.size()-1)));
            }
        }
        return weatherInfoList;
    }
	
	
    public String timeConvertion(String time){
    	String result = "";
    	String time1[] = time.split(" ");
    	String time2[] = time1[0].split("-");
    	String time3[] = time1[1].split(":");
    	
    	for(String s: time2){
    		if(s.length()==1){
    			result = result+"0"+s;
    		}else{
    			result += s;
    		}
    	}
    	result = result+time3[0];
    	
    	return result;
    }
    
    

    /**
    *
    * Get wind speed from a line
    *
    * @param line input line
    * @return wind speed
    */
   public String getWindSpeedFromRecord(String input){
	   if(input != ""){
		   float result = 0f;
	       result = Float.parseFloat(input);
	       return String.valueOf(result/10f);
	   }
	   return "";
   }

   /**
    *
    * Get temperature from a line
    *
    * @param line input line
    * @return temperature
    */
   public String getTemperatureFromRecord(String input){
	   if(input != ""){
		   float result = 0f;
		   result = Float.parseFloat(input);
		   return String.valueOf(result/10);
	   }
	   return "";
   }

   /**
    * rain: 
    * @param line
    * @return precipitation
    */
   public String getPrecipitationFromRecord(String input){
	   if(!input.equals("null")){
		   return input;
		   }
	   return "";
   }
   

   public WeatherInfo extractWeatherInformation(String line){
   	WeatherInfo weatherInfo = new WeatherInfo();
   		
       weatherInfo.windSpeed = getWindSpeedFromRecord(line);
       weatherInfo.temperature = getTemperatureFromRecord(line);
       weatherInfo.precipitation= getPrecipitationFromRecord(line);
       //System.out.println(weatherInfo.precipitation);
       return weatherInfo;
   	
   }
   
   public ArrayList<String> getEachWeatherTag(WeatherInfo weatherInfo){
   	ArrayList<String> eachWeatherTag = new ArrayList<String>();
   	String windSpeed = "";
 	String precipitation = "";
 	
 	
   	try{
   		if(weatherInfo != null){
           	// decide wind (3 levels)
   			if(!weatherInfo.windSpeed.equals("")){
   				if(Float.parseFloat(weatherInfo.windSpeed) <= 2.3){
   	           		windSpeed = "no wind";
   	           	}else if(Float.parseFloat(weatherInfo.windSpeed) <= 6.7){
   	           		windSpeed = "lightBreeze";
   	           	}else if(Float.parseFloat(weatherInfo.windSpeed) <= 13.5){
   	           		windSpeed = "gentleBreeze";
   	           	}else{
   	           		windSpeed = "wind";
   	           	}
   			}
           	eachWeatherTag.add(windSpeed);
          
           	if(!weatherInfo.precipitation.equals("")){
           		int amount = Integer.parseInt(weatherInfo.precipitation);	
           		// if weatherInfo lack of temperature info, treat it as > 0
           		if(!weatherInfo.temperature.equals("")){
           			double temperature = Double.parseDouble(weatherInfo.temperature);
           			if(temperature<0){
           				if(amount>40){
           					precipitation = "heavy snow";
           				}else if(amount>20){
           					precipitation = "medium snow";
           				}else{
           					precipitation = "little snow";
           				}
           			}
           		}
           		if(amount>40){
       				precipitation = "heavy rain";
       			}else if(amount>20){
       				precipitation = "medium rain";
       			}else{
       				precipitation = "little rain";
       			}
   			}
           	eachWeatherTag.add(precipitation);
   		}else{
   			eachWeatherTag.add("");
   		}
   	} catch (Exception ex){
   		ex.printStackTrace();
   	}	
   	return eachWeatherTag;
   }
    

   public ArrayList<ArrayList<String>> weatherInfoToTag(ArrayList<WeatherInfo> weatherInfoList){
	 ArrayList<ArrayList<String>> weatherTag = new ArrayList<ArrayList<String>>();
	 // tag1 can be "" "" ""
   	 ArrayList<String> tag1 =  getEachWeatherTag(weatherInfoList.get(0));
   	    weatherTag.add(tag1);
   	    // merge weather tag: if the current tag is "" "" "", set the value the same as last tag
   	    for(int i=1; i<weatherInfoList.size(); i++){
   	    	ArrayList<String> eachWeatherTag = new ArrayList<String>();
   	    	if(weatherInfoList.get(i).windSpeed.equals("") && weatherInfoList.get(i).precipitation.equals("") && weatherInfoList.get(i).temperature.equals("")){
   	    		eachWeatherTag = getEachWeatherTag(weatherInfoList.get(i-1));
   	    		// weatherInfoList.set(i, weatherInfoList.get(i-1));
   	    	}else{
   	    		eachWeatherTag = getEachWeatherTag(weatherInfoList.get(i));
   	    	}
   	        weatherTag.add(eachWeatherTag);
   	    }
   	   return weatherTag;
   }
   

	/**
	 * get length of an interval in seconds
	 * @param pretime DateTime of start of interval
	 * @param afttime DateTime of end of interval
	 * @return length length of this interval in seconds
	 */
   public static double getPeriod(String pretime, String afttime){
   	double length = 0;
   	double diff = 0;
   	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    Date d1 = null;
       Date d2 = null;
       try {
           d1 = format.parse(pretime);
           d2 = format.parse(afttime);
           //ms
           diff = d2.getTime() - d1.getTime();
           //System.out.println(diff / 1000);	//seconds

       } catch (Exception e) {
           e.printStackTrace();
       }
       length = diff/1000;      //time in seconds
       return length;
   }
   
	
}
