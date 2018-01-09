import Jama.Matrix;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.*;

public class Raytracer {
    Camera cam;
    double[] ambient;
    ArrayList<Light> lights;
    ArrayList<Sphere> spheres;
    ArrayList<Model> models;
    private int recursionLevel;

    public Raytracer() {
        cam = new Camera();
        ambient = new double[3];
        lights = new ArrayList<>();
        spheres = new ArrayList<>();
        models = new ArrayList<>();
    }

    public Surface ray_find(Ray ray) {
        for (Sphere sphere : spheres) {
            ray.sphere_test(sphere);
        }
        for (Model model : models) {
            ray.model_test(model);
        }
        return ray.getSurface();
    }

    public boolean shadow(Matrix pt, Light lt) {
        Matrix ltP = new Matrix(lt.getP(), 1);
        Matrix L = ltP.minus(pt);
        Matrix DV = L.times(1 / L.normF());
        Ray ray = new Ray(pt, DV);
        double dtl = L.times(ray.getDV().transpose()).getArray()[0][0];

        boolean ret = false;

        for (Sphere sphere : spheres) {
            if (ray.sphere_test(sphere) && ray.getT() < dtl) {
                ret = true;
            }
        }

        for (Model model : models) {
            if (ray.model_test(model) && ray.getT() < dtl) {
                ret = true;
            }
        }
        return ret;
    }

    public double[] pt_illum(Ray ray, Matrix N, Material mtl, double[] accum, double[] refatt) {
        double[] color = new double[3];

        // ambient
        for (int i = 0; i < color.length; i++) {
            color[i] = ambient[i] * mtl.getKa()[i];
        }

        // iterate through each light
        for (Light light : lights) {

            // get unit vector toL
            Matrix light_L = new Matrix(light.getP(), 1);
            Matrix toL = light_L.minus(ray.getPt());
            toL = toL.times(1 / toL.normF());

            // get NL
            double NL = N.times(toL.transpose()).getArray()[0][0];

            if (NL > 0 && shadow(ray.getPt(), light) == false) {
                for (int i = 0; i < color.length; i++) {
                    // diffuse
                    color[i] += mtl.getKd()[i] * light.getB()[i] * NL;

                    // specular & phong
//                    Matrix toC = ray.getPt().minus(ray.getPt());
//                    toC = toC.times(1 / toC.normF());
                    Matrix toC = ray.getDV().times(-1);

                    Matrix spR = N.times(2 * NL).minus(toL);
                    spR = spR.times(1 / spR.normF());

                    double CdR = toC.times(spR.transpose()).getArray()[0][0];
                    if (CdR > 0) {
                        color[i] += mtl.getKs()[i] * light.getB()[i] * Math.pow(CdR, mtl.getSpow());
                    }
                }
            }
        }
        for (int i = 0; i < accum.length; i++) {
            accum[i] += refatt[i] * mtl.getKo()[i] * color[i];
        }
        return accum;
    }



    public double[] ray_trace(Ray ray, double[] accum, double[] refatt, int level) {

        if (ray_find(ray) != null) {

            // get material
            Material mtl = ray.getSurface().getMtl();

            // get surface normal N
            Matrix N = null;
            if (ray.getSurface() instanceof Sphere) {
                Matrix sphere_c = new Matrix(((Sphere) ray.getSurface()).getC(), 1);
                N = ray.getPt().minus(sphere_c);
                N = N.times(1 / N.normF());
            } else if (ray.getSurface() instanceof Triangle) {
                N = ((Triangle) ray.getSurface()).getNormal();
            }

            accum = pt_illum(ray, N, mtl, accum, refatt);

            if (level > 0) {
                double[] flec = new double[3];

                Matrix toC = ray.getDV().times(-1);
                double NC = N.times(toC.transpose()).getArray()[0][0];
                Matrix refR = N.times(2 * NC).minus(toC);
                refR = refR.times(1 / refR.normF());

                double[] refattTemp = new double[3];
                for (int i = 0; i < refattTemp.length; i++) {
                    refattTemp[i] = refatt[i] * mtl.getKr()[i];
                }
//                accum = ray_trace(new Ray(ray.getPt(), refR), accum, refattTemp, (level - 1));

                flec = ray_trace(new Ray(ray.getPt(), refR), flec, refattTemp, (level - 1));
                for(int i = 0; i < 3; i++){
                    accum[i] += refatt[i] * mtl.getKo()[i] * flec[i];
                }
            }

            if (level > 0 && DoubleStream.of(mtl.getKo()).sum() < 3){
                double[] thru = new double[3];
                Ray fraR = ((Sphere)ray.getSurface()).refract_exit(ray.getDV().times(-1), ray.getPt(), mtl.getEta(), 1);
                if(fraR != null){
                    double[] refattTemp = new double[3];
                    for (int i = 0; i < refattTemp.length; i++) {
                        refattTemp[i] = refatt[i] * mtl.getKr()[i];
                    }

                    thru = ray_trace(fraR, thru, refattTemp, (level - 1));
                    for(int i = 0; i < 3; i++){
                        accum[i] += refatt[i] * ( 1- mtl.getKo()[i]) * thru[i];
                    }
                }
            }


        }

        return accum;
    }

