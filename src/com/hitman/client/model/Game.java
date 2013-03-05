package com.hitman.client.model;

import org.joda.time.DateTime;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class Game implements Serializable {

    private int id;
    private String name;
    private LatLng location;
    private DateTime startDate;
    private Set<Player> players;
    private List<GameEvent> events;

    public Game(int id, String name, LatLng location,
                DateTime startDate, Set<Player> players,
                List<GameEvent> events,
                boolean saved) {
        assert id >= 0;
        this.id = id;
        this.name = name;
        this.location = location;
        this.players = players;
        this.startDate = startDate;
        this.events = events;

    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LatLng getLocation() {
        return location;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public DateTime getStartDateTime() {
        return startDate;
    }

    public List<GameEvent> getEvents() {
        return events;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

}
