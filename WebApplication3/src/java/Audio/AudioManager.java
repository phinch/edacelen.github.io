package Audio;

import Location.IndoorsOutdoors;
import Location.Transport;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by prateek on 3/18/15.
 */
public class AudioManager {

    AudioMixer audioMixer = new AudioMixer();

    /**
     *
     * Uses Audiomixer to mix any two sounds and generate a new mix
     *
     * @param file1 Sound 1
     * @param file2 Sound 2
     */
    public void mixSounds(File file1, File file2){

        audioMixer.mixSounds(file1, file2);

    }

    /**
     *
     * Uses AudioMixer to mix a list of sounds and generate a new mix
     *
     * @param files
     */
    public void mixSounds(ArrayList<File> files){

        audioMixer.mixSounds(files);

    }


    /**
     *
     * Uses AudioMixer to mix a list of sounds and generate a new mix with a given name and a duration
     *
     * @param files
     */
    public void mixSounds(ArrayList<File> files, String name, int duration, String destination){

        audioMixer.mixSounds(files, name, duration, destination);

    }

    /**
     *
     * Given audiosnaps (as a result of analysis of locations), it creates a collection of actual audio files in a
     * folder
     *
     * @param snaps
     */
    public void exportNewAudioCollection(ArrayList<AudioSnap> snaps, String homeDirectory){

        //Create a folder called Project-# where # is the number of your project
        String destination = homeDirectory + "/Project-"+ (existingNumberOfProjects(homeDirectory) + 1);
        new File(destination).mkdir();

        //Put an audio file corresponding to each snap
        for(AudioSnap snap: snaps){
            convertAudioSnapToAudio(snap, destination);
        }

    }

    /**
     *
     * Calculate the number of existing projects in the destination so the new project can be named with the correct
     * project number
     *
     * @param destination Destination to be checked
     *
     */
    public int existingNumberOfProjects(String destination){

        File folder = new File(destination);
        int toReturn = 0;

        for(File file: folder.listFiles()){
            if(file.isDirectory()){
                toReturn++;
            }
        }

        return toReturn;
    }

    public void convertAudioSnapToAudio(AudioSnap audioSnap, String destination){

        //Get audio files that go with an audio snap
        ArrayList<File> files = attributeToFiles(audioSnap.audioAttributes);

        //Get Name
        String name = audioSnap.getName();

        //Get duration
        int duration = (int) (audioSnap.end.getTime() - audioSnap.start.getTime())/1000;

        mixSounds(files, name, duration, destination);

    }



    public ArrayList<File> attributeToFiles(AudioAttributes attribute){

        ArrayList<File> toReturn = new ArrayList<File>();

        //--> Transport:
        //Option 1: Running
        if(attribute.transport == Transport.Running){
            toReturn.add(new File("/Users/prateek/Dropbox/spring-2015/Independent-Study/sounds/mode of transport/running/running-on-concrete.wav"));
            toReturn.add(new File("/Users/prateek/Dropbox/spring-2015/Independent-Study/sounds/mode of transport/running/breathing-running.wav"));
        }
        //Option 2: Walking
        else if (attribute.transport == Transport.Walking){
            if(attribute.indoorsOutdoors == IndoorsOutdoors.Outdoors){
                toReturn.add(new File("/Users/prateek/Dropbox/spring-2015/Independent-Study/sounds/mode of transport/walking/walking-on-stones.wav"));
            } else{
                toReturn.add(new File("/Users/prateek/Dropbox/spring-2015/Independent-Study/sounds/mode of transport/walking/walking-inside.wav"));
            }

        }
        //Option 3: Driving
        else if(attribute.transport == Transport.Driving){
            toReturn.add(new File("/Users/prateek/Dropbox/spring-2015/Independent-Study/sounds/mode of transport/driving/cars-and-traffic-lights.wav"));
        }

        //--> Indoors/Outdoors:
        //Option 1:
        if(attribute.indoorsOutdoors == IndoorsOutdoors.Indoors){
           toReturn.add(new File("/Users/prateek/Dropbox/spring-2015/Independent-Study/sounds/surround/indoors/indoors.wav"));
        }

        return toReturn;

    }
}
