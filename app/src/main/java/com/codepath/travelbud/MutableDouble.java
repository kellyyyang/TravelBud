package com.codepath.travelbud;

public class MutableDouble {
    double value = 1.0; // note that we start at 1 since we're counting

    public void increment () { ++value;      }
    public void divideBy (int num) {
        value = value / num;
    }
    public void addDouble (double addend) {
        value += addend;
    }
    public double  get ()       { return value; }
}
