package com.example.unitsys.entity;

/** 绩效月度趋势：period 形如 2025-11。 */
public class TrendRow {

    private String period;
    private Double avgScore;
    private Integer headcount;

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Double getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(Double avgScore) {
        this.avgScore = avgScore;
    }

    public Integer getHeadcount() {
        return headcount;
    }

    public void setHeadcount(Integer headcount) {
        this.headcount = headcount;
    }
}
