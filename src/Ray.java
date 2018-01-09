import Jama.Matrix;

import java.util.ArrayList;

public class Ray {

    private Matrix LV, DV, best_pt;
    private Surface best_surface;
    private double best_t;

    public Ray(Matrix LV, Matrix DV){
        this.LV = LV;
        this.DV = DV;
        best_t = Double.POSITIVE_INFINITY;
    }

    public boolean sphere_test(Sphere sphere){
        Matrix CV = new Matrix(sphere.getC(),1);
        Matrix TV = CV.minus(LV);

        Matrix temp = TV.times(DV.transpose());
        double v = temp.getArray()[0][0];

        temp = TV.times(TV.transpose());
        double csq = temp.getArray()[0][0];

        double r = sphere.getR();
        double disc = r*r - (csq - v*v);

        if(disc > 0){
            double t = v-Math.sqrt(disc);
            if(t < best_t && t >= 0.00001){
                best_t = t;
                best_surface = sphere;
                best_pt = LV.plus(DV.times(t));
            }
            return true;
        }
        else
            return false;
    }

    public boolean model_test(Model model){
        ArrayList<int[]> fList = model.getFList();
        ArrayList<Material> mtlList = model.getMtlList();
        Matrix vertMat = model.getMat();

        boolean ret = false;

        for(int k = 0; k < fList.size(); k++) {
            Matrix AV = vertMat.getMatrix(fList.get(k)[0] - 1, fList.get(k)[0] - 1, 0, 2);
            Matrix BV = vertMat.getMatrix(fList.get(k)[1] - 1, fList.get(k)[1] - 1, 0, 2);
            Matrix CV = vertMat.getMatrix(fList.get(k)[2] - 1, fList.get(k)[2] - 1, 0, 2);

            Material mtl = mtlList.get(fList.get(k)[3]);

            Triangle tri = new Triangle(AV, BV, CV, mtl);

            // set M
            Matrix M = new Matrix(3, 3);
            M.setMatrix(0, 0, 0, 2, AV.minus(BV));
            M.setMatrix(1, 1, 0, 2, AV.minus(CV));
            M.setMatrix(2, 2, 0, 2, DV);
            M = M.transpose();

            // set Y
            Matrix Y = AV.minus(LV).transpose();

            // solve for X

            Matrix X = M.solve(Y);
            double beta = X.get(0, 0);
            double gamma = X.get(1, 0);
            double t = X.get(2, 0);

            if (beta >= -0.00001 && gamma >= -0.00001 && (beta + gamma) <= 1.00001) {
                if(t < best_t && t >= 0.00001){
                    best_t = t;
                    best_surface = tri;
                    best_pt = LV.plus(DV.times(t));
                }
                ret = true;
            }
        }

        return ret;
    }

    public double getT(){
        return best_t;
    }

    public Matrix getDV(){
        return DV;
    }

    public Surface getSurface(){
        return best_surface;
    }

    public Matrix getPt(){
        return best_pt;
    }
}
