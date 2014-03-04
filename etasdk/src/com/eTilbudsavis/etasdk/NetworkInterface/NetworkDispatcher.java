package com.eTilbudsavis.etasdk.NetworkInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import android.os.Process;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.SessionManager;
import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Method;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Header;
import com.eTilbudsavis.etasdk.Utils.Utils;

@SuppressWarnings("rawtypes")
public class NetworkDispatcher extends Thread {

	public static final String TAG = "NetworkDispatcher";
	
    /** Eta object controlling the whole lot */
    private final Eta mEta;
    
    /** The queue of requests to service. */
	private final BlockingQueue<Request> mQueue;
	
	/** The RequestQueue this NetworkDispatcher receives Requests from */
	private final RequestQueue mRequestQueue;
	
    /** The network interface for processing requests. */
    private final Network mNetwork;
    
    /** The cache to write to. */
    private final Cache mCache;
    
    /** For posting responses and errors. */
    private final Delivery mDelivery;
    
    /** Used for telling us to die. */
    private volatile boolean mQuit = false;
    
    public NetworkDispatcher(Eta eta, RequestQueue requestQueue, BlockingQueue<Request> queue, Network network, Cache cache, Delivery delivery) {
        mQueue = queue;
        mNetwork = network;
        mCache = cache;
        mDelivery = delivery;
        mRequestQueue = requestQueue;
        mEta = eta;
    }

	/**
	 * Terminate this NetworkDispatcher. Once terminated, no further requests will be processed. 
	 */
    public void quit() {
    	mQuit = true;
    }
    
	@Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        Request request;
        while (true) {
            try {
                // Take a request from the queue.
                request = mQueue.take();
            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (mQuit) {
                    return;
                }
                continue;
            }

            try {
            	
                // If the request was cancelled already, do not perform the network request.
                if (request.isCanceled()) {
                	request.finish("network-dispatcher-cancelled-on-recieved");
                    continue;
                } else {
                	request.addEvent("recieved-by-network-dispatcher");
                }
                
                prepare(request);
                
                // Perform the network request.
                NetworkResponse networkResponse = mNetwork.performRequest(request);

                request.addEvent("parsing-network-response");
                Response<?> response = request.parseNetworkResponse(networkResponse);
                
    			if (response.isSuccess()) {
    				
    				updateSessionInfo(networkResponse.headers);
                    mCache.put(request, response);
                    mDelivery.postResponse(request, response);
                    
    			} else {
    				
                	if (SessionManager.recoverableError(response.error)) {
                		
                    	request.addEvent("recoverable-session-error");
                    	
                		if (isSessionEndpoint(request)) {
                			
                			mDelivery.postResponse(request, response);
                			
                		} else {
                			
                    		// Query the session manager to perform an update
                			if (mEta.getSessionManager().recover(response.error)) {
                    			mRequestQueue.add(request);
                			} else {
                    			mDelivery.postResponse(request, response);
                			}
                    		
                		}
                		
                	} else {
                		
                    	request.addEvent("non-recoverable-error");
            			
                    	mDelivery.postResponse(request, response);
            			
                	}
                	
    			}
    			
                
            } catch (EtaError e) {
            	
            	request.addEvent("network-error");
            	EtaLog.d(TAG, e);
                mDelivery.postResponse(request, Response.fromError(e));
                
            }
        }
    }
	
	/**
	 * Wrapper to check for session endpoint
	 * @param request to check
	 * @return true if session-endpoint, eler false 
	 */
	private boolean isSessionEndpoint(Request request) {
		return request.getUrl().contains(Endpoint.SESSIONS);
	}
	
	/**
	 *  If it's a post to sessions, it's to create a new Session, then the API key is needed.
	 *  In any other case, just set the headers, with the current session token and signature.
	 * @param request
	 */
	private void prepare(Request<?> request) {
		
		request.addEvent("preparing-headers");
		
		boolean newSession = (request.getMethod() == Method.POST && request.getUrl().contains(Endpoint.SESSIONS));
		
        if (!newSession) {
        	
        	Map<String, String> headers = new HashMap<String, String>();
        	String token = mEta.getSessionManager().getSession().getToken();
        	headers.put(Header.X_TOKEN, token);
        	String sha256 = Utils.generateSHA256(mEta.getApiSecret() + token);
        	headers.put(Header.X_SIGNATURE, sha256);
        	request.setHeaders(headers);
        	
        }
        
	}
	
	/**
	 * Method checks headers to find X-Token and X-Token-Expires.<br>
	 * If they do not exist, nothing happens as the call has a wrong endpoint, or other
	 * non-API regarding error. If they do exist, then they are checked by the Session
	 * to find out if there are any changes.
	 * @param headers to check for new token.
	 */
	private void updateSessionInfo(Map<String, String> headers) {
		String token = headers.get(Header.X_TOKEN);
	    String expire = headers.get(Header.X_TOKEN_EXPIRES);
	    
	    if ( !(token == null || expire == null) ) {
	    	mEta.getSessionManager().updateTokens(token, expire);
	    }
	    
	}
	
}
