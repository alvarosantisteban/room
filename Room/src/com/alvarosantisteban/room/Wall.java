package com.alvarosantisteban.room;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a wall of a room. A wall has a name, a reference to an image, an id and a list of tags.
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class Wall {
	
	/**
	 * Used for logging purposes
	 */
	private static final String TAG = "Wall";
	
	String name;
	String uriWallImage; //Uri uriWallImage; Find a solution for this
	List<Tag> tags;
	UUID id;
	
	
	public Wall (String theName, String theUri){
		name = theName;
		tags = null;
		id = UUID.randomUUID();
		uriWallImage = theUri;
	}
	
	/*
	public Wall (String theName, Uri theUri){
		name = theName;
		uriWallImage = theUri;
		tags = null;
		id = UUID.randomUUID();
	}
	*/
	
	/**
	 * Adds a new tag to the List of Tags
	 * @param tag the tags to be added
	 */
	public void addTag(Tag tag){
		if (tags == null){
			tags = new ArrayList<Tag>();
		}
		tags.add(tag);
	}
	
	public void removeTag(Tag tag){
		tags.remove(tag);
	}
}