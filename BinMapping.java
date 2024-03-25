package com.playtech.assignment;

public class BinMapping {
    String name;
    String rangeFrom;
    String rangeTo;
    String type;
    String country;

    public BinMapping(String name, String rangeFrom, String rangeTo, String type, String country) {
        this.name = name;
        this.rangeFrom = rangeFrom;
        this.rangeTo = rangeTo;
        this.type = type;
        this.country = country;
    }

    @Override
    public String toString() {  //For debugging purposes
        return "BinMapping{" +
                "name='" + name + '\'' +
                ", rangeFrom=" + rangeFrom +
                ", rangeTo=" + rangeTo +
                ", type='" + type + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
