package algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.io.IOException;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Server{
    private String ipAddress;
    private String domainName;
    private HashMap<Server, Integer> reachableServer;
    private boolean isUp;
    private String htmlPageContent;
    private Socket socket;
    private OutputStream out;
    private InputStream in;


    public Server(String ipAddress, String domainName){
        this.ipAddress = ipAddress;
        this.domainName = domainName;
        this.isUp = true;
        this.reachableServer = new HashMap<>();
        this.htmlPageContent =
            "<!DOCTYPE html>"
            +"<html>"
            +  "<head>"
            +     "<title>"+domainName+"</title>"
            +  "</head>"
            +  "<body>"
            +     "<p>Welcome to <strong>"+domainName+"</strong> page!</p>"
            +  "</body>"
            +"</html>" ; 
        
        try{
            this.socket = new Socket("127.0.0.1",GraphServer.SOCKET_SERVER_PORT);
            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();

            //Informer le serveurSocket de l'adresse IP de ce serveur 
            new PrintWriter(out,true).println(ipAddress);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public String getIpAddress(){ return ipAddress; }

    public String getDomainName() { return domainName; }

    public String getHtmlPageContent() { return htmlPageContent; }

    public void setHtmlPageContent(String htmlDoc) { htmlPageContent = htmlDoc; }

    public boolean getIsUp() { return isUp; }

    public boolean canReach(Server server){
        System.out.printf("%s %s %s with a ttl of %d\n",this.getIpAddress(), reachableServer.containsKey(server) ? "can reach": "can't reach",server.getIpAddress(), reachableServer.get(server));
        return reachableServer.containsKey(server);
    }

    public Set<Server> getAllReachableServer(){
        return reachableServer.keySet();
    }

    public Integer getDistFromReachableServer(Server server){  
        if(server.equals(this))
            return 0;
        else if(!reachableServer.containsKey(server))
            return Integer.MAX_VALUE;
            
        return reachableServer.get(server);
    }

    public InputStream getInputStream() { return in; }

    public OutputStream getOutputStream() { return out; }

    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public void setDomainName(String domainName) { this.domainName = domainName; }

    public void setIsUp(boolean isUp) { this.isUp = isUp; }

    public void addServerReachable(Server server, Integer ttl) { reachableServer.put(server,ttl); }

    public void removeServerReachable(Server server) { reachableServer.remove(server); }

    public void sendRequest(Server destServer,ArrayList<String> path){
        try{
            ObjectOutputStream destOut = new ObjectOutputStream(destServer.getOutputStream());
            
            destOut.writeObject(new Request(ipAddress, destServer.getIpAddress(),path));
            destOut.flush();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public void stop(){
        try{
            in.close();
            out.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public static boolean isIpAdress(String str){
        if(str.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")){
            String[] bytes = str.split("\\.");

            for(String bit : bytes){
                if(Integer.valueOf(bit) < 0 || Integer.valueOf(bit) > 255)
                    return false;
            }

            return true;
        }
        return false;
    }

    @Override
    public int hashCode(){
        return ipAddress.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Server)
            return ipAddress.equals(((Server)o).getIpAddress()) && domainName.equals(((Server)o).getDomainName());
        else if(o == null)
            return this == null;
        else 
            return false;
    }

    @Override
    public String toString(){
        String str = String.format("{\n\t'ipAddress': %s,\n\t'domainName': %s,\n\t'isUp': %s,", ipAddress, domainName,isUp);
        
        str += "\n\t'reachableServer': [";

        for(Server server : reachableServer.keySet())
            str += server.getIpAddress() + ",";
        
        str += "]\n}";

        return str;
    }
}