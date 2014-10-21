package com.eTilbudsavis.etasdk.pageflip;

import java.util.Arrays;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.Log.EtaLog;

public class PageflipUtils {
	
	public static final String TAG = PageflipUtils.class.getSimpleName();
	
	private PageflipUtils() {
		// Empty constructor
	}

	public static boolean isLandscape(Context c) {
		return isLandscape(c.getResources().getConfiguration());
	}
	
	public static boolean isLandscape(Configuration c) {
		return c.orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	private static final boolean LANDSCAPE = true;
	private static final boolean PORTRAIT = false;
	
	public static void test() {
		
		EtaLog.d(TAG, "Running page/position tests");
		
		testPageToPosition(1, LANDSCAPE, 0);
		testPageToPosition(2, LANDSCAPE, 1);
		testPageToPosition(3, LANDSCAPE, 1);
		testPageToPosition(4, LANDSCAPE, 2);
		testPageToPosition(5, LANDSCAPE, 2);
		
		int PAGE_COUNT = 8;
		testPositionToPage(0, PAGE_COUNT, LANDSCAPE, new int[]{1});
		testPositionToPage(1, PAGE_COUNT, LANDSCAPE, new int[]{2,3});
		testPositionToPage(2, PAGE_COUNT, LANDSCAPE, new int[]{4,5});
		testPositionToPage(3, PAGE_COUNT, LANDSCAPE, new int[]{6,7});
		testPositionToPage(4, PAGE_COUNT, LANDSCAPE, new int[]{8});
		
		testPageToPosition(1, PORTRAIT, 0);
		testPageToPosition(2, PORTRAIT, 1);
		testPageToPosition(3, PORTRAIT, 2);
		testPageToPosition(4, PORTRAIT, 3);

		PAGE_COUNT = 4;
		testPositionToPage(0, PAGE_COUNT, PORTRAIT, new int[]{1});
		testPositionToPage(1, PAGE_COUNT, PORTRAIT, new int[]{2});
		testPositionToPage(2, PAGE_COUNT, PORTRAIT, new int[]{3});
		testPositionToPage(3, PAGE_COUNT, PORTRAIT, new int[]{4});
		testPositionToPage(4, PAGE_COUNT, PORTRAIT, new int[]{5});
		
		EtaLog.d(TAG, "Done page/position tests");
	}
	
	private static void testPageToPosition(int page, boolean land, int expectedPos) {
		if ( pageToPosition(page, land) != expectedPos ) {
			EtaLog.e(TAG, "pageToPosition[page:" + page + ", land:" + land + ", expected:" + expectedPos);
		}
	}
	
	private static void testPositionToPage(int pos, int pageCount, boolean land, int[] expectedPages) {
		int[] pages = positionToPages(pos, pageCount, land);
		if ( !Arrays.equals(pages, expectedPages) ) {
			EtaLog.e(TAG, "positionToPage[pos:" + pos + ", land:" + land + ", expected:" + join(",", expectedPages) + ", got:" + join(",", pages));
		}
	}
	
	public static String join(CharSequence delimiter, int[] tokens) {
		StringBuilder sb = new StringBuilder();
		boolean firstTime = true;
		for (Object token: tokens) {
			if (firstTime) {
				firstTime = false;
			} else {
				sb.append(delimiter);
			}
			sb.append(token);
		}
		return sb.toString();
	}
	
	public static int pageToPosition(int[] pages, boolean landscape) {
		return pageToPosition(pages[0], landscape);
	}

	public static int pageToPosition(int page, boolean landscape) {
		int pos = page-1;
		if (landscape && page > 1) {
			if (page%2==1) {
				page--;
			}
			pos = page/2;
		}
		return pos;
	}
	
	public static int[] positionToPages(int position, int pageCount, boolean landscape) {
		// default is offset by one
		int page = 0;
		if (landscape && position != 0) {
			page = (position*2);
		} else {
			page = position+1;
		}
		
		int[] pages = null;
		if (!landscape || page == 1 || page == pageCount) {
			// first, last, and everything in portrait is single-page
			pages = new int[]{page};
		} else {
			// Anything else is double page
			pages = new int[]{page, (page+1)};
		}
		return pages;
	}
	
	public static boolean isValidPage(Catalog c, int page) {
		return ( 1 <= page && page <= c.getPageCount());
	}
	
	public static boolean almost(float first, float second) {
		return almost(first, second, 0.1f);
	}
	
	public static boolean almost(float first, float second, float epsilon) {
		return Math.abs(first-second)<epsilon;
	}
	
	public static Bitmap mergeImage(Bitmap leftBitmap, Bitmap rightBitmap) {
		int w = leftBitmap.getWidth()*2;
		int h = leftBitmap.getHeight();
		Bitmap b = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(b);
		canvas.drawBitmap(leftBitmap, 0, 0, null);
		canvas.drawBitmap(rightBitmap, (w/2), 0, null);
		return b;
	}
	
}
