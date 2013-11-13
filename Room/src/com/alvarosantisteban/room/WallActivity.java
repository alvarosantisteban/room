package com.alvarosantisteban.room;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Adapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The WallActivity allows the user tag parts of a wall, visualize areas already tagged and XXXX.
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class WallActivity extends Activity {
	
	/**
	 * Used for logging purposes
	 */
	private static final String TAG = "WallActivity";
	
	/**
	 * File name of the preferences
	 */
	String PREFS_NAME = "RoomPreferences";
	private static SharedPreferences prefs;
	// Access the preference for the Wall
	static String WALL_PREFERENCES = "aWall";
	
	// Used to store a Wall object as json string
	private static Gson GSON = new Gson();
	private static final Type WALL_TYPE = new TypeToken<Wall>() {}.getType();
	
	// To get the extra from the intent 
	String WALL_PATH = "wallPath";
	static Wall wall;
	
	static Uri imageUri;
	ImageView wallImage;
	
	/**
	 * The ListView with the actions that the user can do regarding a Tag
	 */
	ListView editTagOptionsList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wall);
		Log.d(TAG, "onCreate");
		
		wallImage = (TaggeableImageView) findViewById(R.id.wallImage);
		editTagOptionsList = (ListView) findViewById(R.id.editTagOptionsList);
		
		// Get the preferences to load and save the tags
		prefs = getBaseContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		
		/*
		 * To delete everything in the SharedPreferences
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		Editor editor = settings.edit();
		editor.clear();
		editor.commit();
		*/
		
		/*
		 * This helps somehow having the list in a smaller size
		String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
		        "Blackberry", "WebOS"};
		
		final ArrayList<String> list = new ArrayList<String>();
	    for (int i = 0; i < values.length; ++i) {
	      list.add(values[i]);
	    }
	    */
	    //final StableArrayAdapter adapter = new StableArrayAdapter(this,R.layout.simple_list_item, list);
	    //editTagOptionsList.setAdapter(adapter);

	    //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_list_item, R.id.name_for_options_list, values);
	    //editTagOptionsList.setAdapter(adapter);
	    //editTagOptionsList.getLayoutParams().width = getWidestView(this, adapter);
		
	    editTagOptionsList.getLayoutParams().width = 240;
		
	    // Get the intent from the main activity to obtain the URI of the image
		Intent intent = getIntent();
		imageUri = intent.getParcelableExtra(WALL_PATH);

		// Display the image
		wallImage.setImageURI(imageUri);
		// Set its touch listener
		wallImage.setOnTouchListener(onTouchListener);

		// Load the corresponding wall or create a new one		
		wall = loadWall();
		// Set the tags for the TaggeableImageView
		((TaggeableImageView) wallImage).setTags((ArrayList<Tag>) wall.tags);
	}
	
	private OnTouchListener onTouchListener = new OnTouchListener(){
		@Override
		public boolean onTouch(View v, MotionEvent event)  {    
			Log.v(TAG, "onTouch");
		    
			final int action = event.getAction();
		    switch (action & MotionEvent.ACTION_MASK) {
		       case MotionEvent.ACTION_DOWN: {
		    	   int xPosition = (int) event.getX();
		    	   int yPosition = (int) event.getY();
		    	   Point touchedPoint = new Point(xPosition, yPosition);
		    	   if(isThereATag(touchedPoint)){
		    		   	// Move or change the size of the current Tag
		    		   editTagOptionsList.setVisibility(View.VISIBLE);
		    		   saveWall(wall);
		    	   }else{
		    		   	// Add a new Tag
		    		   	Tag tag = new Tag("TagX", new Point(xPosition,yPosition));
		   				wall.addTag(tag);
		   				// Update the set of tags of the WallImage
		   				((TaggeableImageView) wallImage).setTags((ArrayList<Tag>) wall.tags);
		   				// Ask it to paint itself again
		   				wallImage.invalidate();
		    	   }
		       break;
		       }
		    }
		    return false;
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.wall, menu);
		return true;
	}

	/**
	 * Check if the touchedPoint is inside a Tag
	 * @param touchedPoint the touched point
	 * @return true if the touched point is inside a tag, false otherwise
	 */
	protected boolean isThereATag(Point touchedPoint) {
		if(wall.tags != null){
			// Iterate through the Tags list to see if the touched point is inside any of them
			Iterator<Tag> tagsIterator = wall.tags.iterator();
			while(tagsIterator.hasNext()){
				Rect rect = tagsIterator.next().rect;
				if(rect.contains(touchedPoint.x, touchedPoint.y)){
					// TODO Return the id of the Tag
					return true; 
				}
			}	
		}
		return false;
	}
	
	/**
	 * Saves the current state of the wall in the shared preferences
	 * @param wall the wall to be saved
	 */
	public static void saveWall (Wall wall) {
		Log.d(TAG, "saveWall");
		
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(WALL_PREFERENCES, GSON.toJson(wall, WALL_TYPE));
	    editor.commit();
	}

	/**
	 * Loads the wall saved in the shared preferences
	 * @return
	 */
	public static Wall loadWall() {
		Log.d(TAG, "loadWall");
		wall = GSON.fromJson(prefs.getString(WALL_PREFERENCES, null), WALL_TYPE);
		if (wall == null) {
			Log.d(TAG, "wall is null");
			// There is no saved wall, create a new one
	    	wall = new Wall("Not defined", imageUri.toString());
	    }else{
	    	if(!wall.uriWallImage.equals(imageUri.toString())){
	    		Log.d(TAG, "different wall to the saved one");
	    		// The image of the saved wall does not match the current used image, create a new Wall
	        	wall = new Wall("Not defined", imageUri.toString());
	        }
	    }
	    return wall;
	}
	
	/**
	 * Computes the widest view in an adapter, best used when you need to wrap_content on a ListView, please be careful
	 * and don't use it on an adapter that is extremely numerous in items or it will take a long time.
	 *
	 * @param context Some context
	 * @param adapter The adapter to process
	 * @return The pixel width of the widest View
	 */
	public static int getWidestView(Context context, Adapter adapter) {
	    int maxWidth = 0;
	    View view = null;
	    FrameLayout fakeParent = new FrameLayout(context);
	    for (int i=0, count=adapter.getCount(); i<count; i++) {
	        view = adapter.getView(i, view, fakeParent);
	        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
	        int width = view.getMeasuredWidth();
	        if (width > maxWidth) {
	            maxWidth = width;
	        }
	    }
	    return maxWidth;
	}	
	
	/*
	private OnLongClickListener longListener = new OnLongClickListener(){

		@Override
		public boolean onLongClick(View v) {
			System.out.println("OnLongClick");
			// Si el punto esta dentro de una de las regiones
			// entonces es que lo quieren aumentar/disminuir/mover
			//wallImage.changeSize();
			// If not, we create a new one
			
			Tag tag = new Tag("TagX", new Point(xPosition,yPosition));
			wall.addTag(tag);
			((TaggeableImageView) wallImage).update((ArrayList<Tag>) wall.tags);
			wallImage.invalidate();
			
			return false;
		}
		
	};
	*/
	
	/*
	private class StableArrayAdapter extends ArrayAdapter<String> {

	    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

	    public StableArrayAdapter(Context context, int textViewResourceId,
	        List<String> objects) {
	      super(context, textViewResourceId, objects);
	      for (int i = 0; i < objects.size(); ++i) {
	        mIdMap.put(objects.get(i), i);
	      }
	    }

	    @Override
	    public long getItemId(int position) {
	      String item = getItem(position);
	      return mIdMap.get(item);
	    }

	    @Override
	    public boolean hasStableIds() {
	      return true;
	    }

	  }
	  */
}