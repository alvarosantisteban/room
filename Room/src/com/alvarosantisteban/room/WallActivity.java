package com.alvarosantisteban.room;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.DialogFragment;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The WallActivity allows the user tag parts of a wall, visualize areas already tagged and XXXX.
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class WallActivity extends Activity implements NoticeDialogFragment.NoticeDialogListener{
	
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
	// Access the preferences for the tag mode
	static String TAG_MODE_PREFERENCES = "tagMode";
	
	// Used to store a Wall object as json string
	private static Gson GSON = new Gson();
	private static final Type WALL_TYPE = new TypeToken<Wall>() {}.getType();
	
	// To get the extra from the intent 
	String WALL_PATH = "wallPath";
	static Wall wall;
	
	static Uri imageUri;
	ImageView wallImage;
	
	Context context;
	
	/**
	 * The ListView with the actions that the user can do regarding a Tag
	 */
	ListView editTagOptionsList;
	
	Switch editTagsSwitch;
	/*
	 * if true, user can edit tags, 
	 * if false, user can add new tags
	 */
	private boolean editTagsMode;

	/**
	 * A reference to the last touched tag by the user
	 */
	protected Tag lastTouchedTag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wall);
		Log.d(TAG, "onCreate");
		context = this;
		
		wallImage = (TaggeableImageView) findViewById(R.id.wallImage);
		editTagOptionsList = (ListView) findViewById(R.id.editTagOptionsList);
		editTagsSwitch = (Switch) findViewById(R.id.editModeSwitch);
		
		// Get the preferences to load and save the tags
		prefs = getBaseContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

		editTagsMode = editTagsSwitch.isChecked();
		
		editTagsSwitch.setOnCheckedChangeListener(checkListener);
		
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
		editTagOptionsList.setOnItemClickListener(listItemListener);
		
	    // Get the intent from the main activity to obtain the URI of the image
		Intent intent = getIntent();
		imageUri = intent.getParcelableExtra(WALL_PATH);

		// Display the image
		wallImage.setImageURI(imageUri);
		wallImage.setOnTouchListener(onTouchListener);

		// Load the corresponding wall or create a new one		
		wall = loadWall();
		// Set the tags for the TaggeableImageView
		((TaggeableImageView) wallImage).setTags((ArrayList<Tag>) wall.tags);
		
		lastTouchedTag = null;
	}
	
	////////////////////////////////////////////////////////////////
	// SAVE AND LOAD THE WALL
	////////////////////////////////////////////////////////////////
	
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
	
	////////////////////////////////////////////////////////////////
	// HELPER METHODS
	////////////////////////////////////////////////////////////////
	
	/**
	 * Check if the touchedPoint is inside a Tag
	 * @param touchedPoint the touched point
	 * @return true if the touched point is inside a tag, false otherwise
	 */
	protected Tag isThereATag(Point touchedPoint) {
		if(wall.tags != null){
			// Iterate through the Tags list to see if the touched point is inside any of them
			Iterator<Tag> tagsIterator = wall.tags.iterator();
			while(tagsIterator.hasNext()){
				Tag tag = tagsIterator.next();
				Rect rect = tag.rect;
				if(rect.contains(touchedPoint.x, touchedPoint.y)){
					return tag; 
				}
			}	
		}
		return null;
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
	
	////////////////////////////////////////////////////////////////
	// LISTENERS
	////////////////////////////////////////////////////////////////
	
	/**
	 * On touch listener. Controls the touched points to generate new tags and edit/delete existing ones.
	 */
	private OnTouchListener onTouchListener = new OnTouchListener(){
		@Override
		public boolean onTouch(View v, MotionEvent event)  {    
			Log.v(TAG, "onTouch");
		    
			final int action = event.getAction();
			
			// To add new tags
			if(!editTagsMode){		
				switch (action & MotionEvent.ACTION_MASK) {
			       case MotionEvent.ACTION_DOWN: {
			    	   int xPosition = (int) event.getX();
			    	   int yPosition = (int) event.getY();
			    	   Point touchedPoint = new Point(xPosition, yPosition);
			    	   if(isThereATag(touchedPoint) == null){
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
			// To edit already existing tags
			}else{
				switch (action & MotionEvent.ACTION_MASK) {
			       case MotionEvent.ACTION_DOWN: {
			    	   int xPosition = (int) event.getX();
			    	   int yPosition = (int) event.getY();
			    	   Point touchedPoint = new Point(xPosition, yPosition);
			    	   Tag touchedTag = isThereATag(touchedPoint);
			    	   if(touchedTag != null){
			    		   	// Move or change the size of the current Tag
			    		   lastTouchedTag = touchedTag;
			    		   editTagOptionsList.setVisibility(View.VISIBLE);
			    		   saveWall(wall);
			    	   }
			       break;
			       }
			    }	
			}
		    return false;
		}
	};

	/**
	 * Listener for the state of the Switch
	 */
	OnCheckedChangeListener checkListener = new OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			editTagOptionsList.setVisibility(View.INVISIBLE);
			editTagsMode = arg1;			
		}
		
	};
	
	/**
	 * Listener for the items of the List
	 */
	OnItemClickListener listItemListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
			switch (position) {
		       case 0: {
		    	   // Add or edit tags name
		    	   Log.d(TAG,"Add or edit the name of the tag");
		    	   // TODO Pass the current name of the tag to the dialog
		    	   showNoticeDialog();
		       break;
		       }case 1: {
		    	   // Add or edit the data associated to the tag
		    	   Log.d(TAG,"Add or edit the data of the tag");
		    	   break;
		       }case 2: {
		    	   // Move the tag
		    	   Log.d(TAG,"Move the tag");
		    	   break;
		       }case 3: {
		    	   // Delete the tag
		    	   Log.d(TAG, "Delete the tag");
	   				wall.removeTag(lastTouchedTag);
	   				// Update the set of tags of the WallImage
	   				((TaggeableImageView) wallImage).setTags((ArrayList<Tag>) wall.tags);
	   				// Ask it to paint itself again
	   				wallImage.invalidate();
		    	   break;
		       }case 4: {
		    	   // Change the size of the tag
		    	   Log.d(TAG,"Change the size of the tag");
		    	   break;
		       }
		    }
			editTagOptionsList.setVisibility(View.INVISIBLE);
		}
	};
	
	////////////////////////////////////////////////////////////////
	// METHODS FOR THE NOTICE DIALOG LISTENER
	////////////////////////////////////////////////////////////////
	
	
	public void showNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new NoticeDialogFragment();
        ((NoticeDialogFragment) dialog).setName(lastTouchedTag.name);
        dialog.show(getFragmentManager(), "NoticeDialogFragment");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        EditText newName = (EditText)dialog.getDialog().findViewById(R.id.editText);
        lastTouchedTag.setName(newName.getText().toString());
 	   	// Ask it to paint itself again
        wallImage.invalidate();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
    }
    
    
    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.wall, menu);
		return true;
	}

	
	//private String showTagNamePopup() {
		/*
		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		// 2. Chain together various setter methods to set the dialog characteristics
		builder.setMessage(R.string.change_name_popup).setTitle(R.string.dialog_title);
		
		// Add the buttons
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               // User clicked OK button
		           }
		       });
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               // User cancelled the dialog
		           }
		       });

		
		// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();
		*/
		
		
		/* Inflate the popup_layout.xml
		LinearLayout viewGroup = (LinearLayout) findViewById(R.id.popup);
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = layoutInflater.inflate(R.layout.change_name_popup, viewGroup);
		
		
		final PopupWindow popup = new PopupWindow();
		popup.setWidth(200);
		popup.setHeight(150);
		popup.setFocusable(true);
		popup.setBackgroundDrawable(null);
		popup.showAtLocation(wallImage, Gravity.CENTER, 0, 0);
		// Getting a reference to Close button, and close the popup when clicked.
		Button close = (Button) layout.findViewById(R.id.cancelButton);
		close.setOnClickListener(new OnClickListener() {
		 
			@Override
		    public void onClick(View v) {			
				popup.dismiss();
		    }
		});
		final EditText editText = (EditText)layout.findViewById(R.id.editText);
		Button accept = (Button) layout.findViewById(R.id.okButton);
		String newName = "";
		accept.setOnClickListener(new OnClickListener(){
			@Override
		    public void onClick(View v) {
				Log.d(TAG, editText.getText().toString());
				popup.dismiss();
		    }
		});
		return newName;
		*/
	
	
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