package Location2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

import Surrounding.SurroundingDatabase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.util.JSON;

/**
 * Created by Jiahui on 6/11/15.
 */
public class LocationManager2 {
	
	public ArrayList<Location2> inputLocation;
	public ArrayList<Location2> outputLocation;
	
	/**
	 * 
	 * @param fileName name of input file
	 * @return input data in ArrayList [Latitude, Longitude, dateTime]
	 */
	public ArrayList<Location2> generateLocationsFromFile(String[] inputFile){
        ArrayList<Location2> inputLocation = new ArrayList<Location2>();

        try {
            for(int i=0; i<inputFile.length; i++){
                if (inputFile[i] != null) {
                    Location2 location2 = new Location2();
                    String[] eachLine = inputFile[i].split(",");
                    eachLine[eachLine.length-1] = eachLine[eachLine.length-1].replace("\r\n","");

                    location2.latitude = eachLine[0];
                    location2.longitude = eachLine[1];
                    location2.dateTime = eachLine[2];

                    //System.out.println("l2 is: "+location2.toString());
                    inputLocation.add(location2);
                }
            }
        } catch(Exception ex){
            System.out.println("Exception: "+ex);
        }
            return inputLocation;
        }
	
	/**
	 * 
	 * @param inputLocation
	 * @return
	 */
	public ArrayList<ArrayList<Location2>> modePrediction(ArrayList<Location2> inputLocation){
		ArrayList<ArrayList<Location2>> outputLocation = new ArrayList<ArrayList<Location2>>();
		if (inputLocation == null || inputLocation.size() == 0){
			return outputLocation;
		}
		
		ArrayList<Location2> tempOutputLocation = new ArrayList<Location2>();
		// the final big map
		// HashMap<String, String> modeMap = new HashMap<String, String>();
		// store pre step "still" mode
		// ArrayList<ArrayList<String>> stillMode = new ArrayList<ArrayList<String>>();
					
		/****************************************************************************************************
		 *  a finalmodeMap to store mode prediction
		 *  ['start time'--'end time'](string) ---- mode
		 ****************************************************************************************************/
		
		// To store [time --> coordinates]
		HashMap<String, ArrayList<Double>> coorMap = new HashMap<String, ArrayList<Double>>();
		
		// To get time period, distance, speed, and acceleration
		double preSpeed = 0;
		double prePeriod = 0;
		double acceleration = 0;
		
		
		/*****************************************************************************************************
         *	use 3 basic mode predictions: still -- walk -- others
         *	put each preMode in preModeList
         * ***************************************************************************************************/  
		//To store preMode (3 kinds) in a big List        
		ArrayList<ArrayList<String>> preModeList = new ArrayList<ArrayList<String>>();	
		String pretime = "";
		String afttime = "";
		double period = 0;
		String coor00 = "";
		String coor01 = "";
		String coor10 = "";
		String coor11 = "";		
		double distance = 0;
		double speed = 0;
		int i = 0;
		int j = i+1;
		int index = 0;
		while(i<inputLocation.size()-1){		    			
			index += 1;
			// store coors in a map
			ArrayList<Double> coor = new ArrayList<Double>();
			coor.add(Double.parseDouble(inputLocation.get(j).latitude));
			coor.add(Double.parseDouble(inputLocation.get(j).longitude));
			coorMap.put(inputLocation.get(j).dateTime, coor);

			ArrayList<String> preMode = new ArrayList<String>();
			pretime = inputLocation.get(i).dateTime;
			afttime = inputLocation.get(j).dateTime;
			period = getPeriod(pretime, afttime);
		    coor00 = inputLocation.get(i).latitude;
		    coor01 = inputLocation.get(i).longitude;
		    coor10 = inputLocation.get(j).latitude;
		    coor11 = inputLocation.get(j).longitude;	
	    	distance = GetDistance(Double.parseDouble(coor00), Double.parseDouble(coor01), Double.parseDouble(coor10), Double.parseDouble(coor11));
	        speed = 0;
	        if (period != 0){
	        	 speed = distance/period;       
	        }
	        if (index > 1){
	        	if (period != 0){
	                acceleration = Math.abs((speed-preSpeed)/period);    
	        	}
	        }
	        preSpeed = speed;
            prePeriod = period;
            // if speed > 240 or acceleration > 4.5, ignore this node(outlier), else add [afttime, period, distance, speed, acceleration, mode, lat, lng]
            if(speed>240 || acceleration>4.5){
            	j++;
            }else{
            	preMode.add(String.valueOf(afttime));
                preMode.add(String.valueOf(period));
                preMode.add(String.valueOf(distance));
                preMode.add(String.valueOf(speed));
                preMode.add(String.valueOf(acceleration));    
                // predict form 3 choices: still -- walk -- others
                String preModeString = preMode(distance, acceleration, speed, period);
                preMode.add(preModeString);
                preMode.add(coor10); 
                preMode.add(coor11);            
                // add preMode to preModeList
                preModeList.add(preMode);
                j++;
                i=j-1; 
            }     
		}     
        
        /*****************************************************************************************************
         *	if consecutive still intervals is more 300 seconds, add "stillMerged" as a tag
         *	put "stillMerged" intervals and all other mode intervals into preSeg (ArrayList<ArrayList<String>>)
         * ***************************************************************************************************/  
        ArrayList<ArrayList<String>> preSeg = new ArrayList<ArrayList<String>>();
        double sum = 0;
        int begin = 0;
        int end = 0;
        i = 0;
        while(i<preModeList.size()){
        	if(preModeList.get(i).get(5) == "still"){
        		sum = 0;
	        	begin = i;
        		while(i<preModeList.size() && preModeList.get(i).get(5) == "still"){
	        		sum += Double.parseDouble(preModeList.get(i).get(1));
	        		i++;
	        	}
	        	end = i-1;
	        	if (sum >= 300){
	        		ArrayList<String> modeTemp = new ArrayList<String>();
	        		modeTemp.add(preModeList.get(begin).get(0)+"--"+preModeList.get(end).get(0));
	        		modeTemp.add(preModeList.get(begin).get(preModeList.get(begin).size()-2));
	        		modeTemp.add(preModeList.get(begin).get(preModeList.get(begin).size()-1));
	        		modeTemp.add("stillMerged");
	        		preSeg.add(modeTemp);	
	        	}else{
	        		for(int t = begin; t<=end; t++){
	        			ArrayList<String> modeTemp = new ArrayList<String>();
	        			modeTemp.add(preModeList.get(t).get(0)+"--"+preModeList.get(t).get(0));
	        			for(j=1; j<preModeList.get(t).size(); j++){
	        				modeTemp.add(preModeList.get(t).get(j));
	        			}
	        			preSeg.add(modeTemp);
	        		}
	        	}
	        	i--;
        	}else{
        		ArrayList<String> modeTemp = new ArrayList<String>();
        		modeTemp.add(preModeList.get(i).get(0)+"--"+preModeList.get(i).get(0));
    			for(j=1; j<preModeList.get(i).size(); j++){
    				modeTemp.add(preModeList.get(i).get(j));
    			}
        		preSeg.add(modeTemp);
        	}
        	i++;
        }
        /*for(int r=0; r<preSeg.size(); r++){
        	System.out.println(preSeg.get(r));
        }*/
        
        
        /**
         * **************************************************************************************************
         * Basic Mode Prediction
         * **************************************************************************************************
         * step 1: merge too small and too large intervals:
         * 		   set too small and too large intervals' mode the same as its previous one, and add a "merge" tag in the end
         * 		   too small: 
         * 					if this interval's mode is still, set its mode the same as its forward interval
         * 		   			if there is only one interval,  merge it into "modeMap" and set its mode as "still" 		
         * 		   too large:
         * 					if the speed is too large set its mode the same as its previous interval	   
         *		   
         * 
         * step 2: merge consecutive walk intervals
         * 		   if the time of the big interval is more than 5 min take it as a walk interval for certain
         * 		   
         * step 3: […..still…..] ----trip----[…..still…..]---- trip ----[…..still…..]…………
         * 		   for each trip:
         * 				[…..walk …..] ----segment----[…..walk …..]---- segment ----[…..walk...]………
         * 		  now each segment represents a single mode other than "still"
         * 				decide its mode with length, expectation of speed, acceleration (3 and get the average)
         * 
         * step 4: remove outliers
         */
        
        /**
         * Add a HashMap to analyze where the home is
         * put [coordinates(range), [appear times, "startTime--endTime","startTime--endTime"... ]] into HashMap stillLocationMap
         * coordinates should be in the range of 100m
         */
        HashMap<ArrayList<String>, ArrayList<String>> stillLocationMap = new HashMap<ArrayList<String>, ArrayList<String>>();
        
        //****************************************************
        //step1 -- merge too small intervals(still intervals)
        //****************************************************
        boolean flag = true;
        for (i=0; i<preSeg.size(); i++){
        	flag = true;
        	if(preSeg.get(i).size() == 4){
        		if(stillLocationMap.keySet().isEmpty()){
        			ArrayList<String> key = new ArrayList<String>();
        			ArrayList<String> value = new ArrayList<String>();
        			key.add(preSeg.get(i).get(1));
        			key.add(preSeg.get(i).get(2));
        			value.add("1");
        			value.add(preSeg.get(i).get(0));
        			stillLocationMap.put(key, value);
        		}else{
        			double tempDistance = 0;
        			for(ArrayList<String> temp: stillLocationMap.keySet()){
        				if(flag){
        					tempDistance = GetDistance(Double.parseDouble(temp.get(0)), Double.parseDouble(temp.get(1)), Double.parseDouble(preSeg.get(i).get(1)), Double.parseDouble(preSeg.get(i).get(2)));
            				if(tempDistance<=100){
            					flag = !flag;
                				ArrayList<String> value = stillLocationMap.get(temp);
                				value.set(0, String.valueOf(Integer.parseInt(value.get(0))+1));
                				value.add(preSeg.get(i).get(0));
                				break;
                			}
        				}
        			}
        			if(flag){
        				ArrayList<String> key = new ArrayList<String>();
            			ArrayList<String> value = new ArrayList<String>();
            			key.add(preSeg.get(i).get(1));
            			key.add(preSeg.get(i).get(2));
            			value.add("1");
            			value.add(preSeg.get(i).get(0));
            			stillLocationMap.put(key, value);
        			}
        		}
        	}
        	// size == 2 ---> time+"stillMerged"
        	if(preSeg.get(i).size() != 4){
        		if(preSeg.get(i).get(5)=="still"){
        			// if this interval's mode is "still", set the mode the same as previous mode, add "merged"
        			// if the previous node is not "stillMerged", set this mode the same as forward mode
        			// if the previous node is "stillMerged", the mode remains "still"
        			preSeg.get(i).add("merged");
        			if(i!=0 && preSeg.get(i-1).size() != 4){
        				preSeg.get(i).set(5, preSeg.get(i-1).get(5));
        			}
        		}
        	}
        }
        /*for(ArrayList<String> a: stillLocationMap.keySet()){
        	System.out.println(a);
        	System.out.println(stillLocationMap.get(a));
        }*/
        
        // decide which location is "home"
        // coorPeriod {[lat, lng] --> [period1(total periods in this location), period2(add up each period between start time of interval and 23:59)]}
        HashMap<ArrayList<String>, ArrayList<Double>> coorPeriod = new HashMap<ArrayList<String>, ArrayList<Double>>();
        for(ArrayList<String> a: stillLocationMap.keySet()){
        	if(!stillLocationMap.get(a).get(0).equals("1")){
        		ArrayList<Double> tempTotalPeriods = new ArrayList<Double>();
        		double totalPeriod1 = 0;
        		double totalPeriod2 = 0;
        		ArrayList<String> tempTimes = stillLocationMap.get(a);
        		for(int r=1; r<tempTimes.size(); r++){
        			String[] tempTimeIntervals = tempTimes.get(r).split("--");
        			totalPeriod1 += getPeriod(tempTimeIntervals[0], tempTimeIntervals[1]);
        			String tempDate[] = tempTimeIntervals[0].split(" ");
        			totalPeriod2 += getPeriod(tempTimeIntervals[0], tempDate[0]+" 23:59:59");
        		}
        		tempTotalPeriods.add(totalPeriod1);
        		tempTotalPeriods.add(totalPeriod2);
        		coorPeriod.put(a, tempTotalPeriods);
        	}
        }
        
        // let the one with longest totalPeriod be home
        double longestTempTotalPeriods = 0;
        ArrayList<String> home = new ArrayList<String>();
        for(ArrayList<String> a: coorPeriod.keySet()){
        	if(coorPeriod.get(a).get(0) > longestTempTotalPeriods){
        		longestTempTotalPeriods = coorPeriod.get(a).get(0);
        		home = a;
        	}
        }
        
        // get those home periods
        // home periods stored in homeIntervals
        ArrayList<String> tempHomeIntervals = stillLocationMap.get(home);
        ArrayList<String> homeIntervals = new ArrayList<String>();
        ArrayList<String> homeIntervals2 = new ArrayList<String>();
        for(int r=1; r<tempHomeIntervals.size(); r++){
        	String tempIntervals[] = tempHomeIntervals.get(r).split("--");
        	// if stay at home for more than 5 hours, set it as the end of a trip
        	if(getPeriod(tempIntervals[0], tempIntervals[1])>=18000){
        		homeIntervals.add(tempHomeIntervals.get(r));
        	}
        }
         System.out.println(home);
         System.out.println(homeIntervals);
        
        
        //***********************************************************************************
        // step 2
        // merge consecutive walk intervals -- if period >= 5min, -- merge them
        // if merged, mode is "walkMerged", [time, period, distance, speed, mode]
        // put all merged and unmerged into mergedSeg (ArrayList<ArrayList<String>>)
        //***********************************************************************************
        ArrayList<ArrayList<String>> mergedSeg = new ArrayList<ArrayList<String>>();
        double sumPeriod = 0;
        double sumDistance = 0;
        String beginTime = "";
        String endTime = "";
        begin = 0;
        end = 0;
        i=0;
        while (i<preSeg.size()){
        	if(preSeg.get(i).size() != 4){
        		if(preSeg.get(i).get(5) == "walk"){
        			begin = i;
        			sumPeriod = 0;
        			sumDistance = 0;
        			while(i<preSeg.size() && preSeg.get(i).size()!=4 && preSeg.get(i).get(5)=="walk"){
    					sumPeriod += Double.parseDouble(preSeg.get(i).get(1));
        				sumDistance += Double.parseDouble(preSeg.get(i).get(2));	        				
        				i++;       				
        			}
        			end = i-1;
        			i--;
	        		if(sumPeriod >= 300){
        				ArrayList<String> temp2 = new ArrayList<String>();
        				beginTime = preSeg.get(begin).get(0);
        				String[] s1 = beginTime.split("--");
        				endTime = preSeg.get(end).get(0);
        				String[] s2 = endTime.split("--");  
        				// [time, period, distance, speed, mode]
        				temp2.add(s1[0]+"--"+s2[1]);
        				temp2.add(String.valueOf(sumPeriod));
        				temp2.add(String.valueOf(sumDistance));
        				temp2.add(String.valueOf(sumDistance/sumPeriod));
        				temp2.add("walkMerged");
        				mergedSeg.add(temp2);
        			}else{
        				for (int k=begin; k<=end; k++){
        					mergedSeg.add(preSeg.get(k));
        				}
        			}
        		}else{
        			mergedSeg.add(preSeg.get(i));
        		}
        		
        	}else{
        		mergedSeg.add(preSeg.get(i));
        	}
        	i++;
        }
        /*for(int r=0; r<mergedSeg.size(); r++){
        	System.out.println(mergedSeg.get(r));
        }*/
        
        //***************************************************************************************************************
        // step 3 predict mode
        // ignore intervals with "merge" tag
        // get mean speed, 3 largest accelerations and get their mean？
        // put final mode predictions of merged and unmerged intervals into modePrediction (ArrayList<ArrayList<String>>)
        //***************************************************************************************************************
        ArrayList<ArrayList<String>> modePrediction = new ArrayList<ArrayList<String>>(); 
        double sumSpeed = 0;
        double meanSpeed = 0;
        ArrayList<Double> largestAcc = new ArrayList<Double>();
        String mode = "";
        i = 0;
        while(i<mergedSeg.size()){
        	if(mergedSeg.get(i).get(mergedSeg.get(i).size()-1) != "stillMerged" && (mergedSeg.get(i).get(mergedSeg.get(i).size()-1) != "walkMerged")){
        		begin = i;
        		largestAcc.clear();
        		sumSpeed = 0;
        		while(i<mergedSeg.size() && (mergedSeg.get(i).get(mergedSeg.get(i).size()-1) != "stillMerged" && (mergedSeg.get(i).get(mergedSeg.get(i).size()-1)) != "walkMerged")){
        			if(mergedSeg.get(i).get(mergedSeg.get(i).size()-1) != "merged"){
        				sumSpeed += Double.parseDouble(mergedSeg.get(i).get(3));
    					largestAcc.add(Double.parseDouble(mergedSeg.get(i).get(4)));
        			}
					i++;
        		}
        		i--;
        		end = i;
        		
        		// predict mode for "others":
        		ArrayList<String> temp2 = new ArrayList<String>();
    			meanSpeed = sumSpeed/(end-begin+1);
    			Collections.sort(largestAcc);
    	        Collections.reverse(largestAcc);
    	        double AccMean = 0;
    	        int largestAccSize = largestAcc.size();
    	        if(largestAccSize>=3){
    	        	AccMean = (largestAcc.get(0)+largestAcc.get(1)+largestAcc.get(2))/3;
    	        }else if (largestAccSize>=2){
    	        	AccMean = (largestAcc.get(0)+largestAcc.get(1))/3;
    	        }else{
    	        	AccMean = largestAcc.get(0);
    	        }
    			mode = transModePrediction(acceleration, meanSpeed);
    			beginTime = mergedSeg.get(begin).get(0);
				String[] s1 = beginTime.split("--");
				endTime = mergedSeg.get(end).get(0);
				String[] s2 = endTime.split("--");  
				temp2.add(s1[0]+"--"+s2[1]);
				temp2.add(mode);
				modePrediction.add(temp2);
        	}else{
        		ArrayList<String> temp = new ArrayList<String>();
        		temp.add(mergedSeg.get(i).get(0));
        		if (mergedSeg.get(i).get(mergedSeg.get(i).size()-1) == "stillMerged"){
        			temp.add("still");
        		}else{
        			temp.add("walk");
        		}
        		modePrediction.add(temp);
        	}
        	i++;
        }
        /*for(int r=0; r<modePrediction.size(); r++){
        	System.out.println(modePrediction.get(r));
        }*/ 
        
        //****************************************************
        //step 4: remove outliers and integrate intervals
        // assign every interval a single mode and remove outliers
        // preModeList stores all intervals
        // ArrayList<String> homeIntervals has time intervals at home
        //****************************************************
        String time0 = "";
        j=0;
        double diff = 0;
        for(i=0; i<preModeList.size(); i++){
        	time0 = preModeList.get(i).get(0);
        	if (j<modePrediction.size()){
        		String[] s = modePrediction.get(j).get(0).split("--");
        		diff = getPeriod(time0, s[1]);
        		if(diff<0){
            		j++;
            	}
        	}      	
        	
        	// ignore locations with abnormal speed according to its mode (i.e. in "vehicle", speed is 177m/s -- 2015-03-28 17:21:43)
        	// only care about "speed" of a single interval 
        	double tempSpeed = 0;
        	if (modePrediction.get(j).get(1) == "still"){
        		if (Double.parseDouble(preModeList.get(i).get(3))>1){
        			int start = 0;
        			if(i >= 1){
        				start = i-1;
        			}
        			tempSpeed  = GetDistance(coorMap.get(preModeList.get(start).get(0)).get(0), coorMap.get(preModeList.get(start).get(0)).get(1), coorMap.get(preModeList.get(i).get(0)).get(0), coorMap.get(preModeList.get(i).get(0)).get(1)) / getPeriod(preModeList.get(start).get(0),  preModeList.get(i).get(0)) ;
        			while(i<preModeList.size()-1 && tempSpeed>1){	        				
        				i++;
        				tempSpeed  = GetDistance(coorMap.get(preModeList.get(start).get(0)).get(0), coorMap.get(preModeList.get(start).get(0)).get(1), coorMap.get(preModeList.get(i).get(0)).get(0), coorMap.get(preModeList.get(i).get(0)).get(1)) / getPeriod(preModeList.get(start).get(0),  preModeList.get(i).get(0)) ;
        			}
        		}
        	}else if (modePrediction.get(j).get(1) == "vehicle"){
        		if (Double.parseDouble(preModeList.get(i).get(3))>80){
        			int start = 0;
        			if(i >= 1){
        				start = i-1;
        			}
        			tempSpeed  = GetDistance(coorMap.get(preModeList.get(start).get(0)).get(0), coorMap.get(preModeList.get(start).get(0)).get(1), coorMap.get(preModeList.get(i).get(0)).get(0), coorMap.get(preModeList.get(i).get(0)).get(1)) / getPeriod(preModeList.get(start).get(0),  preModeList.get(i).get(0)) ;
        			while(i<preModeList.size()-1 && tempSpeed>80){	        				
        				i++;
        				tempSpeed  = GetDistance(coorMap.get(preModeList.get(start).get(0)).get(0), coorMap.get(preModeList.get(start).get(0)).get(1), coorMap.get(preModeList.get(i).get(0)).get(0), coorMap.get(preModeList.get(i).get(0)).get(1)) / getPeriod(preModeList.get(start).get(0),  preModeList.get(i).get(0)) ;
        			}
        		}
        	}else if (modePrediction.get(j).get(1) == "run"){
        		if (Double.parseDouble(preModeList.get(i).get(3))>10){
        			int start = 0;
        			if(i >= 1){
        				start = i-1;
        			}
        			tempSpeed  = GetDistance(coorMap.get(preModeList.get(start).get(0)).get(0), coorMap.get(preModeList.get(start).get(0)).get(1), coorMap.get(preModeList.get(i).get(0)).get(0), coorMap.get(preModeList.get(i).get(0)).get(1)) / getPeriod(preModeList.get(start).get(0),  preModeList.get(i).get(0)) ;	        			
        			while(i<preModeList.size()-1 && tempSpeed>10){
        				i++;
        				tempSpeed  = GetDistance(coorMap.get(preModeList.get(start).get(0)).get(0), coorMap.get(preModeList.get(start).get(0)).get(1), coorMap.get(preModeList.get(i).get(0)).get(0), coorMap.get(preModeList.get(i).get(0)).get(1)) / getPeriod(preModeList.get(start).get(0),  preModeList.get(i).get(0)) ;
        			}
        		}
        	}else if (modePrediction.get(j).get(1) == "walk"){
        		if (Double.parseDouble(preModeList.get(i).get(3))>5){
        			int start = 0;
        			if(i >= 1){
        				start = i-1;
        			}
        			tempSpeed  = GetDistance(coorMap.get(preModeList.get(start).get(0)).get(0), coorMap.get(preModeList.get(start).get(0)).get(1), coorMap.get(preModeList.get(i).get(0)).get(0), coorMap.get(preModeList.get(i).get(0)).get(1)) / getPeriod(preModeList.get(start).get(0),  preModeList.get(i).get(0)) ;	        			
        			while(i<preModeList.size()-1 && tempSpeed>5){
        				i++;
        				tempSpeed  = GetDistance(coorMap.get(preModeList.get(start).get(0)).get(0), coorMap.get(preModeList.get(start).get(0)).get(1), coorMap.get(preModeList.get(i).get(0)).get(0), coorMap.get(preModeList.get(i).get(0)).get(1)) / getPeriod(preModeList.get(start).get(0),  preModeList.get(i).get(0)) ;
        			}
        		}
        	}else{
        		if(Double.parseDouble(preModeList.get(i).get(3))>270){
        			int start = 0;
        			if(i >= 1){
        				start = i-1;
        			}
        			tempSpeed  = GetDistance(coorMap.get(preModeList.get(start).get(0)).get(0), coorMap.get(preModeList.get(start).get(0)).get(1), coorMap.get(preModeList.get(i).get(0)).get(0), coorMap.get(preModeList.get(i).get(0)).get(1)) / getPeriod(preModeList.get(start).get(0),  preModeList.get(i).get(0)) ;
        			while(i<preModeList.size()-1 && tempSpeed>270){
        				i++;
        				tempSpeed  = GetDistance(coorMap.get(preModeList.get(start).get(0)).get(0), coorMap.get(preModeList.get(start).get(0)).get(1), coorMap.get(preModeList.get(i).get(0)).get(0), coorMap.get(preModeList.get(i).get(0)).get(1)) / getPeriod(preModeList.get(start).get(0),  preModeList.get(i).get(0)) ;
        			}
        		}
        	}      			
        	// [time, lat, log, mode] in 	Location2  eachOutputLocation 
        	Location2 eachOutputLocation = new Location2();
        	eachOutputLocation.latitude = String.valueOf(coorMap.get(preModeList.get(i).get(0)).get(0));
        	eachOutputLocation.longitude = String.valueOf(coorMap.get(preModeList.get(i).get(0)).get(1));
        	eachOutputLocation.dateTime = preModeList.get(i).get(0);
        	eachOutputLocation.mode = modePrediction.get(j).get(1);
        	/*if(!eachOutputLocation.mode.equals("still")){
        		System.out.println(eachOutputLocation);
        	}*/
        	tempOutputLocation.add(eachOutputLocation); 
        }
        /*
        for(int r=0; r<tempOutputLocation.size(); r++){
        	System.out.println(tempOutputLocation.get(r));
        }
        */
        
        // ignore "still" locations
        // integrate tempOutputLocation and homeIntervals
        ArrayList<Location2> tempResult = new ArrayList<Location2>();
        int indexOfHomeIntervals = 0; 
        for(int r=0; r<tempOutputLocation.size(); r++){
        	if(!tempOutputLocation.get(r).mode.equals("still")){
        		if(indexOfHomeIntervals < homeIntervals.size()){
        			tempResult = new ArrayList<Location2>();
        			String[] tempTimeIntervals = homeIntervals.get(indexOfHomeIntervals).split("--");
            		while(getPeriod(tempOutputLocation.get(r).dateTime, tempTimeIntervals[0])>=0){
            			if(!tempOutputLocation.get(r).mode.equals("still")){
            				tempResult.add(tempOutputLocation.get(r));
            			}
            			r++;
            		}
            		r--;
            		indexOfHomeIntervals++;
            		outputLocation.add(tempResult);
        		}else{
        			tempResult = new ArrayList<Location2>();
        			while(r<tempOutputLocation.size()){
        				if(!tempOutputLocation.get(r).mode.equals("still")){
        					tempResult.add(tempOutputLocation.get(r));
        					
        				}
        				r++;
        			}
        			outputLocation.add(tempResult);
        		}
        	}
        }        
        return  outputLocation;
	}        
		        
