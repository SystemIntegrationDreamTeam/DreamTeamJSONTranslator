/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.dreamteamjsontranslator;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;

/**
 *
 * @author Buhrkall
 */
public class Translator {
    
    static final String SENDING_QUEUE_NAME = "DreamTeamJSONQueue";
    static final String LISTENING_QUEUE_NAME = "DreamTeamJSONTranslatorQueue";
    static final String EXCHANGE_NAME = "TranslatorExchange";
    
    static String message = "";
    static Gson gson = new Gson();
    
    public static void main(String[] args) throws IOException {
        
        
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("datdb.cphbusiness.dk");
    factory.setVirtualHost("student");
    factory.setUsername("Dreamteam");
    factory.setPassword("bastian");
    Connection connection = factory.newConnection();
    final Channel listeningChannel = connection.createChannel();
    final Channel sendingChannel = connection.createChannel();

    
    listeningChannel.queueDeclare(LISTENING_QUEUE_NAME, false, false, false, null);
    
    listeningChannel.queueBind(LISTENING_QUEUE_NAME, EXCHANGE_NAME, "DreamTeamBankJSON");

    
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    Consumer consumer = new DefaultConsumer(listeningChannel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {
          System.out.println("Hello");
         message = new String(body, "UTF-8");
         System.out.println(" [x] Received '" + message + "'");

         String [] arr = message.split(",");
         
         Result res = new Result(arr[0],Integer.parseInt(arr[1]),Double.parseDouble(arr[2]),Integer.parseInt(arr[3]));
         
         
         String result = gson.toJson(res);
         
         
         
         sendingChannel.queueDeclare(SENDING_QUEUE_NAME, false, false, false, null);
         sendingChannel.basicPublish("", SENDING_QUEUE_NAME, null, result.getBytes());
    
         
      }
    };
    listeningChannel.basicConsume(LISTENING_QUEUE_NAME, true, consumer);
        
        
        
        
    }
    
    
}
