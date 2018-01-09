import Jama.Matrix;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.*;

public class Test {
    static Camera cam ;


    public static void main(String[] args) {
        cam = new Camera();
        String[] bounds = "bounds -2 -2 2 2".split(" ");
        cam.setBounds(bounds);
        String[] res = "res 8 8".split(" ");
        cam.setRes(res);
        for (int i = 0; i < cam.getHeight(); i++) {
            for (int j = 0; j < cam.getWidth(); j++) {
//                Ray ray = pixel_ray(j, (int) cam.getWidth() - i - 1);
               anti(j, (int)cam.getWidth()-i-1, 4);
            }


        }
    }

    public static void anti (int i, int j, int num){
        for(int index = 0; index < num; index++){
            double px = i / (cam.getWidth() - 1) * (cam.getRight() - cam.getLeft()) + cam.getLeft();
            double py = j / (cam.getHeight() - 1) * (cam.getTop() - cam.getBottom()) + cam.getBottom();
            System.out.println("px: " + px + " random_y: " + rand(px));
            System.out.println("py: " + py + " random_y: " + rand(py));
        }
        System.out.println();
    }

    public static double rand(double x){
        double randomNum = ThreadLocalRandom.current().nextDouble(x-0.5, x+0.5);
        return randomNum;
    }
}
