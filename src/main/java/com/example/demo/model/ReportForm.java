package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportForm {
    private Integer userid;
    private Integer score;
    private Integer truenum;
    public Integer getID() {
        return userid;
    }

    public void setID(Integer ID) {
        this.userid = ID;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTrue() {
        return truenum;
    }

    public void setTrue(Integer aTrue) {
        this.truenum = aTrue;
    }
}
