package com.example.mobile.sdaclient;

/**
 * Created by kellymaclauchlan on 2018-02-23.
 */

public class ServerConnection {
    //https://developer.android.com/training/volley/simple.html
    //that resource should help we should be able to use most of what we did with the bank client i think
    String ipadd="http://localhost:8080/";
    String baseUrl="COMP4601-SDA/rest/sda";
    String compleatURL=ipadd+baseUrl;
    String delete="/delete";
    String search="/search";
    String reset="/reset";
    String list="/list";
    String pagerank="/pagerank";
    String boost="/boost";
}
