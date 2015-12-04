package Audio;

import Location.IndoorsOutdoors;
import Location.Transport;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * Created by prateek on 4/7/15.
 *
 * An audio snap represents a time frame and the characteristics about it that will be included in the corresponding
 * audio clip
 *
 */
public class AudioSnap {

    Date start;
    Date end;
    AudioAttributes audioAttributes;

    /***
     *
     * Public constructor
     *
     * @param start Start date/time
     * @param end End date/time
     * @param audioAttributes the attributes for this audioSnap
     */
    public AudioSnap(Date start, Date end, AudioAttributes audioAttributes){

        this.start = start;
        this.end = end;
        this.audioAttributes = audioAttributes;

    }

    /***
     *
     * Public constructor
     *
     * @param start Start date/time
     * @param audioAttributes the attributes for this audioSnap
     */
    public AudioSnap(Date start, AudioAttributes audioAttributes){

        this.start = start;
        this.audioAttributes = audioAttributes;

    }

    public void setEnd(Date end){
        this.end = end;
    }

    @Override
    public String toString(){
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return  dateFormat.format(start.getTime()) + " to " + dateFormat.format(end.getTime()) + " ==> " + audioAttributes.transport + ", " + audioAttributes.indoorsOutdoors;
    }


    public String getName(){

        DateFormat dateFormat = new SimpleDateFormat("{HH-mm-ss}");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(start.getTime()) + " to " + dateFormat.format(end.getTime()) + "==> " + audioAttributes.transport +", "+ audioAttributes.indoorsOutdoors;

    }
}
