package mekanism.api;

public enum AutomationType {
    /**
     * External interaction (third party interacting with a machine)
     */
    EXTERNAL,
    /**
     * Internal interaction (machine interacting with its own contents)
     */
    INTERNAL,
    /**
     * Manual interaction (player interacting manually, such as in a GUI)
     */
    MANUAL;
}