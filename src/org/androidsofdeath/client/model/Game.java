package org.androidsofdeath.client.model;

import java.util.Date;

public class Game {

    private String name;
    private String location;
    private int numPlayers;
    private Date startDate;

    public Game(String name, String location, int numPlayers, Date startDate) {
        this.name = name;
        this.location = location;
        this.numPlayers = numPlayers;
        this.startDate = startDate;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public Date getStartDate() {
        return startDate;
    }

}
