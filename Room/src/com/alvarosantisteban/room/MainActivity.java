package com.alvarosantisteban.room;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

/**
 * The MainActivity allows the user to make a photo of a wall or load one.
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class MainActivity extends Activity {
	
	/**
	 * Used for logging purposes
	 */
	private static final String TAG = "MainActivity";
	
	// The extra that contains the URI of the saved photo to be added to the Intent
	private final String WALL_PATH = "wallPath";
	// Constant to request taking a photo
	private final int CAMERA_PIC_REQUEST = 5;
	/**
	 * File name of the preferences
	 */
	private final String PREFS_NAME = "roomPreferences";
	// Access the preference with the saved URI
	private final String URI_PREFERENCES = "uri";
	
	//RelativeLayout layout; It will probably be used when I need to have the gallery with the saved photos
	
	Uri uriSavedImage; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//layout = (RelativeLayout)findViewById(R.id.layout);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * Makes a photo and stores the resulting URI
	 * @param view
	 */
	public void makePhoto(View view){
		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
			// Intent to make a photo
			Intent imageIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(new Date());

			// Create the folder where we will store the images
			File imagesFolder = new File(Environment.getExternalStorageDirectory(), "RoomImages");
			imagesFolder.mkdirs();

			// Create the image file
			File image = new File(imagesFolder, "Wall_" + timeStamp + ".png");
			uriSavedImage = Uri.fromFile(image);
			Log.d(TAG,"uripath:"+uriSavedImage.getPath());
			
			// Save the URI in the preferences
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			Editor editor = settings.edit();
			editor.putString(URI_PREFERENCES, uriSavedImage.toString());
			editor.commit();
			
			// Start the intent
			imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
			startActivityForResult(imageIntent, CAMERA_PIC_REQUEST);
		}else{
			// Ask to upload the photo
			Toast.makeText(this, "Please, upload a photo", Toast.LENGTH_LONG).show();
			// Open the Gallery / File System
			//http://stackoverflow.com/questions/2169649/get-pick-an-image-from-androids-built-in-gallery-app-programmatically
			//http://stackoverflow.com/questions/2507898/how-to-pick-an-image-from-gallery-sd-card-for-my-app-in-android
		}
	}
	
	/**
	 * Loads the stored wall 
	 * @param view
	 */
	public void loadWall(View view){
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String uri = settings.getString(URI_PREFERENCES, "noUri");
		if(!uri.equals("noUri")){
			Uri imageUri = Uri.parse(uri);
			goToWall(imageUri);
		}else{
			Toast.makeText(this, "There are no walls store, make a photo first.", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Goes to WallActivity displaying the image pointed by the URI given as parameter
	 * @param uriSavedImage the URI of the photo to be displayed in the WallActivity
	 */
	private void goToWall(Uri uriSavedImage) {
		Intent intentWall = new Intent(this, WallActivity.class);
		intentWall.putExtra(WALL_PATH, uriSavedImage);
		startActivity(intentWall);
	} 
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
	    if (requestCode == CAMERA_PIC_REQUEST) {  
	    	if(resultCode == RESULT_OK) {
	        	// Image captured and saved to fileUri specified in the Intent
	            Toast.makeText(this, "Image saved", Toast.LENGTH_LONG).show();       
	            // Go to the WallActivity
	            goToWall(uriSavedImage);
	    	}else if (resultCode == RESULT_CANCELED) {
	        	// User cancelled the image capture
	    		Toast.makeText(this, "Cancelled action", Toast.LENGTH_LONG).show();
	    	}else {
	    		// Image capture failed, advise user
	    		Toast.makeText(this, "Image capture failed", Toast.LENGTH_LONG).show();
	    	}
	    }  
	}
}