package net.darkhax.tesla.api;

/**
 * Logic for a Tesla Consumer that will consume infinite amounts of power.
 * 
 * @deprecated Moved to net.darkhax.tesla.api.implementation
 */
@Deprecated
public class InfiniteTeslaConsumer implements ITeslaConsumer {
    
    @Override
    public long givePower (long power, boolean simulated) {
        
        return power;
    }
}