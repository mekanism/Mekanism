package net.darkhax.tesla.api;

public interface ITeslaConsumer {
    
    /**
     * Offers power to the Tesla Consumer.
     * 
     * @param power The amount of power to offer.
     * @param simulated Whether or not this is being called as part of a simulation.
     *        Simulations are used to get information without affecting the Tesla Producer.
     * @return The amount of power that the consumer accepts.
     */
    long givePower (long power, boolean simulated);
}
