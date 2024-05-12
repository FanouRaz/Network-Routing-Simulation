package algo;

import java.io.Serializable;
import java.util.ArrayList;

public class Request implements Serializable{
    public String ipSource;
    public String ipDest;
    public ArrayList<String> requestHeader;

    public Request(String ipSource, String ipDest, ArrayList<String> header){
        this.ipSource = ipSource;
        this.ipDest = ipDest;
        this.requestHeader = header;
    }

    public String getIpSource() { return ipSource; }

    public String getIpDest() { return ipDest; }

    public ArrayList<String> getHeader() { return requestHeader; }

    public void setIpSource(String ipSource) { this.ipSource = ipSource; }

    public void setIpDest(String ipDest) { this.ipDest = ipDest; }

    public void setHeader(ArrayList<String> header) { this.requestHeader = header; }

    @Override
    public String toString(){
        return String.format("{'source' : '%s' , 'destination' : '%s' , 'header' : '%s'}", ipSource , ipDest , requestHeader);
    }
}
 