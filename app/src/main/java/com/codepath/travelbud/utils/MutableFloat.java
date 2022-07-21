package com.codepath.travelbud.utils;

public class MutableFloat {
    float value = 1; // note that we start at 1 since we're counting

    public void increment () { ++value;      }
    public void divideBy (int num) {
        value = value / num;
    }
    public float  get ()       { return value; }
}
