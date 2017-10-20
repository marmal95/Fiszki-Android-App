package fiszki.xyz.fiszkiapp.source;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class RequestBuilder {
    private Map<String, String> requestParameters;

    public RequestBuilder() {
        requestParameters = new LinkedHashMap<>();
    }

    public void putParameter(String key, String value){
        requestParameters.put(key, value);
    }

    public String buildRequest() {
        String request = "";

        Iterator<Map.Entry<String, String>> iterator = requestParameters.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, String> entry = iterator.next();
            request += entry.getKey() + "=" + entry.getValue();

            if(iterator.hasNext())
                request += "&";
        }

        return request;
    }
}
