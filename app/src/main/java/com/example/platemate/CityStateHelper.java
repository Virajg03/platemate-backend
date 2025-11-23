package com.example.platemate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityStateHelper {
    
    private static final Map<String, String> cityStateMap = new HashMap<>();
    
    static {
        // Major Indian cities with their states
        cityStateMap.put("Mumbai", "Maharashtra");
        cityStateMap.put("Pune", "Maharashtra");
        cityStateMap.put("Nagpur", "Maharashtra");
        cityStateMap.put("Nashik", "Maharashtra");
        cityStateMap.put("Aurangabad", "Maharashtra");
        
        cityStateMap.put("Delhi", "Delhi");
        cityStateMap.put("New Delhi", "Delhi");
        
        cityStateMap.put("Bangalore", "Karnataka");
        cityStateMap.put("Mysore", "Karnataka");
        cityStateMap.put("Hubli", "Karnataka");
        cityStateMap.put("Mangalore", "Karnataka");
        
        cityStateMap.put("Hyderabad", "Telangana");
        cityStateMap.put("Warangal", "Telangana");
        
        cityStateMap.put("Chennai", "Tamil Nadu");
        cityStateMap.put("Coimbatore", "Tamil Nadu");
        cityStateMap.put("Madurai", "Tamil Nadu");
        cityStateMap.put("Salem", "Tamil Nadu");
        
        cityStateMap.put("Kolkata", "West Bengal");
        cityStateMap.put("Howrah", "West Bengal");
        cityStateMap.put("Durgapur", "West Bengal");
        
        cityStateMap.put("Ahmedabad", "Gujarat");
        cityStateMap.put("Surat", "Gujarat");
        cityStateMap.put("Vadodara", "Gujarat");
        cityStateMap.put("Rajkot", "Gujarat");
        
        cityStateMap.put("Jaipur", "Rajasthan");
        cityStateMap.put("Jodhpur", "Rajasthan");
        cityStateMap.put("Udaipur", "Rajasthan");
        cityStateMap.put("Kota", "Rajasthan");
        
        cityStateMap.put("Lucknow", "Uttar Pradesh");
        cityStateMap.put("Kanpur", "Uttar Pradesh");
        cityStateMap.put("Agra", "Uttar Pradesh");
        cityStateMap.put("Varanasi", "Uttar Pradesh");
        cityStateMap.put("Allahabad", "Uttar Pradesh");
        
        cityStateMap.put("Chandigarh", "Punjab");
        cityStateMap.put("Amritsar", "Punjab");
        cityStateMap.put("Ludhiana", "Punjab");
        
        cityStateMap.put("Bhopal", "Madhya Pradesh");
        cityStateMap.put("Indore", "Madhya Pradesh");
        cityStateMap.put("Gwalior", "Madhya Pradesh");
        
        cityStateMap.put("Bhubaneswar", "Odisha");
        cityStateMap.put("Cuttack", "Odisha");
        
        cityStateMap.put("Patna", "Bihar");
        cityStateMap.put("Gaya", "Bihar");
        
        cityStateMap.put("Thiruvananthapuram", "Kerala");
        cityStateMap.put("Kochi", "Kerala");
        cityStateMap.put("Kozhikode", "Kerala");
        
        cityStateMap.put("Guwahati", "Assam");
        cityStateMap.put("Silchar", "Assam");
        
        cityStateMap.put("Raipur", "Chhattisgarh");
        cityStateMap.put("Bilaspur", "Chhattisgarh");
        
        cityStateMap.put("Ranchi", "Jharkhand");
        cityStateMap.put("Jamshedpur", "Jharkhand");
        
        cityStateMap.put("Dehradun", "Uttarakhand");
        cityStateMap.put("Haridwar", "Uttarakhand");
        
        cityStateMap.put("Shimla", "Himachal Pradesh");
        cityStateMap.put("Dharamshala", "Himachal Pradesh");
        
        cityStateMap.put("Gandhinagar", "Gujarat");
        cityStateMap.put("Panaji", "Goa");
        cityStateMap.put("Imphal", "Manipur");
        cityStateMap.put("Aizawl", "Mizoram");
        cityStateMap.put("Kohima", "Nagaland");
        cityStateMap.put("Agartala", "Tripura");
        cityStateMap.put("Shillong", "Meghalaya");
        cityStateMap.put("Gangtok", "Sikkim");
        cityStateMap.put("Itanagar", "Arunachal Pradesh");
    }
    
    public static List<String> getAllCities() {
        return new ArrayList<>(cityStateMap.keySet());
    }
    
    public static String getStateForCity(String city) {
        return cityStateMap.get(city);
    }
    
    public static boolean isValidCity(String city) {
        return cityStateMap.containsKey(city);
    }
    
    public static List<String> getCitiesForState(String state) {
        List<String> cities = new ArrayList<>();
        for (Map.Entry<String, String> entry : cityStateMap.entrySet()) {
            if (entry.getValue().equals(state)) {
                cities.add(entry.getKey());
            }
        }
        return cities;
    }
}