    /***
     * get samples from output locations
     * still: pick one point
     * walk & run: every 50 meter pick a point, search places in range of 100 meters
     * vehicle: every 250 meters pick a point, search places in range of 250 meters
     * plane: every 1000 meters pick a point, (picture from the satellite??? only start and end of flight)
     * @param outputLocation output locations
     * @return samplesFromOutputLocations samples from output locations
     */
	public ArrayList<Location2> getSamplesFromOutputLocations(ArrayList<Location2> outputLocation){
		ArrayList<Location2> samplesFromOutputLocations = new ArrayList<Location2>();
		if (outputLocation == null || outputLocation.size() == 0){
			return samplesFromOutputLocations;
		}
		int i=0;
		int size = outputLocation.size();
        double distance = 0;
        String mode = "";
        while(i<size-2){
        	if(outputLocation.get(i).mode.equals("still")){
        		Location2 eachSample = new Location2();
        		eachSample.latitude = outputLocation.get(i).latitude;
        		eachSample.longitude = outputLocation.get(i).longitude;
        		eachSample.dateTime = outputLocation.get(i).dateTime;
        		eachSample.mode = outputLocation.get(i).mode;
        		samplesFromOutputLocations.add(eachSample);
        		while(i<size-2 && outputLocation.get(i).mode.equals("still")){
        			i++;
        		}
        		i--;
        	}else{
        		if(outputLocation.get(i).mode.equals("walk")){
	        		distance = 50;
	        		mode = "walk";			        		
	        	}else if(outputLocation.get(i).mode.equals("run")){
	        		distance = 50;
	        		mode = "run";		        		
	        	}else if (outputLocation.get(i).mode.equals("vehicle")){
	        		distance = 250;
	        		mode = "vehicle";			        		
	        	}else if (outputLocation.get(i).mode.equals("plane")){
	        		distance = 1000;
	        		mode = "plane";
	        	}		        		
        		i = extractSamples(i, samplesFromOutputLocations, outputLocation, mode, distance);
        		i--;		        		
        	}		        	
        	i++;
        } 
        //add the last node
        Location2 eachSample = new Location2();
		eachSample.latitude = outputLocation.get(i+1).latitude;
		eachSample.longitude = outputLocation.get(i+1).longitude;
		eachSample.dateTime = outputLocation.get(i+1).dateTime;
		eachSample.mode = outputLocation.get(i+1).mode;
        return samplesFromOutputLocations;
	}
		
