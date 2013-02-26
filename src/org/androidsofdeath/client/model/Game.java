package org.androidsofdeath.client.model;

import android.location.Location;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Set;

public class Game implements Serializable {

    private int id;
    private String name;
    private Location location;
    private Set<Player> players;
    private DateTime startDate;

    public Game(int id, String name, Location location,
                Set<Player> players, DateTime startDate, boolean saved) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.players = players;
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

    public Set<Player> getPlayers() {
        return players;
    }

    public DateTime getStartDate() {
        return startDate;
    }

}
