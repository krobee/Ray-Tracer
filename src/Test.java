import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Test {

    private ArrayList<Material> mtlList;
    private ArrayList<int[]> fList;

    public Test() {
        mtlList = new ArrayList<>();
        fList = new ArrayList<>();
    }

    public static void main(String[] args) {
        Test t = new Test();

        t.read_obj("mu90.obj");
    }

    public void read_obj(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String currentLine;
            BufferedWriter bw = new BufferedWriter(new FileWriter("mu90.obj"));

            while ((currentLine = br.readLine()) != null) {

                if (currentLine.startsWith("f ")) {

                    bw.write(currentLine.replaceAll("/", "//"));

                }
                else{
                    bw.write(currentLine);
                }
                bw.write("\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
