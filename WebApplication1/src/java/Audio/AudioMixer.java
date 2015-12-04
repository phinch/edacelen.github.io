package Audio;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

/**
 * Created by prateek on 3/18/15.
 */
public class AudioMixer {

    //public File


    public void mixSounds(File file1, File file2){

        String wavFile1 = file1.getAbsolutePath();
        String wavFile2 = file2.getAbsolutePath();

        try {
            AudioInputStream clip1 = AudioSystem.getAudioInputStream(new File(wavFile1));
            AudioInputStream clip2 = AudioSystem.getAudioInputStream(new File(wavFile2));

            Collection list=new ArrayList();
            list.add(clip1);
            list.add(clip2);

            //Calculate the smallest length
            long minFrameLength = Math.min(clip1.getFrameLength(), clip2.getFrameLength());

            //Mix the sounds
            AudioInputStream toReturn = new MixingAudioInputStream(clip1.getFormat(), list);

            //Set the length to the smaller one
            AudioInputStream toReturn2 = new AudioInputStream(toReturn, clip1.getFormat(), minFrameLength);

            //Write the File
            AudioSystem.write(toReturn2,
                    AudioFileFormat.Type.WAVE,
                    new File("/Users/prateek/Dropbox/spring-2015/Independent-Study/sounds/mix/"+file1.getName()+file2.getName()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public void mixSounds(ArrayList<File> files){

        StringBuilder name = new StringBuilder();
        for(File file: files) {
            name.append(file.getName());
        }
        try {

            ArrayList<AudioInputStream> list=new ArrayList();
            long smallestFrameLength = Long.MAX_VALUE;


            for(File file: files) {
                //Add clip to the list
                AudioInputStream clip = AudioSystem.getAudioInputStream(new File(file.getAbsolutePath()));
                list.add(clip);

                //Update smallest length
                if(clip.getFrameLength()<smallestFrameLength){
                    smallestFrameLength = clip.getFrameLength();
                }
            }

            //Mix the sounds
            AudioInputStream toReturn = new MixingAudioInputStream(list.get(0).getFormat(), list);

            //Truncate to the smallest length
            AudioInputStream toReturn2 = new AudioInputStream(toReturn, list.get(0).getFormat(), smallestFrameLength);

            AudioSystem.write(toReturn2,
                    AudioFileFormat.Type.WAVE,
                    new File("/Users/jiahui/Desktop/mixed sounds/"+name));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mixSounds(ArrayList<File> files, String fileName, int fileDuration, String destination){

        try {

            ArrayList<AudioInputStream> list=new ArrayList();
            long smallestFrameLength = Long.MAX_VALUE;

            for(File file: files) {
                //Add each clip to the list
                AudioInputStream clip = AudioSystem.getAudioInputStream(new File(file.getAbsolutePath()));
                list.add(clip);

                //Update the smallest length
                if(clip.getFrameLength()<smallestFrameLength){
                    smallestFrameLength = clip.getFrameLength();
                }
            }

            //Calculate framelength of smallestFile
            long frameLength = fileDuration * (long)list.get(0).getFormat().getFrameRate();

            //Mix the sounds
            AudioInputStream toReturn = new MixingAudioInputStream(list.get(0).getFormat(), list);

            //Truncate to the smallest length
            AudioInputStream toReturn2 = new AudioInputStream(toReturn, list.get(0).getFormat(), smallestFrameLength);

            //Calculate number of additions required for the right duration
            double temp = ( frameLength/smallestFrameLength) + 1 ;
            int numberOfFilesNeeded = (int)temp;

            //Write small file a temp location
            AudioSystem.write(toReturn2,
                    AudioFileFormat.Type.WAVE,
                    new File("/Users/jiahui/Desktop/mixed sounds/"+fileName+".wav"));

            //Append the same file over and over again until you have the desired length
            File tempFile = new File("/Users/jiahui/Desktop/mixed sounds/"+fileName+".wav");
            AudioInputStream audio1 = AudioSystem.getAudioInputStream(tempFile);
            AudioInputStream audio2 = AudioSystem.getAudioInputStream(tempFile);

            AudioInputStream audioBuild = new AudioInputStream(new SequenceInputStream(audio1, audio2), audio1.getFormat(), audio1.getFrameLength() + audio2.getFrameLength());
            for(int i = 0; i < numberOfFilesNeeded-2; i++)
            {
                audioBuild = new AudioInputStream(new SequenceInputStream(audioBuild, /* keep creating new input streams */ AudioSystem.getAudioInputStream(tempFile)), audioBuild.getFormat(), audioBuild.getFrameLength() + audio2.getFrameLength());
            }

            //Finally, write the file of the desired duration at the desired destination
            AudioInputStream finalStream = new AudioInputStream(audioBuild, audioBuild.getFormat(), frameLength);
            AudioSystem.write(finalStream, AudioFileFormat.Type.WAVE, new File(destination +"/" +fileName+".wav"));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
