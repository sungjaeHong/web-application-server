package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.IOUtils;
import webserver.RequestHandler;

public class HttpRequest {
	
	private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
	
	InputStreamReader isr;
	BufferedReader br;
	Map<String, String> headers = new HashMap<String,String>();
	Map<String, String> paramMap = new HashMap<String,String>();
	String line, methodLine;
	
	public HttpRequest(InputStream is){
		try {
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			
			line = br.readLine();
			methodLine = line;
			
			
			while(!line.equals("")){
				if(line==null)	return;
				
				if(line.split(": ").length==2)
					headers.put(line.split(": ")[0], line.split(": ")[1]);
				line = br.readLine();
			}
			
			if(methodLine.split(" ")[0].equals("GET")){
				//GET
				String parameters = methodLine.substring(methodLine.indexOf("?")+1);
				Map<String, String> paramMap = HttpRequestUtils.parseQueryString(parameters);
			} else {
				//POST
				String requestBody = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
				Map<String, String> paramMap = HttpRequestUtils.parseQueryString(requestBody);
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.debug("io error : {}",e.getMessage());
		}


	}
	
	public String getHeaders(String keyName){
		return headers.get(keyName);
	}
	
	public String getParams(String keyName){
		return paramMap.get(keyName);
	}
	
	public String getPath(){
		return methodLine.split(" ")[1];
	}
	
	public String getMethod(){
		return methodLine.split(" ")[0];
	}
}
