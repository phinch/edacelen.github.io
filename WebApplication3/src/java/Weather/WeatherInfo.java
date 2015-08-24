package Weather;

import java.util.ArrayList;

/**
 * Created by prateek on 3/4/15.
 */
public class WeatherInfo {

    public String windSpeed;
    public String temperature;
    public String precipitation;
    
    public WeatherInfo(){
    	windSpeed = "";
    	temperature = "";
    	precipitation = "";
    }

    public WeatherInfo(WeatherInfo weatherInfo) {
    	windSpeed = weatherInfo.windSpeed;
    	temperature = weatherInfo.temperature;
    	precipitation = weatherInfo.precipitation;
    }

	@Override
    public String toString(){
        return "Wind speed: " + windSpeed + " meter/second\nTemperature: " + temperature + "C\n" + "Precipitation is: " + precipitation;
    }
    

}
