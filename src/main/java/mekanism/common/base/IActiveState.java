package mekanism.common.base;

/**
 * Implement this if your machine/generator has some form of active state.
 *
 * @author aidancbrady
 */
public interface IActiveState {

    /**
     * Gets the active state as a boolean.
     *
     * @return active state
     */
    boolean getActive();

    /**
     * Sets the active state to a new value.
     *
     * @param active - new active state
     */
    void setActive(boolean active);

    /**
     * Determine if a machine/generator was "recently" active, where "recently" is up to the interface
     * implementor. This is useful for reducing rendering calls, esp. when the entity can oscillate between
     * active/inactive states rapidly.
     *
     * The default implementation just returns current active state.
     *
     */
    default boolean wasActiveRecently() { return getActive(); }

    /**
     * Whether or not this block has a visual effect when it is on it's active state. Used for rendering.
     *
     * @return if the block has a visual effect in it's active state
     */
    boolean renderUpdate();

    boolean lightUpdate();
}
