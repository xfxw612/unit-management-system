package com.example.unitsys.entity;

/** 分组计数（人员结构用）：label 为分组名，value 为人数。 */
public class NameCount {

    private String label;
    private Integer value;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
