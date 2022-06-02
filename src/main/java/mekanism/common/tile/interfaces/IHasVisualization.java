package mekanism.common.tile.interfaces;

public interface IHasVisualization {

    boolean isClientRendering();

    void toggleClientRendering();

    default boolean canDisplayVisuals() {
        return true;
    }
}