	/**
	 * 
	 * @param i index of outputLocation
	 * @param samplesOfSameMode output ArrayList<Location2> samplesOfSameMode
	 * @param outputLocation original outputLocations
	 * @param mode mode of a single interval
	 * @param distance distance between Node(i) & Node(i+1)
	 * @return index of outputLocation (next item in an iteration over outputLocation)
	 */
	public int extractSamples(int i, ArrayList<Location2> samplesOfSameMode, ArrayList<Location2> outputLocation, String mode, Double distance){
		int size = outputLocation.size();
        double dist = 0;
        Location2 eachSample = new Location2();  
        // add the first point of a new mode
		eachSample = new Location2();
		eachSample.latitude = outputLocation.get(i).latitude;
		eachSample.longitude = outputLocation.get(i).longitude;
		eachSample.dateTime = outputLocation.get(i).dateTime;
		eachSample.mode = outputLocation.get(i).mode;  
		samplesOfSameMode.add(eachSample);
		dist = 0;
		while(i<size-2 && outputLocation.get(i).mode.equals(mode)){		
			if(dist < distance){
				// distance between Pi & Pi+1
    			dist += GetDistance(Double.parseDouble(outputLocation.get(i).latitude), Double.parseDouble(outputLocation.get(i).longitude), Double.parseDouble(outputLocation.get(i+1).latitude), Double.parseDouble(outputLocation.get(i+1).longitude));		
    		}else{    			
        		if(outputLocation.get(i+1).mode.equals(mode)){
        			eachSample = new Location2();
        			eachSample.latitude = outputLocation.get(i+1).latitude;
        			eachSample.longitude = outputLocation.get(i+1).longitude;
        			eachSample.dateTime = outputLocation.get(i+1).dateTime;
        			eachSample.mode = outputLocation.get(i+1).mode;       			
        			samplesOfSameMode.add(eachSample);
        		}
        		dist = 0;	
    		}
			i++;
		}
		return i;		
	}
	
	
	/**
	 * get interesting intervals from interesting locations database and weather database
	 * @param interestingsamplesIndex
	 * @param surroundingDatabase surrounding Database
	 * @param outputLocation ouputted location (object Location2)
	 * @return all interesting intervals in order of time (object Location2)
	 * 1. get interesting samples' indices
	 * 2. get start and end Of intervals to be rewound
	 * 3. get all intervals to be rewound from outputlocations
	 * 
	 */
	public ArrayList<Location2> getInterestingIntervals(ArrayList<Integer> interestingsamplesIndex, ArrayList<Location2>samplesFromOutputLocations, ArrayList<Location2> outputLocation){	
		ArrayList<Location2> interestingIntervals =  new ArrayList<Location2>();
		// if no location in this trip is found in the database
		if (interestingsamplesIndex == null || interestingsamplesIndex.size() == 0){
			return interestingIntervals;
		}
	    ArrayList <String> startsAndEndsOfIntervals = this.getStartAndEndOfSubIntervels(interestingsamplesIndex, samplesFromOutputLocations);	    
	    interestingIntervals = getAllLocationsExtrated(startsAndEndsOfIntervals, outputLocation);   
		return interestingIntervals;
	}	
	 
