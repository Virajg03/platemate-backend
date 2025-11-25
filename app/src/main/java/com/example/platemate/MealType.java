package com.example.platemate;

public enum MealType {
    VEG, NON_VEG, JAIN;
    
    @Override
    public String toString() {
        return name();
    }
}

