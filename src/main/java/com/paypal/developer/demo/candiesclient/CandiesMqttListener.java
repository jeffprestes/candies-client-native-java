/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paypal.developer.demo.candiesclient;

import com.paypal.developer.jeffprestes.speaker.Speaker;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.dio.gpio.GPIOPin;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author jeffprestes
 */
public class CandiesMqttListener implements MqttCallback {

    private GPIOPin[] pins;
    private MqttAsyncClient client;
    private String queue;
    
    public CandiesMqttListener(GPIOPin[] parPins, MqttAsyncClient client, String queue)  {
        this.pins = parPins;
        this.client = client;
        this.queue = queue;
    }
    
    @Override
    public void connectionLost(Throwable th) {
        System.out.println("Connection was closed");
        th.printStackTrace();
        System.exit(0);
    }

    @Override
    public void messageArrived(String topic, MqttMessage mm)  {
        System.out.println("=Message arrived: " + mm.toString() + " at " + topic);
        
        if (mm.toString().equals("alive"))      {
            this.publish("yes");
            
        } else if (mm.toString().equals("release"))  {
            
            try {
                
                //TODO: Create methods to activate and deactivate pins
                pins[0].setValue(false);
                pins[1].setValue(false);
                System.out.println("--> GPIO state should be: ON");
            
                Thread.sleep(3500);
                
                // turn off gpio pin #01
                pins[0].setValue(true);
                pins[1].setValue(true);
                System.out.println("--> GPIO state should be: OFF");
                
                Speaker.speak("Thank you    thank you", "pt");
            
                this.publish("candies delivered");
                
            } catch (Exception ex) {
                System.err.println("The motor couldn't be actioned.");
                try { pins[0].close(); } catch (IOException exc)    {  exc.printStackTrace();       }
                try { pins[1].close(); } catch (IOException exc)    {  exc.printStackTrace();       }
                this.publish("The motor couldn't be actioned. Candies weren't delivered. Reason: " + ex.getLocalizedMessage());
                Logger.getLogger(CandiesMqttListener.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(0);
            } 
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
        
        if (imdt == null)     {
            System.out.println("DeliveryToke is null");
            return;
        }
        
        try {
            System.out.println("A message was deliveried into " + imdt.getTopics()[0]);
            
        } catch (NullPointerException npe)  {
            System.out.println("msg "+ npe.getMessage());
            System.out.println("loc "+ npe.getLocalizedMessage());
            System.out.println("cause "+ npe.getCause());
            System.out.println("excep "+ npe);
            npe.printStackTrace();
        }
    }
    
    
    public void publish(String msg)  {
        
        System.out.println("+Publishing message: "+msg);
        MqttMessage message = new MqttMessage(msg.getBytes());
        message.setQos(2);
        
        try {
            client.publish(queue, message);
            System.out.println("+Message published");  
            
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
}
