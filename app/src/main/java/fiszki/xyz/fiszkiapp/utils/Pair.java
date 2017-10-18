package fiszki.xyz.fiszkiapp.utils;

import java.io.Serializable;

public class Pair implements Serializable{
    private String leftValue;
    private String rightValue;

    public Pair(){
        leftValue = null;
        rightValue = null;
    }

    public Pair(String leftValue, String rightValue){
        this.leftValue = leftValue;
        this.rightValue = rightValue;
    }

    public void swapValues(){
        String temp = leftValue;
        leftValue = rightValue;
        rightValue = temp;
    }

    public String getLeftValue() {
        return leftValue;
    }

    public void setLeftValue(String leftValue) {
        this.leftValue = leftValue;
    }

    public String getRightValue() {
        return rightValue;
    }

    public void setRightValue(String rightValue) {
        this.rightValue = rightValue;
    }
}
