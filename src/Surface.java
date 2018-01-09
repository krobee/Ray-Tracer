public class Surface {
    private Material mtl;

    public Surface(){
        mtl = new Material();
    }

    public void setMtl(Material mtl){
        this.mtl = mtl;
    }

    public Material getMtl(){
        return mtl;
    }
}
