package com.insta4j.instagram;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Logger;

import com.insta4j.instagram.enums.Relationship;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.insta4j.instagram.enums.HttpClientType;
import com.insta4j.instagram.exception.InstagramException;
import com.insta4j.instagram.http.APICallerFactory;
import com.insta4j.instagram.http.APICallerInterface;
import com.insta4j.instagram.util.Constants;

/**
 * This is the main Instagram class that will have methods which return instagram data as well as
 * publish data to instagram.
 * 
 * @author Nischal Shetty - nischalshetty85@gmail.com
 */
public class Instagram implements Serializable {

	private static final long serialVersionUID = 6396713915605509203L;

	Logger logger = Logger.getLogger(Instagram.class.getName());

	private OAuthAccessToken authAccessToken;

	private APICallerInterface caller = null;

	/**
	 * If only the access token is passed, then the Apache Http Client library is used for making http
	 * requests
	 * 
	 * @param authAccessToken
	 */
	public Instagram(OAuthAccessToken authAccessToken) {
		// apache http client is the default client type
		this(authAccessToken, HttpClientType.APACHE_HTTP_CLIENT);
	}

	public Instagram(OAuthAccessToken authAccessToken, HttpClientType clientType) {
		this.authAccessToken = authAccessToken;
		caller = APICallerFactory.getAPICallerInstance(clientType);
	}

	/**
	 * Returns a Instagram user's available info.
	 * 
	 * @param fbId
	 * @return
	 * @throws InstagramException
	 */
	public Map<String, Object> getUser(String fbId) throws InstagramException {
		NameValuePair[] nameValuePairs = { new BasicNameValuePair(Constants.PARAM_ACCESS_TOKEN, this.authAccessToken.getAccessToken()) };
		return pullData(Constants.INSTAGRAM_GRAPH_URL + "/" + "users" + "/" + fbId+"/", nameValuePairs);
	}

  public String relationship(String fbId, Relationship relationship) throws InstagramException {
    NameValuePair[] nameValuePairs = new NameValuePair[1];
    nameValuePairs[0] = new BasicNameValuePair(Constants.PARAM_ACTION, relationship.toString().toLowerCase());
    return postData(Constants.INSTAGRAM_GRAPH_URL + "/" + "users" + "/" + fbId+"/relationship?access_token=" + this.authAccessToken.getAccessToken(), nameValuePairs);
  }

  public Map<String, Object>  relationship(String fbId) throws InstagramException {
    NameValuePair[] nameValuePairs =  new NameValuePair[1];
    nameValuePairs[0] = new BasicNameValuePair(Constants.PARAM_ACCESS_TOKEN, this.authAccessToken.getAccessToken());
    return pullData(Constants.INSTAGRAM_GRAPH_URL + "/" + "users" + "/" + fbId+"/relationship", nameValuePairs);
  }

  /**
   * Get the list of users this user follows.
   *
   * @param fbId
   * @return
   * @throws InstagramException
   */
  public Map<String, Object> follows(String fbId, String cursor) throws InstagramException {
    NameValuePair[] nameValuePairs = null;
    if(cursor == null){
      nameValuePairs = new NameValuePair[1];
      nameValuePairs[0] = new BasicNameValuePair(Constants.PARAM_ACCESS_TOKEN, this.authAccessToken.getAccessToken());
    }else{
      nameValuePairs = new NameValuePair[2];
      nameValuePairs[0] = new BasicNameValuePair(Constants.PARAM_ACCESS_TOKEN, this.authAccessToken.getAccessToken());
      nameValuePairs[1] = new BasicNameValuePair(Constants.PARAM_CURSOR, cursor);
    }
    return pullData(Constants.INSTAGRAM_GRAPH_URL + "/" + "users" + "/" + fbId+"/follows", nameValuePairs);
  }

  /**
   * Get the list of users this user is followed by.
   *
   * @param fbId
   * @return
   * @throws InstagramException
   */
  public Map<String, Object> followedBy(String fbId, String cursor) throws InstagramException {
    NameValuePair[] nameValuePairs = null;
    if(cursor == null){
      nameValuePairs = new NameValuePair[1];
      nameValuePairs[0] = new BasicNameValuePair(Constants.PARAM_ACCESS_TOKEN, this.authAccessToken.getAccessToken());
    }else{
      nameValuePairs = new NameValuePair[2];
      nameValuePairs[0] = new BasicNameValuePair(Constants.PARAM_ACCESS_TOKEN, this.authAccessToken.getAccessToken());
      nameValuePairs[1] = new BasicNameValuePair(Constants.PARAM_CURSOR, cursor);
    }
    return pullData(Constants.INSTAGRAM_GRAPH_URL + "/" + "users" + "/" + fbId+"/followed-by", nameValuePairs);
  }
	
	public OAuthAccessToken getAuthAccessToken() {
		return authAccessToken;
	}

	/**
	 * Raw API method to pull any data in json form and transform it into the right object <br>
	 * An HTTP GET method is used here
	 * 
	 * @param url
	 * @param nameValuePairs Pass parameters that need to accompany the call
	 * @return
	 * @throws InstagramException
	 */
	public Map<String, Object> pullData(String url, NameValuePair[] nameValuePairs) throws InstagramException {
		// APICaller would retrieve the json string object from instagram by making a https call
		// Once the json string object is obtaind, it is passed to obj transformer and the right object
		// is retrieved
    return caller.getData(url, nameValuePairs);
	}

  /**
   * Raw API method to post any data in json form and transform it into the right object <br>
   * An HTTP POST method is used here
   *
   * @param url
   * @param nameValuePairs Pass parameters that need to accompany the call
   * @return
   * @throws InstagramException
   */
  public String postData(String url, NameValuePair[] nameValuePairs) throws InstagramException {
    // APICaller would retrieve the json string object from instagram by making a https call
    // Once the json string object is obtaind, it is passed to obj transformer and the right object
    // is retrieved
    return caller.postData(url, nameValuePairs);
  }
	
}