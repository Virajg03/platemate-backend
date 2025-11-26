package com.example.platemate;

import com.google.gson.annotations.SerializedName;

public class DeliveryZone {
    @SerializedName("id")
    private Long id;

    @SerializedName("zoneName")
    private String zoneName;

    @SerializedName("city")
    private String city;

    @SerializedName("pincodeRanges")
    private String pincodeRanges;

    public DeliveryZone() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPincodeRanges() {
        return pincodeRanges;
    }

    public void setPincodeRanges(String pincodeRanges) {
        this.pincodeRanges = pincodeRanges;
    }

    /**
     * Get formatted display string: "zoneName, city - pincode"
     * Extracts first pincode from pincodeRanges if it's JSON format
     */
    public String getDisplayText() {
        String pincode = extractFirstPincode(pincodeRanges);
        if (pincode != null && !pincode.isEmpty()) {
            return zoneName + ", " + city + " - " + pincode;
        } else {
            return zoneName + ", " + city;
        }
    }

    /**
     * Get the first pincode from pincodeRanges
     * Useful for auto-populating address forms
     */
    public String getFirstPincode() {
        return extractFirstPincode(pincodeRanges);
    }

    /**
     * Extract first pincode from pincodeRanges
     * Handles both JSON array format and simple string format
     */
    private String extractFirstPincode(String pincodeRanges) {
        if (pincodeRanges == null || pincodeRanges.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = pincodeRanges.trim();
        
        // If it's a JSON array like ["380001", "380002"]
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            try {
                // Simple extraction: remove brackets and quotes, get first value
                String content = trimmed.substring(1, trimmed.length() - 1);
                String[] parts = content.split(",");
                if (parts.length > 0) {
                    String first = parts[0].trim();
                    // Remove quotes if present
                    if (first.startsWith("\"") && first.endsWith("\"")) {
                        first = first.substring(1, first.length() - 1);
                    }
                    return first;
                }
            } catch (Exception e) {
                // If parsing fails, return as is
            }
        }
        
        // If it's a simple string or comma-separated, return first part
        String[] parts = trimmed.split(",");
        if (parts.length > 0) {
            return parts[0].trim();
        }
        
        return trimmed;
    }
}

