package org.phylowidget.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class PhyloTransformServices
{

	private static String replaceXmlChars(String s)
	{
//		s = s.replaceAll("&lt;", "<");
		s = s.replaceAll("&","&amp;");
		return s;
	}
	
//	public static String transformTree2(String urlString, String xmldata) throws Exception
//	{
//		xmldata = replaceXmlChars(xmldata);
//		
//		xmldata = "nexml="+xmldata;
//		//Create socket
//		URL url = new URL(urlString);
//		String hostname = url.getHost();
//		String path = url.getPath();
////	      String hostname = "www.pascalbotte.be";
//	      int port = 80;
//	      InetAddress  addr = InetAddress.getByName(hostname);
//	      Socket sock = new Socket(addr, port);
//				
//	      //Send header
////	      String path = "/rcx-ws/rcx";
//	      BufferedWriter  wr = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(),"UTF-8"));
//	      // You can use "UTF8" for compatibility with the Microsoft virtual machine.
//	      wr.write("POST " + path + " HTTP/1.0\r\n");
//	      wr.write("Host: "+hostname+"\r\n");
//	      wr.write("Accept: */*\r\n");
//	      wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
////	      wr.write("Content-Type: text/xml\r\n");
//	      wr.write("Content-Length: " + xmldata.length() + "\r\n");
//	      wr.write("\r\n");
//				
//	      //Send data
////	      System.out.println(xmldata);
//	      wr.write(xmldata);
//	      wr.flush();
//				
//	      // Response
//	      BufferedReader rd = new BufferedReader(new InputStreamReader(sock.getInputStream()));
//	      String line;
//	      StringBuffer sb = new StringBuffer();
//	      boolean areWeThereYet = false;
//	      while((line = rd.readLine()) != null)
//	      {
//	    	  if (areWeThereYet)
//	    	  {
//	    		  sb.append(line);
//	    	  }
//	    	  if (line.startsWith("Content-Type"))
//	    		  areWeThereYet = true;
//	      }
////	      System.out.println("RESPONSE:"+sb.toString());
//	      return sb.toString();
//	}
	
	public static String transformTree(String urlString, String nexml) throws Exception
	{
		// Construct data
		nexml = replaceXmlChars(nexml);
        String data = URLEncoder.encode("nexml", "UTF-8") + "=" + URLEncoder.encode(nexml, "UTF-8");
//        data += "&" + URLEncoder.encode("key2", "UTF-8") + "=" + URLEncoder.encode("value2", "UTF-8");
    
        // Send data
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(data);
        wr.flush();
    
        // Get the response
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        StringBuffer sb = new StringBuffer();
        while ((line = rd.readLine()) != null) {
        	sb.append(line);
        	sb.append("\n");
        }
        wr.close();
        rd.close();

        String s = sb.toString();
		return s;
	}
	
}
