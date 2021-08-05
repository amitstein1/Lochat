package com.example.stein.client_1;

/**
 * Created by amit stein on 11/01/2018.
 */


import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.net.Socket;
import java.net.SocketException;

import java.net.SocketTimeoutException;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class ClientTask extends AsyncTask<String,Void,Void> {
    private Socket client_sock;
    private PrintWriter pw;
    private BufferedReader br;
    DataInputStream in;
    private static final String TAG = "ClientTask";
    private boolean connected;
    static Queue<String> queToSend;
    static Queue<String> queRecv;
    private Serialization serialize;

    private final String SIZE_HEADER_FORMAT = "000000|";
    private final int size_header_size = SIZE_HEADER_FORMAT.length();
    private final Boolean DEBUG = true;








    ClientTask() throws IOException {//Queue<String> queToSend, Queue<String> queRecv) throws IOException {
        queToSend = new LinkedList<String>();//queToSend;
        queRecv = new LinkedList<String>();//queRecv;
        this.pw = null;
        this.br = null;
        this.in = null;
        serialize = new Serialization();
        //genderType gender;

        //this.br = new BufferedReader(new InputStreamReader(client_sock.getInputStream()));//set the input stream
        //Log.d("client_sock", "buffer read is open");
        //this.pw = new PrintWriter(this.client_sock.getOutputStream());//set the output stream
        //Log.d("client_sock", "buffer write is open");
    }

    private Boolean connect(){
        try {
            Log.d(TAG, "before connect");

            String homeRoutersIP = "94.159.249.129";
            int homePort = 4900;

            String schoolRoutersIP = "37.142.40.150";
            int schoolPort = 20139;
            String ipPhone = "192.168.42.124";
            String ofirIpPhone = "192.168.43.226";
            String yossiIpPhone = "192.168.43.226";

            //myPublicIP = "37.142.40.150"; also need to port forwarding on the router to the right computer
            String myLocalIP = "10.0.2.2";
            //this.client_sock = new Socket();
            //int connection_time_out = 1000;
            //this.client_sock.connect(new InetSocketAddress(homeRoutersIP,homePort),connection_time_out); //local 10.0.2.2 "192.168.1.8"
            this.client_sock = new Socket(yossiIpPhone,homePort);
            Log.d(TAG, "connect: " + homeRoutersIP + ", " + String.valueOf(homePort));
            client_sock.setSoTimeout(100);
            Log.d(TAG, "after connect");
            //only after the connect we can set the streams
            this.pw = new PrintWriter(this.client_sock.getOutputStream());//set the output stream
            //this.in = new DataInputStream(this.client_sock.getInputStream());
            this.br = new BufferedReader(new InputStreamReader(client_sock.getInputStream()));//set the input stream
            Log.d(TAG, "after setting streams");

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "did not connected:" + e.toString());
            return false;
        }

    }

    private void tryRecv() {
        String message = null;
        try {
            //Log.d("client_sock", "before receive");
            //if (client_sock.getInputStream().available() != 0)
            message = this.br.readLine();
            //message = recvBySize(this.client_sock,this.br);
            //Log.d("client_sock", "after receive");
        } catch (Exception e) {
            //do nothing
            //Log.d("client_sock", "exeption after readline error" + e.toString());
        }
        if (message != null) {
            Log.d(TAG, "received" + ": " + message);
            /*List<String> listMassage = Arrays.asList(message.split("@"));
            Log.d(TAG, "listMassage=" + listMassage);
            String action = listMassage.get(0);
            List<String> params = listMassage.subList(1, listMassage.size());
            Log.d(TAG, "action=" + action);
            Log.d(TAG, "params=" + params);
            if (action.equals("user_obj")){
                Log.d(TAG, "before updating user");
                serialize.updatedStaticUser(params);
                Log.d(TAG, "after updating user");
            }
            else*/
            queRecv.add(message); //send to the current activity
            Log.d(TAG, "queRecv add" + ": " + message);

        }
        else{
            Log.d("client_sock", "received" + ": null");
        }
        //return message;
    }

    //the messages comes in a queue from the main activity
    private void trySend(){
        if (!queToSend.isEmpty()) {
            Log.d(TAG, "before send");
            String message = queToSend.remove();
            Log.d(TAG, "queToSend remove" + ": " + message);
            this.pw.write(message); //send to server
            Log.d(TAG, "after send");
            this.pw.flush();
            //pw.close();



        }
    }


    @Override
    protected Void doInBackground(String... params) {
        Log.d(TAG, "now we are in background");
        ///make while for connect so when the server disconnect and connect again,
        ///the client will connect automatically
        if (this.connect()) {
            while (true) {
                try {
                    //one send function for all data - all data comes from main activity
                    //before send - lock the queue, remove everything, send all together
                    trySend();  //sends all the data from the main program - with the location data which will be the data most of the time

                    tryRecv();//need a time out so we will not get stuck in block mode


                    //TimeUnit.SECONDS.sleep((long) (0.01));
                    /*
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(c,"Message sent",Toast.LENGTH_LONG).show();
                        }
                    });
                    */
                    //

                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
            //try {
            //    br.close();
            //} catch (IOException e) {
            //    e.printStackTrace();
            //}pw.close();

        }
        else{
            //Log.d(TAG, "did not connected");
            return null;
        }


    }

    protected void finalize(){
        //Objects created in run method are finalized when
        //program terminates and thread exits
        try{
            client_sock.close();
            //automatically by jave: br.close(); and pw.close();

        } catch (IOException e) {
            System.out.println("Could not close socket");
            System.exit(-1);
        }
    }

}







