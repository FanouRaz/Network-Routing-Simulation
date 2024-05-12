package algo;

import java.io.Serializable;
import java.util.ArrayList;

public class Response implements Serializable{
    private String ipSource;
    private String ipDest;
    private String responseBody;
    private ArrayList<String> responseHeader;
    
    public Response(String ipSource, String ipDest, ArrayList<String> responseHeader, String responseBody){
        this.ipSource = ipSource;
        this.ipSource = ipSource;
        this.ipDest = ipDest;
        this.responseHeader = responseHeader;
        this.responseBody = responseBody;
    }

    public String getIpSource() { return ipSource; }

    public String getIpDest() { return ipDest; }

    public ArrayList<String> getHeader() { return responseHeader; }

    public String getResponseBody() { return responseBody; }

    public void setIpSource(String ipSource) { this.ipSource = ipSource; }

    public void setIpDest(String ipDest) { this.ipDest = ipDest; }

    public void setResponseHeader(ArrayList<String> header) { this.responseHeader = header; }

    public void setResponseBody(String content)  { this.responseBody = content; }
    
    @Override
    public String toString(){
        return String.format("{'source' : '%s' , 'destination' : '%s' , 'header' : '%s', 'body' : '%s'}", ipSource , ipDest , responseHeader , responseBody);
    }
}
