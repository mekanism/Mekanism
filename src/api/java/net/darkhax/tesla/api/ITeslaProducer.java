package net.darkhax.tesla.api;

public interface ITeslaProducer {
    
    /**
     * Requests an amount of power from the Tesla Producer.
     * 
     * @param power The amount of power to request.
     * @param simulated Whether or not this is being called as part of a simulation.
     *        Simulations are used to get information without affecting the Tesla Producer.
     * @return The amount of power that the Tesla Producer will give.
     */
    long takePower (long power, boolean simulated);
}