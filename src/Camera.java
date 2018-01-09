import Jama.Matrix;

public class Camera {

    private double[] eye, look, up;
    private double near, left, bottom, right, top;
    private double width, height;
    private Matrix EV, LV, UP, WV, UV, VV;

    public Camera() {
        eye = new double[3];
        look = new double[3];
        up = new double[3];
    }

    public void setEye(String[] array) {
        for (int i = 0; i < 3; i++) {
            eye[i] = Double.parseDouble(array[i + 1]);
        }
    }

    public void setLook(String[] array) {
        for (int i = 0; i < 3; i++) {
            look[i] = Double.parseDouble(array[i + 1]);
        }
    }

    public void setUp(String[] array) {
        for (int i = 0; i < 3; i++) {
            up[i] = Double.parseDouble(array[i + 1]);
        }
    }

    public void setNear(String[] array) {
        near = (-1) * Double.parseDouble(array[1]);
    }

    public void setBounds(String[] array) {
        left = Double.parseDouble(array[1]);
        bottom = Double.parseDouble(array[2]);
        right = Double.parseDouble(array[3]);
        top = Double.parseDouble(array[4]);
    }

    public void setRes(String[] array) {
        width = Integer.parseInt(array[1]);
        height = Integer.parseInt(array[2]);
    }

    public void setUVW() {
        EV = new Matrix(eye, 1);
        LV = new Matrix(look, 1);
        UP = new Matrix(up, 1);

        // get W
        WV = EV.minus(LV);
        WV = WV.times(1 / WV.normF());

        // get U
        UV = new Matrix(Util.cross_product(up, WV.getArray()[0]), 1);
        UV = UV.times(1 / UV.normF());

        // get V
        VV = new Matrix(Util.cross_product(WV.getArray()[0], UV.getArray()[0]), 1);
    }

    public double[] getEye() {
        return eye;
    }

    public double[] getLook() {
        return look;
    }

    public double[] getUp() {
        return up;
    }

    public double getNear() {
        return near;
    }

    public double getLeft() {
        return left;
    }

    public double getBottom() {
        return bottom;
    }

    public double getRight() {
        return right;
    }

    public double getTop() {
        return top;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Matrix getUV() {
        return UV;
    }

    public Matrix getVV() {
        return VV;
    }

    public Matrix getWV() {
        return WV;
    }

    public Matrix getEV() {
        return EV;
    }
}
