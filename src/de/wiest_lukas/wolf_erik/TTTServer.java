/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wiest_lukas.wolf_erik;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import javax.swing.JTextPane;

/**
 *
 * @author lukas
 */
public class TTTServer implements Runnable
{
    JTextPane logLine;
    HashMap<Character, TTTClientConnection> clients;
    ServerSocket serverSocket;

    public TTTServer(JTextPane logLine)
    {
        this.logLine = logLine;
    }

    @Override
    public void run()
    {
        // add player O
        clients = new HashMap<>();
        clients.put('O', new TTTClientConnection('O'));
        clients.put('X', new TTTClientConnection('X'));

        clients.keySet().stream()
                .forEach(c ->
                {
                    try
                    {
                        System.out.println("wait for player " + c + " to connect");
                        serverSocket = new ServerSocket(5050);
                        TTTClientConnection client = clients.get(c);
                        client.setClient(serverSocket.accept());
                        System.out.println("player " + c+ " connected.");
                        
                    } catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                });

        while(!Thread.interrupted())
        {
            clients.values().stream().forEach(c ->
            {
                try
                {
                    if (c.in.available() > 0)
                    {
                        TTTProtocol telegram = (TTTProtocol) c.in.readObject();
                        System.out.println(telegram.message);
                    }
                }
                catch (IOException|ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
            });
        }
    }

    public class TTTClientConnection
    {
        char figure;
        Socket client;
        public ObjectInputStream in;
        public ObjectOutputStream out;

        public TTTClientConnection(char figure)
        {
            this.figure = figure;
            this.client = null;
        }

        public void setClient(Socket client) throws IOException
        {
            this.client = client;
            System.out.println("creating output stream for player " + figure);
            out = new ObjectOutputStream(client.getOutputStream());
            System.out.println("set client for player " + figure + " finished");
            out.flush();
            System.out.println("flushed output stream to player " + figure);
            System.out.println("creating input stream for player " + figure);
            in = new ObjectInputStream(client.getInputStream());
            System.out.println("set client for player " + figure + " finished");
        }
    }
}


