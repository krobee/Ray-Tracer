public class Sphere extends Surface {

    private double[] C;
    private double r;


    public Sphere(String[] array) {
        super();
        C = new double[3];

        for (int i = 0; i < 3; i++) {
            C[i] = Double.parseDouble(array[i + 1]);
            getMtl().setKa(i, Double.parseDouble(array[i + 5]));
            getMtl().setKd(i, Double.parseDouble(array[i + 8]));
            getMtl().setKs(i, Double.parseDouble(array[i + 11]));
            getMtl().setKr(i, Double.parseDouble(array[i + 14]));
        }

        r = Double.parseDouble(array[4]);
        getMtl().setPhong(16);
    }

    public double[] getC() {
        return C;
    }

    public double getR() {
        return r;
    }
}
