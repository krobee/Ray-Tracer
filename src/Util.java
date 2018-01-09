public class Util {

    public static double[] cross_product(double[] u, double[] v) {
        double[] result = new double[3];
        result[0] = u[1] * v[2] - v[1] * u[2];
        result[1] = v[0] * u[2] - u[0] * v[2];
        result[2] = u[0] * v[1] - v[0] * u[1];
        return result;
    }
}
