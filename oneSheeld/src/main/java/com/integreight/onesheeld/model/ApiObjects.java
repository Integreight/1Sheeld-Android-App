package com.integreight.onesheeld.model;

import java.util.HashMap;

public class ApiObjects {
    public static ApiObject facebook = new ApiObject();
    public static ApiObject twitter = new ApiObject();
    public static ApiObject foursquare = new ApiObject();
    public static ApiObject parse = new ApiObject();
    public static ApiObject analytics = new ApiObject();

    public static class ApiObject {
        private HashMap<String, String> apiObjects;

        public ApiObject() {
            apiObjects = new HashMap<String, String>();
        }

        public void add(String key, String value) {
            apiObjects.put(key, value);
        }

        public boolean has(String key) {
            return apiObjects.containsKey(key);
        }

        public String get(String key) {
            if (has(key)) return apiObjects.get(key);
            else return "";
        }

    }
}
