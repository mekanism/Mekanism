package net.darkhax.tesla.api;

public interface ITeslaHolder {
    
    /**
     * Gets the amount of Tesla power stored being stored.
     * 
     * @return The amount of Tesla power being stored.
     */
    long getStoredPower ();
    
    /**
     * Gets the maximum amount of Tesla power that can be held.
     * 
     * @return The maximum amount of Tesla power that can be held.
     */
    long getCapacity ();
}