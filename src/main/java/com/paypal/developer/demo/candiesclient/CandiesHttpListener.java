/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paypal.developer.demo.candiesclient;

import com.paypal.developer.jeffprestes.speaker.Speaker;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.dio.gpio.GPIOPin;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

/**
 * Listen a queue managed by Candies PayPal PHP 
 * @author jprestes
 */
public class CandiesHttpListener {
    
    private String queue;
    private GPIOPin pin;
    private final static String URL = "https://www.novatrix.com.br/candies-paypal-php/gqi.php?q=";
    
    private class ClientRequester implements Runnable    {

        @Override
        public void run() {
            HttpClient client = HttpClients.createMinimal();
            HttpGet request = new HttpGet(URL + getQueue());
            request.addHeader("User-Agent", HttpHeaders.USER_AGENT);
            
            while (!Thread.currentThread().isInterrupted())    {
                try {
                    HttpResponse resp = client.execute(request);
                    
                    if (resp.getStatusLine().getStatusCode()==200)   {
                        
                        BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
 
                        StringBuffer result = new StringBuffer();
                        String line = "";
                        while ((line = rd.readLine()) != null) {
                            result.append(line);
                        }
                        
                        String retorno = result.toString();
                        if (retorno.trim().length()>2)  {
                            String[] retornos = retorno.split("|");
                            if (retornos.length>1)  {
                                if ("1".equals(retornos[0]))    {
                                    try {
                                        pin.setValue(true);
                                        System.out.println("--> GPIO state should be: ON");

                                        Thread.sleep(5000);

                                        // turn off gpio pin #01
                                        pin.setValue(false);
                                        System.out.println("--> GPIO state should be: OFF");

                                        Speaker.speak("Obrigado&nbsp;Obrigado", "pt");

                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(CandiesMqttListener.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (IOException ex)    {
                                        System.err.println("The motor couldn't be actioned.");
                                        Logger.getLogger(CandiesMqttListener.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                } else if ("0".equals(retornos[0]))     {
                                    System.out.println("There is not new purchases...");
                                }
                            }
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(CandiesHttpListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
    
    public CandiesHttpListener(GPIOPin parPin, String queue)    {
        this.setPin(parPin);
        this.setQueue(queue);
        System.out.println("Initializing HTTP Client for queue at: " +  URL + this.getQueue());
        new Thread(new ClientRequester()).run();
    }

    /**
     * @return the queue
     */
    public String getQueue() {
        return queue;
    }

    /**
     * @param queue the queue to set
     */
    public void setQueue(String queue) {
        this.queue = queue;
    }

    /**
     * @return the pin
     */
    public GPIOPin getPin() {
        return pin;
    }

    /**
     * @param pin the pin to set
     */
    public void setPin(GPIOPin pin) {
        this.pin = pin;
    }
    
    
}
