package com.example.unitsys.entity;

/** 部门绩效统计行：人数、平均分、任务完成率。taskRate 由 Service 计算。 */
public class DeptStat {

    private Integer deptId;
    private String deptName;
    private String leader;
    private Integer headcount;
    private Double avgScore;
    private Integer totalTasks;
    private Integer doneTasks;
    private Double taskRate;

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public Integer getHeadcount() {
        return headcount;
    }

    public void setHeadcount(Integer headcount) {
        this.headcount = headcount;
    }

    public Double getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(Double avgScore) {
        this.avgScore = avgScore;
    }

    public Integer getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(Integer totalTasks) {
        this.totalTasks = totalTasks;
    }

    public Integer getDoneTasks() {
        return doneTasks;
    }

    public void setDoneTasks(Integer doneTasks) {
        this.doneTasks = doneTasks;
    }

    public Double getTaskRate() {
        return taskRate;
    }

    public void setTaskRate(Double taskRate) {
        this.taskRate = taskRate;
    }
}
