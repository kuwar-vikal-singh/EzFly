package com.axel.myapplication.Model;

public class Seat {
    public enum SeatStatus{
        AVAILABLE,SELECTED,UNAVAILABLE,EMPTY
    }

    private SeatStatus status;
    private String name;

    public Seat(SeatStatus status, String name) {
        this.status = status;
        this.name = name;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
