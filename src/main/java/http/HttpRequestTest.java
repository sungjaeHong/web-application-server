package http;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Test;

public class HttpRequestTest {

	private String testDirectory = "./src/test/resources/";
	
	@Test
	public void testHttpRequest() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetHeaders() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetParams() {
		fail("Not yet implemented");
	}
	
	@Test
	public void request_GET() throws FileNotFoundException{
		InputStream in = new FileInputStream(new File(testDirectory + "Http_GET.txt"));
		HttpRequest httpRequest = new HttpRequest(in);
		
		assertEquals(HttpMethod.GET, httpRequest.getMethod());
		assertEquals("/user/create", httpRequest.getPath());
		assertEquals("keep-alive", httpRequest.getHeader("Connection"));
		assertEquals("javajigi", httpRequest.getParameter("userId"));
		
	}
	
	@Test
	public void request_POST(){
		
	}

	@Test
	public void testGetPath() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMethod() {
		fail("Not yet implemented");
	}

}
