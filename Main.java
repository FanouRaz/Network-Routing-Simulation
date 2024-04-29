import java.io.IOException;

public class Main {
    public static void main(String... args){
        try{
            Graph graph = new Graph("graph.txt");

            graph.getShortestPath("8.8.8.0", "8.8.8.4");
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
