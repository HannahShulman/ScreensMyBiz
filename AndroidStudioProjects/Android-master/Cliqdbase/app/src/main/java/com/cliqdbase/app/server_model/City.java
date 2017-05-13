package com.cliqdbase.app.server_model;

/**
 * @author Yuval
 * A class that represents a city in our database.
 */
public class City {
	private int cityCode;
	private String cityName;
	private String stateName;
	
	public City (int cityCode, String cityName, String stateName) {
		this.cityCode = cityCode;
		this.cityName = cityName;
		this.stateName = stateName;
	}

	public int getCityCode() {
		return this.cityCode;
	}

    public String getDisplayName() {
        return this.cityName + " (" + this.stateName + ")";
    }


}
