package com.alvarosantisteban.room;

import android.graphics.Point;
import android.graphics.Rect;

/**
 * A Tag has a name, a center, an a rectangle which size is determinate by a distance.
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class Tag {
	
	/**
	 * Used for logging purposes
	 */
	private static final String TAG = "Tag";
	
	/**
	 * The distance from the center of a rectangle to the top, bottom, right and left 
	 */
	private static int distance = 25;

	String name = "";
	Point center;
	Rect rect;
	
	public Tag(String theName, Point theCenter) {
		name = theName;
		center = theCenter;
		rect = new Rect(theCenter.x-distance, theCenter.y-distance, theCenter.x+distance, theCenter.y+distance);
	}
	
	/**
	 * Sets the distance and updates the rectangle
	 */
	public void setDistance(int newDistance){
		distance = newDistance;
		// Update the rect
		rect.set(center.x-distance, center.y-distance, center.x+distance, center.y+distance);
	}
	
	/**
	 * Sets the center and updates the rectangle
	 */
	public void setCenter(Point newCenter){
		center = newCenter;
		// Update the rect
		rect.set(center.x-distance, center.y-distance, center.x+distance, center.y+distance);
	}
}
