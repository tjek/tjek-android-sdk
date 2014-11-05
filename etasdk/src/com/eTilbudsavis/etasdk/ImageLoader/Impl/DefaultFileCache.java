package com.eTilbudsavis.etasdk.ImageLoader.Impl;

import static android.os.Environment.MEDIA_MOUNTED;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;

import android.content.Context;
import android.os.Environment;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.ImageLoader.FileCache;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Utils.PermissionUtils;

public class DefaultFileCache implements FileCache {

	public static final String TAG = Eta.TAG_PREFIX + DefaultFileCache.class.getSimpleName();
	
	private File mCacheDir;
	private ExecutorService mExecutor;
	
	public DefaultFileCache(Context context, ExecutorService executor){
		mCacheDir = getCacheDirectory(context, true);
		mExecutor = executor;
		EtaLog.v(TAG, "CacheDir: " + mCacheDir.getAbsolutePath());
		cleanup();
	}
	
	public static File getCacheDirectory(Context context, boolean preferExternal) {
		File cacheDir = null;
		if (preferExternal &&
				MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) &&
					PermissionUtils.hasWriteExternalStorage(context)) {
			cacheDir = getExternalCacheDir(context);
		}
		
		if (cacheDir == null) {
			cacheDir = context.getCacheDir();
		}
		
		if (cacheDir == null) {
			String filesDir = context.getFilesDir().getPath();
			cacheDir = new File(filesDir + context.getPackageName() + "/cache/");
		}
		
		return cacheDir;
	}
	
	private static File getExternalCacheDir(Context context) {
		File dataDir = new File(Environment.getExternalStorageDirectory(), "Android/data");
		File cacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
		if (!cacheDir.exists() && !cacheDir.mkdirs()) {
			EtaLog.w(TAG, "External directory couldn't be created");
			return null;
		}
		return cacheDir;
	}
	
	public void save(final ImageRequest ir, final byte[] b) {
		
		Runnable r = new Runnable() {
			
			public void run() {
				
				File f = new File(mCacheDir, ir.getFileName());
				FileOutputStream fos = null;
				if (f.exists()) {
					f.delete();
				}
				try {
					fos = new FileOutputStream(f);
					fos.write(b);
				} catch (IOException e) {
					EtaLog.d(TAG, e.getMessage(), e);
				} finally {
					try{
						fos.close();
					} catch(Throwable t) {
						EtaLog.d(TAG, t.getMessage(), t);
					}
				}
//				EtaLog.d(TAG, "SaveByteArray: " + (System.currentTimeMillis()-start));
			}
		};
		
		mExecutor.execute(r);
		
	}
	
	public byte[] getByteArray(ImageRequest ir) {
		File f = new File(mCacheDir, ir.getFileName());
		byte[] b = null;
		if (f.exists()) {
			try {
				b = readFile(f);
			} catch (FileNotFoundException e) {
				EtaLog.d(TAG, e.getMessage(), e);
			} catch ( IOException  e2) {
				
			}
		}
		return b;
	}
	
	public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }
	private static final long WEEK_IN_MILLIS = 1000 * 60 * 60 * 24 * 7;
	
	Runnable cleaner = new Runnable() {
		
		public void run() {
			File[] files = mCacheDir.listFiles();
			if( files == null ) {
				return;
			}
			int count = 0;
			for(File f:files) {
				boolean recentlyModified = (System.currentTimeMillis()-f.lastModified()) < WEEK_IN_MILLIS;
				if ( !recentlyModified ) {
					count++;
					f.delete();
				}
			}
			if (count > 0) {
				EtaLog.v(TAG, "Deleted " + count + " files from " + getClass().getSimpleName());
			}
		}
	};
	
	public void cleanup(){
		
		mExecutor.execute(cleaner);
		
	}
	
	public void clear(){
		File[] files = mCacheDir.listFiles();
		if( files == null ) {
			return;
		}
		for(File f:files) {
			f.delete();
		}
	}
	
}
