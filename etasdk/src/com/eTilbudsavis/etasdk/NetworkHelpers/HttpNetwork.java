package com.eTilbudsavis.etasdk.NetworkHelpers;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.DefaultHttpRoutePlanner;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.ByteArrayBuffer;

import com.eTilbudsavis.etasdk.NetworkInterface.Network;
import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.Utils.EtaLog;

public class HttpNetwork implements Network {

	public static final String TAG = "HttpNetwork";
	/**
	 * Default connection timeout, this is for both connection and socket
	 */
	private static final int CONNECTION_TIME_OUT = 10000;
	private static final int BUFFER_SIZE = 0x1000; // 4K
	
	public NetworkResponse performRequest(Request<?> request) throws EtaError {

		HttpResponse resp = null;
		int sc = 0;
		byte[] content = null;
		Map<String, String> responseHeaders = new HashMap<String, String>();
		try {

			resp = performHttpRequest(request);
			sc = resp.getStatusLine().getStatusCode();
			content = resp.getEntity() == null ? new byte[0] : entityToBytes(resp.getEntity());
			
			if (!isSuccess(sc)) {
				throw new IOException();
			}
			
			return new NetworkResponse(sc, content, responseHeaders);
			
		} catch (ClientProtocolException e) {
			EtaLog.d(TAG, e);
		} catch (IOException e) {
			EtaLog.d(TAG, e);
			if (resp == null || content == null) {
				throw new NetworkError(e);
			} else {
				sc = resp.getStatusLine().getStatusCode();
				NetworkResponse r = new NetworkResponse(sc, content, responseHeaders);
				EtaError er = new EtaError(r);
				if (isClientError(sc) || isServerError(sc)) {
					if (er.getCode() == 1101 ||
							er.getCode() == 1104 || 
							er.getCode() == 1108 || 
							er.getCode() == 1300 || 
							er.getCode() == 1301) {
						throw new SessionError(r);
					} 
				}
				throw er;
			}
		}

		return null;
	}

	private HttpResponse performHttpRequest(Request<?> request) throws ClientProtocolException, IOException {

		// Start the interwebs work stuff
		DefaultHttpClient httpClient = new DefaultHttpClient();

		setHostNameVerifierAndRoutePlanner(httpClient);

		// Set timeouts
		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), CONNECTION_TIME_OUT);
		HttpConnectionParams.setSoTimeout(httpClient.getParams(), CONNECTION_TIME_OUT);

		// Get the right request type, and set body if necessary
		HttpRequestBase httpRequest = createRequest(request);
		setHeaders(request, httpRequest);

		return httpClient.execute(httpRequest);
	}

	private void setHostNameVerifierAndRoutePlanner(DefaultHttpClient httpClient) {

		// Use custom HostVerifier to accept our wildcard SSL Certificates: *.etilbudsavis.dk
		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

		SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
		socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		registry.register(new Scheme("https", socketFactory, 443));
		SingleClientConnManager mgr = new SingleClientConnManager(httpClient.getParams(), registry);

		httpClient = new DefaultHttpClient(mgr, httpClient.getParams());

		// Change RoutePlanner to avoid SchemeRegistry causing IllegalStateException.
		// Some devices with faults in their default route planner
		httpClient.setRoutePlanner(new DefaultHttpRoutePlanner(registry));

		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

	}

	private HttpRequestBase createRequest(Request<?> request) {

		switch (request.getMethod()) {
		case Request.Method.POST: 
			HttpPost post = new HttpPost(request.getUrl());
			setEntity(post, request);
			return post;

		case Request.Method.GET:
			HttpGet get = new HttpGet(request.getUrl());
			return get;

		case Request.Method.PUT:
			HttpPut put = new HttpPut(request.getUrl());
			setEntity(put, request);
			return put;

		case Request.Method.DELETE:
			HttpDelete delete = new HttpDelete(request.getUrl());
			return delete;

		default:
			return null;
		}

	}

	private static void setEntity(HttpEntityEnclosingRequestBase httpRequest, Request<?> request) {
		byte[] body = request.getBody();
		if (body != null) {
			HttpEntity entity = new ByteArrayEntity(body);
			httpRequest.setEntity(entity);
			httpRequest.setHeader(Request.Header.CONTENT_TYPE, request.getBodyContentType());
		}
	}


	@SuppressWarnings("rawtypes")
	private void setHeaders(Request request, HttpRequestBase http) {
		HashMap<String, String> headers = new HashMap<String, String>(request.getHeaders().size());
		headers.putAll(request.getHeaders());
		for(String key : headers.keySet())
			http.setHeader(key, headers.get(key));
	}


	private static byte[] entityToBytes(HttpEntity entity) {
		ByteArrayBuffer bytes = new ByteArrayBuffer((int)entity.getContentLength());
		try {
			byte[] buf = new byte[BUFFER_SIZE];
			int c = -1;
			while (( c = entity.getContent().read(buf)) != -1) {
				bytes.append(buf, 0, c);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytes.toByteArray();
	}


	public static boolean isSuccess(int statusCode) {
		return 200 <= statusCode && statusCode < 300 || statusCode == 304;
	}

	public static boolean isClientError(int statusCode) {
		return 400 <= statusCode && statusCode < 500;
	}

	public static boolean isServerError(int statusCode) {
		return 500 < statusCode;
	}
	
}
