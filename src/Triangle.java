import Jama.Matrix;

public class Triangle extends Surface {
    Matrix v1,v2,v3;

    public Triangle(Matrix v1, Matrix v2, Matrix v3, Material mtl){
        super();
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        setMtl(mtl);
    }

    public Matrix getNormal(){
        Matrix e1 = v1.minus(v2);
        Matrix e2 = v3.minus(v1);
        Matrix N = new Matrix(Util.cross_product(e1.getArray()[0], e2.getArray()[0]),1);
        N = N.times(1 / N.normF());
        return N;
    }
}
