package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class Competitor{
    private long Starttime;
    private Integer ID;
    private Integer rank;


    public long getStarttime() {
        return Starttime;
    }

    public void setStarttime(long starttime) {
        Starttime = starttime;
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}