public class Light {
    private double[] P, B;

    public Light(String[] array){

        P = new double[3];
        B = new double[3];

        for(int i = 0; i < 3; i++){
            P[i] = Double.parseDouble(array[i+1]);
            B[i] = Double.parseDouble(array[i+5]);
        }
    }

    public double[] getP() {
        return P;
    }

    public double[] getB() {
        return B;
    }
}
