package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;


public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	private Socket connection;
	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		//log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());
		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			// TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
			DataOutputStream dos = new DataOutputStream(out);

			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(isr);

			byte[] body = null;
			String line = br.readLine();
			String url = HttpRequestUtils.getURL(line);
			String firstHeader = line;
			Map<String, String> header = new HashMap<String,String>();
			while(!"".equals(line)){
				if(line==null){	log.debug("null");return;	}
				line=br.readLine();
				String[] headerToken = line.split(":");
				if(headerToken.length==2)	{
					header.put(headerToken[0],headerToken[1].trim());
				}
			}

			if(url.startsWith("/user/create")){						//회원가입
				String requestBody = IOUtils.readData(br, Integer.parseInt(header.get("Content-Length")));
				Map<String, String> paramMap = HttpRequestUtils.parseQueryString(requestBody);
				User user = new User(paramMap.get("userId"),paramMap.get("password"),paramMap.get("name"),paramMap.get("email"));
				log.debug("addUser");
				DataBase.addUser(user);
				url = "/index.html";
				response302Header(dos, url);
			} else if(url.equals("/user/login")){					//로그인

				String requestBody = IOUtils.readData(br, Integer.parseInt(header.get("Content-Length")));
				Map<String, String> paramMap = HttpRequestUtils.parseQueryString(requestBody);
				User loginUser = DataBase.getUser(paramMap.get("userId"));
				if(loginUser==null ){		//ID가 없을 때
					log.debug("Login Error! "+paramMap.get("userId")+"{}",firstHeader);
					response302Header(dos, "/user/login_failed.html");
				}
				if(loginUser!=null && loginUser.getPassword().equals(paramMap.get("password"))){
					setCookies(dos, "/index.html");
				} else	{
					response302Header(dos, "/user/login_failed.html");
				}


			} else if(url.startsWith("/user/list")){
				body = Files.readAllBytes(new File("./webapp"+url).toPath());
				StringBuilder sb = new StringBuilder();
				Collection<User> c = DataBase.findAll();
				log.debug("user : "+c.size());
				sb.append("<table>");
				for(User user : c){
					sb.append("<tr>");
					sb.append("<td>"+user.getUserId()+"</td>");
					sb.append("<td>"+user.getName()+"</td>");
					sb.append("<td>"+user.getEmail()+"</td>");
					sb.append("</tr>");
				}
				sb.append("</table>");
				byte[] table = sb.toString().getBytes();
				byte[] destination = new byte[body.length+table.length];
				System.arraycopy(body, 0, destination, 0, body.length);
				System.arraycopy(table, 0, destination, body.length, table.length);

				response200Header(dos, destination.length);
				responseBody(dos, destination);
			}
			else if(url.endsWith(".css")){
				body = Files.readAllBytes(new File("./webapp"+url).toPath());
				responseCss(dos, body.length);
			}
			else	{
				body = Files.readAllBytes(new File("./webapp"+url).toPath());
				response200Header(dos, body.length);
				responseBody(dos, body);
			}



			//
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	private void responseCss(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	private void setCookies(DataOutputStream dos, String url) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			dos.writeBytes("Location: "+url+"\r\n");
			dos.writeBytes("Set-Cookie: logined=true\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	private void response302Header(DataOutputStream dos, String url) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			dos.writeBytes("Location: "+url+"\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
