package org.androidsofdeath.client.model;

import android.location.Location;
import org.joda.time.DateTime;

public class Game {

    private int id;
    private String name;
    private Location location;
    private int numPlayers;
    private DateTime startDate;

    public Game(int id, String name, Location location, int numPlayers, DateTime startDate) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.numPlayers = numPlayers;
        this.startDate = startDate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public DateTime getStartDate() {
        return startDate;
    }

}
