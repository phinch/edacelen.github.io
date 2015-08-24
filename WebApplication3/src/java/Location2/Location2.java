package Location2;

/**
 * Created by Jiahui on 6/11/15.
 */
public class Location2 {
	public String latitude;
	public String longitude;
	public String dateTime;
	public String mode;
	public String weather;
	
	public String toString(){
		String string = "";
		string += this.latitude;
		string += ",";
		string += this.longitude;
		string += ",";
		string += this.dateTime;
		string += ",";
		string += this.mode;
		return string;
	}
}
