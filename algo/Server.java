package algo;

import java.util.HashMap;
import java.util.Set;

public class Server{
    private String ipAddress;
    private String domainName;
    private HashMap<Server, Integer> reachableServer;
    private boolean isUp;
    private String htmlPageContent;

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

    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public void setDomainName(String domainName) { this.domainName = domainName; }

    public void setIsUp(boolean isUp) { this.isUp = isUp; }

    public void addServerReachable(Server server, Integer ttl) { reachableServer.put(server,ttl); }

    public void removeServerReachable(Server server) { reachableServer.remove(server); }

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