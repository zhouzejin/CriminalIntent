package com.sunny.criminalintent;

import org.json.JSONException;
import org.json.JSONObject;

public class Photo {
	
	private static final String JSON_FILENAME = "filename";
	
	private String mFilename;

	/**
	 * Create a Photo representing an existing file on disk
	 * @param filename
	 */
	public Photo(String filename) {
		mFilename = filename;
	}
	
	public Photo(JSONObject json) throws JSONException {
		mFilename = json.getString(JSON_FILENAME);
	}
	
	public JSONObject toJson() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(JSON_FILENAME, mFilename);
		return json;
	}
	
	public String getFilename() {
		return mFilename;
	}

}