    public void render(String fileName) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
            bw.write("P3\n");
            bw.write((int) cam.getWidth() + " " + (int) cam.getHeight() + " " + 255 + "\n");
            for (int i = 0; i < cam.getHeight(); i++) {
                for (int j = 0; j < cam.getWidth(); j++) {
                    double[] rgb = pixel_ray(j, (int) cam.getWidth() - i - 1, 4);

                    int r = (int) Math.max(0, Math.min(255, Math.round(rgb[0] * 255)));
                    int g = (int) Math.max(0, Math.min(255, Math.round(rgb[1] * 255)));
                    int b = (int) Math.max(0, Math.min(255, Math.round(rgb[2] * 255)));
                    bw.write(r + " " + g + " " + b + " ");
                }
                bw.write("\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double[] pixel_ray(int i, int j, int num) {
        double[] rgb = new double[3];


        for(int index = 0; index < num; index++){
            double[] color = new double[3];
            double[] att = {1, 1, 1};

            double px = i / (cam.getWidth() - 1) * (cam.getRight() - cam.getLeft()) + cam.getLeft();
            double py = j / (cam.getHeight() - 1) * (cam.getTop() - cam.getBottom()) + cam.getBottom();
            px = rand(px);
            py = rand(py);

            // get LV and DV for RAY
            Matrix LV = cam.getEV().plus(cam.getWV().times(cam.getNear()).plus(cam.getUV().times(px)).plus(cam.getVV().times(py)));
            Matrix DV = LV.minus(cam.getEV());
            DV = DV.times(1 / DV.normF());
            Ray ray = new Ray(LV, DV);

            double[] rgbTemp = ray_trace(ray, color, att, recursionLevel);
            for(int k = 0; k < 3; k++){
                rgb[k] += rgbTemp[k];
            }
        }

        for(int index = 0; index < 3; index++){
            rgb[index] = rgb[index]/num;
        }
        return rgb;

    }

    public static double rand(double x){
        double randomNum = ThreadLocalRandom.current().nextDouble(x-0.0001, x+0.0001);
        return randomNum;
    }

    public void read_driver(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

            String currentLine;

            while ((currentLine = br.readLine()) != null) {
                String[] array = currentLine.split(" ");

                if (currentLine.startsWith("eye "))
                    cam.setEye(array);

                if (currentLine.startsWith("look "))
                    cam.setLook(array);

                if (currentLine.startsWith("up "))
                    cam.setUp(array);

                if (currentLine.startsWith("d "))
                    cam.setNear(array);

                if (currentLine.startsWith("bounds "))
                    cam.setBounds(array);

                if (currentLine.startsWith("res "))
                    cam.setRes(array);

                if (currentLine.startsWith("recursionLevel"))
                    recursionLevel = Integer.parseInt(array[1]);

                if (currentLine.startsWith("ambient ")) {
                    for (int i = 0; i < ambient.length; i++) {
                        ambient[i] = Double.parseDouble(array[i + 1]);
                    }
                }

                if (currentLine.startsWith("light ")) {
                    Light light = new Light(array);
                    lights.add(light);
                }

                if (currentLine.startsWith("sphere ")) {
                    Sphere sphere = new Sphere(array);
                    spheres.add(sphere);
                }

                if (currentLine.startsWith("model ")) {
                    Model model = new Model(array);
                    models.add(model);
                }

            }
            cam.setUVW();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Raytracer [driver] [output]");
            System.exit(-1);
        }

        Raytracer raytracer = new Raytracer();

        raytracer.read_driver(args[0]);
        raytracer.render(args[1]);
    }
}
