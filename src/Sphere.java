import Jama.Matrix;

import java.util.Arrays;
import java.util.stream.*;

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
            getMtl().setKo(i, Double.parseDouble(array[i + 17]));
        }

        r = Double.parseDouble(array[4]);
        getMtl().setPhong(16);
        getMtl().setSpow(Double.parseDouble(array[20]));
        getMtl().setEta(Double.parseDouble(array[21]));
    }

    public double[] getC() {
        return C;
    }

    public double getR() {
        return r;
    }

    public Matrix refract_tray(Matrix W, Matrix pt, Matrix N, double eta1, double eta2){
        double etar = eta1 / eta2;
        double a = -etar;
        double wn = W.times(N.transpose()).getArray()[0][0];
        double radsq = Math.pow(etar, 2) * (Math.pow(wn, 2) - 1) + 1;

        Matrix T;
        if(radsq < 0.0){
            T = new Matrix(1,3);
        }
        else{
            double b = (etar * wn) - Math.sqrt(radsq);
            T = W.times(a).plus(N.times(b));
        }
        return T;
    }

    public Ray refract_exit(Matrix W, Matrix pt, double eta_in, double eta_out){
        Matrix matC = new Matrix(C,1);
        Matrix pt_C = pt.minus(matC);
        pt_C = pt_C.times(1 / pt_C.normF());

        Matrix T1 = refract_tray(W, pt, pt_C, eta_out, eta_in);
        if(T1.normInf() == 0){
            return null;
        }
        else{
            Matrix C_pt = matC.minus(pt);
            Matrix exit = pt.plus(T1.times(2*C_pt.times(T1.transpose()).getArray()[0][0]));

            Matrix Nin = matC.minus(exit);
            Nin = Nin.times(1 / Nin.normF());
            Matrix T2 = refract_tray(T1.times(-1), exit, Nin, eta_in, eta_out);
            Ray refR = new Ray(exit, T2);
            return refR;
        }
    }
}
