package com.alvarosantisteban.room;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * A TaggeableImageView is an ImageView that can be tagged. Therefore, contains the set of tags to be painted, the Paint needed to do so
 * and the coordinates of the point where the user wants to set a tag.
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class TaggeableImageView extends ImageView {
	
	/**
	 * Used for logging purposes
	 */
	private static final String TAG = "TaggeableImageView";
	
	/**
	 * Coordinates of the touched point
	 */
	int xPosition;
	int yPosition;
	/**
	 * The paint for the rectangles
	 */
	Paint paintRect;
	
	/**
	 * The list with the tags to be painted
	 */
	List<Tag> tags;	

	public TaggeableImageView(Context context) {
		super(context);
		init();
	}

	public TaggeableImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	/**
	 * Creates the Paint
	 */
	public void init(){		
		paintRect = new Paint();
		paintRect.setColor(Color.RED);
		paintRect.setStyle(Paint.Style.STROKE);
	}

	/**
	 * Draws the tags 
	 */
	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		if(tags != null){
			Iterator<Tag> iterator = tags.iterator();
			while(iterator.hasNext()){			
				Tag tag = iterator.next();
				canvas.drawRect(tag.rect, paintRect);
				canvas.drawText(tag.name, tag.rect.left, tag.rect.top - 5, paintRect);
			}
		}
	}
	
	/**
	 * Registers the touch events to capture the x and y coordinates.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event){
		Log.v(TAG, "OnTouchEvent from TaggeableImageView");
		final int action = event.getAction();
	    switch (action & MotionEvent.ACTION_MASK) {
	       case MotionEvent.ACTION_DOWN: {
	    	   xPosition = (int) event.getX();
	    	   yPosition = (int) event.getY();
	       break;
	       }
	       
	    }
		return true;
	}
	
	/*
	private OnLongClickListener longListener = new OnLongClickListener(){

		@Override
		public boolean onLongClick(View v) {
			Log.v(TAG, "OnLongClick from TaggeableImageView");
			// Si el punto esta dentro de una de las regiones
			// entonces es que lo quieren aumentar/disminuir/mover
			//wallImage.changeSize();
			// If not, we create a new one
			//Tag tag = new Tag("TagX", new Point(xPosition,yPosition));
			//wall.addTag(tag);
			//((TaggeableImageView) wallImage).update((ArrayList<Tag>) wall.tags);
			//wallImage.invalidate();
			
			return false;
		}
	};
	*/
	
	/**
	 * Sets the tags to be printed
	 * @param theTags the tags to be printed
	 */
	public void setTags(ArrayList<Tag> theTags){
		tags = theTags;
	}
}
