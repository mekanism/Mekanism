package mekanism.common.block.states;

public interface IStateFacing {

    //TODO: Try to also add some sort of helpers from this for rotation (maybe fully move rotation out of TEs)

    /**
     * If false then assumes it can only do horizontals
     */
    default boolean supportsAll() {
        //TODO: Should we have it so that it returns a PropertyDirection instead
        return false;
    }
}