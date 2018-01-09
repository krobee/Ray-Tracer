public class Material {

    private double phong;
    private double[] Ka, Kd, Ks, Kr;
    private String name;

    public Material() {
        Ka = new double[3];
        Kd = new double[3];
        Ks = new double[3];
        Kr = new double[3];
    }

    public Material(String name) {
        this.name = name;
        Ka = new double[3];
        Kd = new double[3];
        Ks = new double[3];
        Kr = new double[3];
    }

    public void setPhong(String[] array) {
        phong = Double.parseDouble(array[1]);
    }

    public void setPhong(double val) {
        phong = val;
    }

    public void setKa(String[] array) {
        for (int i = 0; i < 3; i++) {
            Ka[i] = Double.parseDouble(array[i + 1]);
        }
    }

    public void setKd(String[] array) {
        for (int i = 0; i < 3; i++) {
            Kd[i] = Double.parseDouble(array[i + 1]);
        }
    }

    public void setKs(String[] array) {
        for (int i = 0; i < 3; i++) {
            Ks[i] = Double.parseDouble(array[i + 1]);
        }
    }

    public void setKa(int index, double val) {
        Ka[index] = val;
    }

    public void setKd(int index, double val) {
        Kd[index] = val;
    }

    public void setKs(int index, double val) {
        Ks[index] = val;
    }

    public void setKr(int index, double val) {
        Kr[index] = val;
    }


    public String getName() {
        return name;
    }

    public double[] getKa() {
        return Ka;
    }

    public double[] getKd() {
        return Kd;
    }

    public double[] getKs() {
        return Ks;
    }

    public double[] getKr() {
        return Kr;
    }

    public double getPhong() {
        return phong;
    }
}
