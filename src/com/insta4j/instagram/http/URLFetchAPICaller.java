package com.insta4j.instagram.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import com.insta4j.instagram.InstaProp;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.insta4j.instagram.exception.InstagramException;
import com.insta4j.instagram.util.JSONToObjectTransformer;

public class URLFetchAPICaller implements APICallerInterface {

	public Map<String, Object> getData(String url, NameValuePair[] nameValuePairs) throws InstagramException {

		URLFetchService fetchService = URLFetchServiceFactory.getURLFetchService();
		URL fetchURL = null;

		HTTPResponse response = null;
		String responseString = null;
		String constructedParams = null;



			if (nameValuePairs != null) {
				constructedParams = constructParams(nameValuePairs);

				if (url.contains("?")) {
					url = url.concat("&" + constructedParams);
				} else {
					url = url.concat("?" + constructedParams);
				}
			}

      int retry = Integer.parseInt(InstaProp.get("NETWORK_FAILURE_RETRY_COUNT"));
      while (retry > 0){
        try {
          fetchURL = new URL(url);
          response = fetchService.fetch(fetchURL);
          break;
        } catch (IOException ex) {
          retry --;
          if(retry <= 0){
            throw new InstagramException("IO Exception while calling facebook!", ex);
          }
        }
      }

			int statusCode = response.getResponseCode();
			if (statusCode != HttpStatus.SC_OK) {
				// InstagramError error = new InstagramError(statusCode,
				// "I guess you are not permitted to access this url. HTTP status code:"+statusCode, null);
				responseString = new String(response.getContent());
				throw new InstagramException(JSONToObjectTransformer.getError(responseString, statusCode));
			}
			responseString = new String(response.getContent());


		// if response string contains accessToken=xxx remove it!
		// responseString = Util.replaceAccessToken(responseString, nameValuePairs);

			ObjectMapper mapper = new ObjectMapper();
	    Map<String, Object> responseMap = null;
	    try {
				responseMap = mapper.readValue(responseString, Map.class);
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}    
	    
    return responseMap;
	}

	/**
	 * @param url
	 * @param nameValuePairs
	 * @return
	 * @throws InstagramException
	 */
	public String postData(String url, NameValuePair[] nameValuePairs) throws InstagramException {

		String content = null;
		String constructedParams = null;
		int statusCode = 0;
		HttpURLConnection connection = null;
    int retry = Integer.parseInt(InstaProp.get("NETWORK_FAILURE_RETRY_COUNT"));
    while (retry > 0){
      try {
        URL posturl = new URL(url);
        connection = (HttpURLConnection) posturl.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        // connection.setConnectTimeout(10000);
        // connection.setReadTimeout(10000);

        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());

        constructedParams = constructParams(nameValuePairs);

        writer.write(constructedParams);
        writer.close();

        statusCode = connection.getResponseCode();
        if (statusCode != HttpURLConnection.HTTP_OK) {
          // "I guess you are not permitted to access this url. HTTP status code:"+statusCode, null);
          content = getResponse(connection);
          throw new InstagramException(JSONToObjectTransformer.getError(content, statusCode));
        } else {
          content = getResponse(connection);
        }
        break;
      } catch (MalformedURLException e) {
        throw new InstagramException("Malformed URL Exception while calling Instagram!", e);
      } catch (IOException e) {
          retry --;
          if(retry <= 0){
            throw new InstagramException("IO Exception while calling facebook!", e);
          }
          if (connection != null) {
            connection.disconnect();
            connection = null;
          }
      } finally {
        if (connection != null) {
          connection.disconnect();
        }
      }
    }

    return content;

	}

	private String getResponse(HttpURLConnection connection) throws IOException {
		String content;
		// Get Response
		InputStream is = connection.getInputStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line;
		StringBuilder response = new StringBuilder();
		while ((line = rd.readLine()) != null) {
			response.append(line);
			response.append('\r');
		}
		rd.close();
		content = response.toString();
		return content;
	}

	/*
	 * public String deleteData(String url, NameValuePair[] nameValuePairs) throws InstagramException {
	 * 
	 * String content = null; String constructedParams = null; int statusCode = 0; HttpURLConnection
	 * connection = null; try {
	 * 
	 * constructedParams = constructParams(nameValuePairs);
	 * 
	 * 
	 * 
	 * URL posturl = new URL(url+"/?"+constructedParams); connection = (HttpURLConnection)
	 * posturl.openConnection(); connection.setRequestProperty( "Content-Type",
	 * "application/x-www-form-urlencoded" ); connection.setDoOutput(true);
	 * connection.setRequestMethod("DELETE"); // connection.setConnectTimeout(10000); //
	 * connection.setReadTimeout(10000);
	 * 
	 * //connection.connect();
	 * 
	 * //System.out.println(connection.getContent());
	 * 
	 * OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
	 * 
	 * writer.write(""); writer.close();
	 * 
	 * statusCode = connection.getResponseCode(); if (statusCode != HttpURLConnection.HTTP_OK) {
	 * content = getResponse(connection); throw new
	 * InstagramException(JSONToObjectTransformer.getError(content, statusCode));
	 * 
	 * } else { content = getResponse(connection);
	 * 
	 * } } catch (MalformedURLException e) { throw new
	 * InstagramException("Malformed URL Exception while calling Instagram!", e); } catch (IOException
	 * e) { throw new InstagramException("IOException while calling Instagram!", e); } finally { if
	 * (connection != null) { connection.disconnect(); } }
	 * 
	 * return content;
	 * 
	 * }
	 */

	public String deleteData(String url, NameValuePair[] nameValuePairs) throws InstagramException {
		String content = null;
		String constructedParams = null;
		int statusCode = 0;

		URLFetchService fetchService = URLFetchServiceFactory.getURLFetchService();
		URL posturl = null;
		constructedParams = constructParams(nameValuePairs);

		try {
			posturl = new URL(url + "?" + constructedParams);
		} catch (MalformedURLException e) {
		}

		try {
			HTTPResponse response = fetchService.fetch(new HTTPRequest(posturl, HTTPMethod.DELETE));

			statusCode = response.getResponseCode();

			if (statusCode != HttpURLConnection.HTTP_OK) {
				content = new String(response.getContent());
				throw new InstagramException(JSONToObjectTransformer.getError(content, statusCode));
			} else {
				content = new String(response.getContent());
			}

		} catch (IOException e) {
		}

		return content;
	}

	private String constructParams(NameValuePair[] nameValuePairs) {

		StringBuilder builder = null;
		String constructedParams = null;

		for (NameValuePair nameValuePair : nameValuePairs) {
			if (nameValuePair != null && nameValuePair.getName() != null && nameValuePair.getValue() != null) {
				if (builder != null) {
					try {
						builder.append("&" + nameValuePair.getName() + "=" + URLEncoder.encode(nameValuePair.getValue(), "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						// TODO: Catch error
					}
				} else {
					builder = new StringBuilder();
					try {
						builder.append(nameValuePair.getName() + "=" + URLEncoder.encode(nameValuePair.getValue(), "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						// TODO: Catch error
					}
				}
			}
		}

		if (builder != null) {
			constructedParams = builder.toString();
		}

		return constructedParams;
	}

}