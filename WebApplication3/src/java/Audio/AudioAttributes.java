package Audio;

import Location.IndoorsOutdoors;
import Location.Transport;

/**
 * Created by prateek on 4/7/15.
 */
public class AudioAttributes {

    IndoorsOutdoors indoorsOutdoors;
    Transport transport;

    /***
     *
     * Constructor for AudioAttributes
     *
     * @param indoorsOutdoors IndoorsOutdoors
     * @param transport Transport
     */
    public AudioAttributes(IndoorsOutdoors indoorsOutdoors, Transport transport){

        this.indoorsOutdoors = indoorsOutdoors;
        this.transport = transport;
    }

}
