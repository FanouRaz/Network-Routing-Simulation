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
    //+ "	fill-color: white, #EEEEEE;"
    + "	padding: 60px; "
    + "}"
    + ""	
    + "node {"
    + " size: 50px;"
    + " fill-mode: plain;"
    + " fill-color: #CCCC;"
    + " text-alignment: under;"
    + " text-color: white;"
    + " text-style: bold;"
    + " text-background-mode: rounded-box;"
    + " text-background-color: #222C;"
    + " text-padding: 1.5px;"
    + " text-offset: 0px, 2px;"
    + "}"
    + "node.up {"
    + " fill-mode: image-scaled;"
    + " fill-image: url('assets/server-opened.png');"
    + "}"
    + ""
    + "node.down {"
    + " fill-mode: image-scaled;"
    + " fill-image: url('assets/server-closed.png');"
    + "}"
    + ""
    + "node:clicked { "
    + "	stroke-mode: plain;"
    + "	stroke-color: red;"
    + "}"
    + ""
    + "edge.marked {" 
    + " fill-color: green;" 
    + " fill-mode: plain;"
    + "}"
    + ""
    + "edge { "
    + " shape: line;"
    + " size: 1px;"
    + " text-size: 10px;"
    + " text-alignment: above;"
    + " fill-color: grey;"
    + " fill-mode: plain;"
    + "}";
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

    /*
     * Retourne le chemin le plus court partant de startIp vers destIp
     */
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

        if(destServer != null){
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

        else {
            System.out.println("Le serveur que vous recherchez n'existe pas");
            return new ArrayList<>();
        }
    }

    /*
     * Recherche le chemin optimal partant de startIp vers une liste de destinations
     */
    public ArrayList<String> getShortestPath(String startIp, ArrayList<String> destinations){
        ArrayList<String> shortestPath = getShortestPath(startIp, destinations.get(0));

        for(int i=1; i<destinations.size(); i++){
            ArrayList<String> temp = getShortestPath(startIp, destinations.get(i));
            
            if(getPathLength(temp) < getPathLength(shortestPath))
                shortestPath = temp;
        }
           

        return shortestPath;
    }

    /*
     * Retrace le chemin partant de startServer vers destServer
     */
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

    /*
     * Retourne la longueur du chemin proposer
     */
    public int getPathLength(ArrayList<String> path){
        int len = 0;

        for(int i=0; i<path.size()-1; i++){
            Server s1 = findServerByIpAdress(path.get(i)).orElse(null);
            Server s2 = findServerByIpAdress(path.get(i+1)).orElse(null);

            len += s2.getDistFromReachableServer(s1);
        }

        return len;
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
