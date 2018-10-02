package com.outreach.interviews.map.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.outreach.interviews.map.enums.MapModes;
import com.outreach.interviews.map.enums.MapOperations;
import com.outreach.interviews.map.enums.MapRegions;

public class GeoCodeHelper {
    
    public static class GeoCode{
        private String address;
        private MapRegions region;
        private MapOperations operation;
        private JsonObject result;

        private final String URL = "https://maps.googleapis.com/maps/api/";
        private CloseableHttpClient httpclient = HttpClients.createDefault();
    
        /**
         * Set the Origin / Starting Point.
         * @param origin String containing the starting point.
         * @return {@link GeoCode}
         */
        public GeoCode setAddress(String address){
            this.address = address;
            return this;
        }


        /**
         * Set the region {@link MapRegions}
         * @param region
         * @return {@link GeoCode}
         */
        public GeoCode setRegion(MapRegions region){
            this.region = region;
        }


        /**
         * Create URL to communicate with the Google Maps API
         * @param type URL to provide to Apache HttpClient
         * @return {@link GeoCode}
         */
        public GeoCode setURL(MapOperations type){
            if (type.equals(MapOperations.directions))
                throw new UnsupportedOperationException();
            this.operation = type;
            return this;
        }


        /**
         * Perform the HTTP request and restrieve the data from the HttpClient Java Object.
         * @return {@link GeoCode}
         * @throws UnsupportedOperationException
         * @throws IOException
         * @throws IllegalArgumentException
         */
        public GeoCode build() throws UnsupportedOperationException, IOException, IllegalArgumentException {
            String requestURL = this.getURL()   + "&address=" + getAddress()
                                                + "&KEY=" + getAPIKey();

            HttpGet httpGet = new HttpGet(requestURL);
            CloseableHttpClient response = httpclient.execute(httpGet);
            try {
                HttpEntity entity = response.getEntity();
                String result = IOUtils.toString(entity.getContent(), "UTF-8");
                this.result = new JsonParser().parse(result).getAsJsonObject();
            } finally {
                response.close();
            }
            return this;
        }


        /**
         * Retrieve the Longitude and Latitude from the address.
         * @return List of Strings containing the longitude and latitude.
         */
        public List<String> getCoordinates() {
            if (this.operation.equals(MapOperations.geocode) && zeroResults(this.result)) {
                List<String> result = new ArrayList<String>();
                String lat = this.result.get("results").getAsJsonObject()
                                            .get("geometry").getAsJsonObject()
                                            .get("location").getAsJsonObject()
                                            .get("lat").getAsJsonObject().getAsString();
                String lng = (this.result.get("results").getAsJsonObject()
                                            .get("geometry").getAsJsonObject()
                                            .get("location").getAsJsonObject()
                                            .get("lng").getAsJsonObject()).getAsString();
                
                result.add(lat);
                result.add(lng);
                return result;
            } else {
                throw new IllegalArgumentException("Does not support " + MapOperations.directions.name());
            }
        }



        /**
         * Returns the URL
         * @return the String containing the API's URL
         */
        private final String getURL(){
            return this.URL + this.operation.name() + "/json?";
        }


        /**
         * Returns the API Key.
         * @return String containing the API Key
         */
        private final String getAPIKey() {
            return "AIzaSyBJtE2fFXChcd2w44s-OuSY8QcKWiseUKw";
        }


        /**
         * Returns the origin.
         * @return String containing the origin
         */
        private final String getAddress(){
            if (this.origin == null)
                throw new IllegalArgumentException("Address can not be empty.");
            return this.origin;
        }


        /**
         * 
         */
        private final String getRegion() {
			if(this.destination == null)
				throw new IllegalArgumentException("Region cannot be empty");
			return this.region.name();
        }
        

        private final boolean zeroResults(JsonObject obj) {
			return !obj.get("status").getAsString().equals("ZERO_RESULTS");
		}
    }
}