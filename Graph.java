import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Graph {
    private ArrayList<Server> nodes; 

    public Graph(ArrayList<Server> nodes){
        this.nodes = nodes;
    }

    public Graph(String filepath) throws IOException{
        BufferedReader  reader = new BufferedReader(new FileReader(filepath));

        String line;
        int n = Integer.valueOf(reader.readLine()) ,t=0;

        nodes = new ArrayList<>();
      

        while((line = reader.readLine()) != null){
            if(t < n){
                String[] splitted = line.split(" ");
                Server server = new Server(splitted[1], splitted[0]);

                nodes.add(server);
                t++;
            }

            else{
                String[] splitted = line.split(" -> ");
                String[] nodeAndDist = splitted[1].split(" ");
 
                Server server1 = nodes.stream()
                                      .filter(node -> node.getIpAddress().equals(splitted[0]))
                                      .findFirst()
                                      .orElse(null);

                Server server2 = nodes.stream() 
                                      .filter(node -> node.getIpAddress().equals(nodeAndDist[0]))
                                      .findFirst()
                                      .orElse(null);
                
                server1.addServerReachable(server2, Integer.valueOf(nodeAndDist[1]));
                server2.addServerReachable(server1, Integer.valueOf(nodeAndDist[1])); 
            }      
        }
        
        showAdjacencyList();
        reader.close();
    }

    public void getShortestPath(String startIp, String destIp){
        HashMap<Server, Integer> distance = new HashMap<>();
        HashMap<Server, Server> prev = new HashMap<>();
        HashMap<Server, Boolean> done = new HashMap<>();

        Server startServer =  nodes.stream() 
                                   .filter(node -> node.getIpAddress().equals(startIp))
                                   .findFirst()
                                   .orElse(null);
        
        Server destServer =  nodes.stream() 
                                 .filter(node -> node.getIpAddress().equals(destIp))
                                 .findFirst()
                                 .orElse(null);                           

        PriorityQueue<Server> queue = new PriorityQueue<>((a,b) -> distance.get(a) - distance.get(b));

        for(Server node : nodes){
            distance.put(node, Integer.MAX_VALUE);
            done.put(node,false);
            prev.put(node, null);
        }

        distance.replace(startServer, 0);
        done.replace(startServer, true);

        queue.add(startServer);

        while(!queue.isEmpty() && !done.get(destServer)){
            Server current = queue.remove();

            for(Server neighbor : current.getAllReachableServer()){
                if(!done.get(neighbor)){
                    if(distance.get(current) + current.getDistFromReachableServer(neighbor) < distance.get(neighbor) && neighbor.getIsUp()){
                        distance.replace(neighbor,distance.get(current) + current.getDistFromReachableServer(neighbor));
                        prev.replace(neighbor, current);
                    }

                    if(!queue.contains(neighbor) && neighbor.getIsUp())
                        queue.add(neighbor);
                }

                done.replace(current,true);
            }
        }


        System.out.printf("%s : %dms -> %s\n",destIp,distance.get(destServer),findPath(startServer, destServer, prev)); 
    }

    public String findPath(Server startServer, Server destServer, HashMap<Server, Server> prev){
        ArrayList<String> path = new ArrayList<>();

        Server current = destServer;

        while(!path.contains(startServer.getIpAddress())){
            path.addFirst(current.getIpAddress());
            current = prev.get(current);
        }

        return path.toString();
    }

    public void showAdjacencyList(){
        for(Server server : nodes){
            System.out.printf("%s -> [",server.getIpAddress());
            
            for(Server neigbor : server.getAllReachableServer())
                System.out.printf("{%s : %d},",neigbor.getIpAddress(),server.getDistFromReachableServer(neigbor));
            System.out.println("]");
        }
    }
}
