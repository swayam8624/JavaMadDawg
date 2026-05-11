package com.singal.maddog.graphics;

import java.util.Arrays;

public class Screen {

    private int width, height;
    public int[] pixels;

    int xtime ;
    int ytime ;
    int counter=0;

    public Screen(int width, int height) {
        this.width = width;
        this.height = height;
        pixels = new int[width * height]; // flattened array for better performance than accessing a 2d array
    }

    public void render() {
        counter++;
        if(counter%100==0)xtime++;
        if(counter%200==0)ytime++;
        for(int y=0; y<height; y++) {
            if(ytime>=height) break;
            for(int x=0; x<width; x++) {
                if (xtime>=width) break;
                pixels[ytime * width + xtime] = 0xff00ff; // since we are using a one dimensional array, we access it like this
            }
        }
    }

    public void clear(){
        Arrays.fill(pixels, 0); // can be done using a for loop manually
    }

    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public void setHeight(int height) {
        this.height = height;
    }


}
