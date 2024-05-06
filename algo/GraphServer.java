package algo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.PriorityQueue;

public class GraphServer {
    public static String styleSheet = ""
    + "graph {"
    + "	canvas-color: white;  "
    + "	fill-mode: gradient-radial; "
   // + "	fill-color: white, #EEEEEE;"
    + "	padding: 60px; "
    + "}"
    + ""	
    + "node {"
    + " size: 30px;"
    + " fill-mode: plain;"
    + " fill-color: #CCCC;"
    + " fill-mode: image-scaled;"
    + " fill-image: url('assets/server.png');"
    + " text-alignment: under;"
    + " text-color: white;"
    + " text-style: bold;"
    + " text-background-mode: rounded-box;"
    + " text-background-color: #222C;"
    + " text-padding: 1.5px;"
    + " text-offset: 0px, 2px;"
    + "}"
    + ""
    + "node:clicked { "
    + "	stroke-mode: plain;"
    + "	stroke-color: red;"
    + "}"
    + ""
    + "node:selected { "
    + "	stroke-mode: plain; "
    + "	stroke-color: blue; "
    + "}"
    + ""
    + "edge.marked {" 
    + " fill-color: green;" 
    + " fill-mode: plain;"
    + "}"
    + ""
    + "edge { 	shape: line; size: 1px; fill-color: grey; 	fill-mode: plain; } ";
   
    public static final String URL_IMAGE = "assets/server.png";

    private ArrayList<Server> nodes; 

    public GraphServer(ArrayList<Server> nodes){
        this.nodes = nodes;
    }
    
    public GraphServer(String filepath) throws IOException{
        if(filepath != null){

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
        else 
            this.nodes = new ArrayList<>();
    }

    public ArrayList<String> getShortestPath(String startIp, String destIp){
        HashMap<Server, Integer> distance = new HashMap<>();
        HashMap<Server, Server> prev = new HashMap<>();
        HashMap<Server, Boolean> done = new HashMap<>();

        Server startServer = findServerByIpAdress(startIp)
                                .orElse(null);
        
        Server destServer =  findServerByIpAdress(destIp)
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


        if(prev.get(findServerByIpAdress(destIp).orElse(null)) == null && !destIp.equals(startIp)){
            System.out.printf("Pas de chemin allant de %s vers %s", startIp, destIp);
            return new ArrayList<>();
        }

        else{
            ArrayList<String> path = findPath(startServer, destServer, prev);
            System.out.printf("%s : %dms -> %s\n",destIp,distance.get(destServer),path); 
    
            return path;
        }
    }

    public ArrayList<String> findPath(Server startServer, Server destServer, HashMap<Server, Server> prev){
        ArrayList<String> path = new ArrayList<>();

        System.out.println(prev.get(destServer) == null ? "No way" : "");
        Server current = destServer;

        while(!path.contains(startServer.getIpAddress())){
            path.addFirst(current.getIpAddress());
            current = prev.get(current);
        }

        return path;
    }

    public void showAdjacencyList(){
        for(Server server : nodes){
            System.out.printf("%s -> [",server.getIpAddress());
            
            for(Server neigbor : server.getAllReachableServer())
            System.out.printf("{%s : %d},",neigbor.getIpAddress(),server.getDistFromReachableServer(neigbor));
            System.out.println("]");
        }
    }
    
    public ArrayList<Server> getNodes(){ return nodes; }

    public Server get(int i){ return nodes.get(i); }

    public void addNode(Server server) { nodes.add(server); }

    public int size(){ return nodes.size(); }

    public boolean containsServerByIpAdress(String ipAdress) {
        return nodes.stream()
                    .anyMatch(server -> server.getIpAddress()
                    .equals(ipAdress));
    }

    public Optional<Server> findServerByIpAdress(String ip){
        return nodes.stream()
                    .filter(elt -> elt.getIpAddress().equals(ip))
                    .findFirst();
    }
}
