package com.eTilbudsavis.sdkdemo;

import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.eTilbudsavis.etasdk.Api;
import com.eTilbudsavis.etasdk.Api.ListListener;
import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.Pageflip;
import com.eTilbudsavis.etasdk.Pageflip.PageflipListener;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.Utils.Utils;
import com.etilbudsavis.sdkdemo.R;

public class CatalogViewer extends Activity {

	public static final String TAG = "CatalogViewer";
	Eta mEta;
	Pageflip mPageflip;
	ProgressDialog mPd;
	// Pageflip viewer hack
	String mViewSession = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalog_viewer);
        
        //Create a new instance of Eta
        mEta = new Eta(Keys.API_KEY, Keys.API_SECRET, this);
        
        /* Enable debug mode, so debug info will show in LogCat
         * You might not want to have this set to true in a release version. */
        mEta.debug(true);
        
        // Set the location (This could also be set via LocationManager)
        mEta.getLocation().setLatitude(55.63105);
        mEta.getLocation().setLongitude(12.5766);
        mEta.getLocation().setRadius(700000);
        mEta.getLocation().setSensor(false);

		mPageflip = (Pageflip)findViewById(R.id.pageflip);
		/* The view session hack, to fix a problem of redrawing the WebView
		 * in a Fragment, that has had a onDestroyView() */
		mPageflip.setViewSession(mViewSession);
		
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	mEta.onResume();
    	mPd = ProgressDialog.show(CatalogViewer.this, "", "Getting catalogs...", true, true);
		mEta.getCatalogList(catalogListener).execute();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	mEta.onPause();
    }
    
	// A catalogs listener, 
	ListListener<Catalog> catalogListener = new ListListener<Catalog>() {
		
		@Override
		public void onComplete(boolean isCache, int statusCode, List<Catalog> list, EtaError error) {

			mPd.dismiss();

			/* If the request is a success and one or more catalogs is returned,
			 * show the first catalog in a pageflip. */
			if (Utils.isSuccess(statusCode) && !list.isEmpty()) {
				
				mPd = ProgressDialog.show(CatalogViewer.this, "", "Loading catalog into pageflip...", true, true);
				
		        mPageflip.execute(mEta, pfl, list.get(0).getId());
		        
			} else {
				
				Utils.logd(TAG, error.toString());
				
			}
		}
	};
	
	// Pageflip listener, triggered on callbacks from the pageflip.
    PageflipListener pfl = new PageflipListener() {
		
		@Override
		public void onEvent(String event, String uuid, JSONObject object) {
			Toast.makeText(getApplicationContext(), event, Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onReady(String uuid, String viewSession) {
			// Remember to set the viewSession variable, first chance you get.
			mViewSession = viewSession;
			mPd.dismiss();
		}

	};
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

    menu.add(Menu.NONE, 0, 0, "Sideoversigt");

    return super.onCreateOptionsMenu(menu); 
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

    	switch (item.getItemId()) {

    	case 0:
    		mPageflip.toggleThumbnails();
    		break;

    	default:
    		break;

    	}
    	return super.onOptionsItemSelected(item);
    }
    
}