	/**
	 * get interesting Samples from OutputLocation
	 * @param  samplesFromOutputLocations samplesFromOutputLocations
	 * @param interestingSampleIndicesFromOutputLocation reservedSampleIndicesFromOutputLocation
	 * @return interestingSamplesFromOutputLocations
	 */
	public ArrayList<Location2> getInterestingSamples(ArrayList<Location2> samplesFromOutputLocations, ArrayList<Integer> interestingSampleIndicesFromOutputLocation){
		ArrayList<Location2> interestingSamplesFromOutputLocations  = new ArrayList<Location2>();
		for(int i: interestingSampleIndicesFromOutputLocation){
			interestingSamplesFromOutputLocations.add(samplesFromOutputLocations.get(i));
		}
		return interestingSamplesFromOutputLocations;
	}
	
	
	/**
	 * get start location2 object and end location2 object of all sub intervals
	 * ^*^*^*^... ----> pick ^ to test, two adjacent * are bounds of effective intervals
	 * @param interestingsamplesIndex index of interesting samples in samplesFromOutputLocations
	 * @param samplesFromOutputLocations samplesFromOutputLocations
	 * @return start and end (location2 object) of all sub intervals
	 */
	public ArrayList<String> getStartAndEndOfSubIntervels(ArrayList<Integer>interestingsamplesIndex, ArrayList<Location2>samplesFromOutputLocations){
		ArrayList<String> intervalsExtrated = new ArrayList<String>();
	    for(int i: interestingsamplesIndex){
	    	// add start dateTime
	    	if(i == 0){
	    		intervalsExtrated.add(samplesFromOutputLocations.get(i).dateTime);    		
	    	}else{
	    		intervalsExtrated.add(samplesFromOutputLocations.get(i-1).dateTime);
	    	}
	    	// add end dateTime
	    	if(i+1 < samplesFromOutputLocations.size()){
				intervalsExtrated.add(samplesFromOutputLocations.get(i+1).dateTime);
			}else{
				intervalsExtrated.add(samplesFromOutputLocations.get(i).dateTime);
			}		
	    }	    
	    //System.out.println(intervalsExtrated.size());
		return intervalsExtrated;
	}
	   
	
	/**
	 * extract all Location2 objects from outputLocation
	 * @param intervalExtrated start and end (location2 object) of all sub intervals
	 * @param outputLocation all location2 objects in outputLocation
	 * @return all Location2 objects extrated
	 */
	public ArrayList<Location2> getAllLocationsExtrated(ArrayList<String>startsAndEndsOfIntervals, ArrayList<Location2> outputLocation){
		ArrayList <Location2> Extratedpoints = new ArrayList <Location2>();
	    int j=0;
	    String start = "";
	    String end = "";
		for(Location2 l2 : outputLocation){ 
			if(j < startsAndEndsOfIntervals.size()){
				start = startsAndEndsOfIntervals.get(j);
				end = startsAndEndsOfIntervals.get(j+1);
				if(LocationManager2.getPeriod(start,l2.dateTime)>=0){
					if(LocationManager2.getPeriod(l2.dateTime, end)>=0){
						Extratedpoints.add(l2);
					}else{
						j += 2;
					}
				}
			}
		}
		//System.out.println(Extratedpoints.size());
		return Extratedpoints;
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
   
	
	/**
	 * predict mode of an interval
	 * @param acceleration acceleration during an interval
	 * @param speed speed during an interval
	 * @return mode mode of an interval
	 */
	public static String transModePrediction(double acceleration, double speed){
		String mode = "";	                
        if (acceleration >= 3){
            mode = "plane";
        }else if (acceleration >= 0.6){ 
        	if(speed > 30){
        		mode = "plane";
        	}else{
        		mode = "vehicle";
        	}	
        }else{
        	if (speed > 40){
                mode = "plane";
            }else if (speed >= 3.7){
                mode = "vehicle";
            }else if (speed >= 2.5){
                mode = "run";
            }else{
                mode = "walk";
            }
        }
        return mode;
	}
	
	
	
	/**
	 * pre-processing:  3 predictions: walk -- still -- others 
	 * set loose thresholds for "still" and "walk"
	 * see methods from the following code
	 * @param distance distance during an interval
	 * @param acceleration acceleration during an interval
	 * @param speed speed during an interval
	 * @param period length of an interval
	 * @return preMode
	 */
	public static String preMode(double distance, double acceleration, double speed, double period){
		String mode = "";	                
        if (distance == 0 || (speed <= 0.2 && distance <20)){
        	mode = "still";
        }else if(speed<2 && acceleration<=0.4){
            mode = "walk";
        }else{
        	mode = "others";
        }
        return mode;
	}
	
	
	
	/**
	 * convert from degree to radian 
	 * @param d
	 * @return radian radian of a degree
	 */
	public static float rad(double d){
		return (float) (d * Math.PI / 180.0);
	}
	
	// To calculate distance between two coordinates
	public static double GetDistance(double lat1, double lng1, double lat2, double lng2){
		double EARTH_RADIUS = (double) 6378.137;
		double radLat1 = (double) Math.toRadians(lat1);
		double radLat2 = (double) Math.toRadians(lat2);
		double a = (double) (Math.toRadians(lat1) -Math.toRadians(lat2));
		double b = (double) (Math.toRadians(lng1) -Math.toRadians(lng2));
		double s = (double) (2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) + Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2))));
		s = s * EARTH_RADIUS;
	    return s*1000;
	}
}
