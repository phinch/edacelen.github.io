/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.me.newEndpoint;

import Location2.Location2;
import MainApp.test;
import java.io.IOException;
import java.util.ArrayList;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;


import java.text.ParseException;
/**
 *
 * @author jiahuili
 */


@ServerEndpoint("/newEndpoint")
public class newEndpoint {
   
    /**
     * @OnOpen allows us to intercept the creation of a new session.
     * The session class allows us to send data to the client.
     * In the method onOpen, we'll let the client know that the handshake was 
     * successful.
     */
    @OnOpen
    public void onOpen(Session session){
        System.out.println(session.getId() + " has opened a connection"); 
        try {
            session.getBasicRemote().sendText("Connection Established");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
 
    
    /**
     * When client sends a message to the server, this method will intercept the message
     * and allow us to react to it. For now the message is read as a String.
     */
    @OnMessage
    public void onMessage(String message, Session session) throws ParseException{
        //System.out.println("Message from " + session.getId() + ": " + message);
        System.out.println("Message from " + session.getId());

        try {
            String[] input = message.split(";");    
            ArrayList<ArrayList<Location2>> trips = test.getTrips(input);
            System.out.println("there are in total" + trips.size() + "trips");
            for(ArrayList<Location2>trip : trips){
                // get a processed trip    
                System.out.println("records of this trip:" + trip.size());
                ArrayList<String> output = test.processTrip(trip);
                
                String processedTrip = "";
                int i = 0;
                for(i = 0; i < output.size() - 1; i++){
                    processedTrip += output.get(i);
                    processedTrip += ";";
                }
                System.out.println("number of important records: " + output.size());
                processedTrip += output.get(i);
                session.getBasicRemote().sendText(processedTrip);
                
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }          
     
    
    
    /**
     * The user closes the connection.
     * Note: you can't send messages to the client from this method
     */
    @OnClose
    public void onClose(Session session){
        System.out.println("Session " +session.getId()+" has ended");
    }
    
    

    
}
