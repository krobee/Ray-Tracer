import Jama.Matrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Model {

    private ArrayList<double[]> vList;
    private ArrayList<int[]> fList;
    private Matrix vertMat;

    private ArrayList<Material> mtlList;

    public Model(String[] array) {
        vList = new ArrayList<>();
        fList = new ArrayList<>();
        mtlList = new ArrayList<>();
        double[] rotateVec = new double[3];
        double[] transVec = new double[3];

        for (int i = 0; i < 3; i++) {
            rotateVec[i] = Double.parseDouble(array[i + 1]);
            transVec[i] = Double.parseDouble(array[i + 6]);
        }

        double theta = Double.parseDouble(array[4]);
        double scale = Double.parseDouble(array[5]);

        read_obj(array[9]);

        // transformation
        rotate(rotateVec, theta);
        scale(scale);
        trans(transVec);
    }

    public void read_obj(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String currentLine;
            Material mtl = null;

            while ((currentLine = br.readLine()) != null) {
                String[] array = currentLine.split(" ");

                if (currentLine.startsWith("mtllib ")) {
                    read_mtl(array[1]);
                }

                if (currentLine.startsWith("v ")) {
                    double[] v = new double[3];
                    v[0] = Double.parseDouble(array[1]);
                    v[1] = Double.parseDouble(array[2]);
                    v[2] = Double.parseDouble(array[3]);
                    vList.add(v);
                }

                if (currentLine.startsWith("usemtl ")){
                    for(Material temp: mtlList){
                        if(temp.getName().equalsIgnoreCase(array[1])){
                            mtl = temp;
                        }
                    }
                }

                if (currentLine.startsWith("f ")) {
                    int[] f = new int[4];
                    f[0] = Integer.parseInt(array[1].split("//")[0]);
                    f[1] = Integer.parseInt(array[2].split("//")[0]);
                    f[2] = Integer.parseInt(array[3].split("//")[0]);
                    f[3] = mtlList.indexOf(mtl);
                    fList.add(f);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read_mtl(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String currentLine;
            Material mtl = null;

            while ((currentLine = br.readLine()) != null) {
                String[] array = currentLine.split(" ");

                if (currentLine.startsWith("newmtl ")) {
                    mtl = new Material(array[1]);
                }

                if (currentLine.startsWith("Ns "))
                    mtl.setPhong(array);

                if (currentLine.startsWith("Ka "))
                    mtl.setKa(array);

                if (currentLine.startsWith("Kd "))
                    mtl.setKd(array);

                if (currentLine.startsWith("Ks ")) {
                    mtl.setKs(array);

                    // set default Kr
                    mtl.setKr(0, 1);
                    mtl.setKr(1, 1);
                    mtl.setKr(2, 1);

                    mtlList.add(mtl);
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void rotate(double[] rotateVec, double theta) {
        // get rotation matrix RM and RMt
        Matrix RM = getRM(rotateVec);
        Matrix RMt = RM.transpose();

        // get Rz
        theta = Math.toRadians(theta);
        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);
        double[][] RZa = new double[3][3];

        RZa[0] = new double[]{cosTheta, -sinTheta, 0};
        RZa[1] = new double[]{sinTheta, cosTheta, 0};
        RZa[2] = new double[]{0, 0, 1};
        Matrix RZ = new Matrix(RZa);

        // combine two parts
        Matrix RT = RMt.times(RZ).times(RM);

        // apply to vertMat
        double[][] vertArray = new double[vList.size()][3];
        for (int i = 0; i < vertArray.length; i++) {
            vertArray[i] = vList.get(i);
        }

        vertMat = new Matrix(vertArray);
        vertMat = vertMat.transpose();
        vertMat = RT.times(vertMat);

    }

    public Matrix getRM(double[] rotateVec) {
        // normalize W
        Matrix Wv = new Matrix(rotateVec, 1);
        Wv = Wv.times(1 / Wv.normF());
        // find M
        double[] Wa = Wv.copy().getArray()[0];
        double[] Ma = Wv.copy().getArray()[0];


        int index = 0;
        for (int i = 0; i < 3; i++) {
            if (Ma[i] == getMin(Wa)) index = i;
        }
        Ma[index] = 1;

        // find U
        double[] Ua = Util.cross_product(Wa, Ma);
        double sum = 0;
        for (int i = 0; i < 3; i++) {
            sum += Ua[i];
        }
        if (sum == 0) {
            if (index == 0)
                Ma[index + 1] = 1;
            else if (index == 1)
                Ma[index + 1] = 1;
            else
                Ma[index - 1] = 1;
        }

        Ua = Util.cross_product(Wa, Ma);

        Matrix Uv = new Matrix(Ua, 1);
        Uv = Uv.times(1 / Uv.normF());
        Ua = Uv.getArray()[0];

        // find V
        double[] Va = Util.cross_product(Wa, Ua);

        // generate rotation matrix RM
        double[][] RMa = new double[3][3];
        RMa[0] = Ua;
        RMa[1] = Va;
        RMa[2] = Wa;
        Matrix RM = new Matrix(RMa);
        return RM;
    }

    public void scale(double scale) {
        //get scale matrix
        double[][] scaleArray = new double[4][4];
        scaleArray[0] = new double[]{scale, 0, 0, 0};
        scaleArray[1] = new double[]{0, scale, 0, 0};
        scaleArray[2] = new double[]{0, 0, scale, 0};
        scaleArray[3] = new double[]{0, 0, 0, 1};
        Matrix scaleMat = new Matrix(scaleArray);

        // apply to vertMat
        int colNum = vertMat.getColumnDimension();
        double[][] vertArray = new double[4][colNum];
        vertArray[0] = vertMat.getArray()[0];
        vertArray[1] = vertMat.getArray()[1];
        vertArray[2] = vertMat.getArray()[2];
        double[] temp = new double[colNum];
        for (int i = 0; i < colNum; i++) {
            temp[i] = 1;
        }
        vertArray[3] = temp;
        Matrix vertMatAug = new Matrix(vertArray);
        vertMat = scaleMat.times(vertMatAug);
    }

    public void trans(double[] transVec) {
        // get trans matrix
        double[][] transArray = new double[4][4];
        transArray[0] = new double[]{1, 0, 0, transVec[0]};
        transArray[1] = new double[]{0, 1, 0, transVec[1]};
        transArray[2] = new double[]{0, 0, 1, transVec[2]};
        transArray[3] = new double[]{0, 0, 0, 1};
        Matrix transMat = new Matrix(transArray);

        // apply to vertMat
        vertMat = transMat.times(vertMat).transpose();

        double[][] vertArray = new double[vertMat.getRowDimension()][3];

        for (int i = 0; i < vertMat.getRowDimension(); i++) {
            for (int j = 0; j < 3; j++) {
                vertArray[i][j] = vertMat.getArray()[i][j];
            }
        }
        vertMat = new Matrix(vertArray);
    }

    public double getMin(double[] array) {
        double min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) min = array[i];
        }
        return min;
    }

    public ArrayList<double[]> getVList() {
        return vList;
    }

    public ArrayList<int[]> getFList() {
        return fList;
    }

    public Matrix getMat() {
        return vertMat;
    }

    public void printVList() {
        for (int i = 0; i < vList.size(); i++) {
            System.out.println(Arrays.toString(vList.get(i)));
        }
    }

    public void printFList() {
        for (int i = 0; i < fList.size(); i++) {
            System.out.println(Arrays.toString(fList.get(i)));
        }
    }

    public ArrayList<Material> getMtlList() {
        return mtlList;
    }
}
