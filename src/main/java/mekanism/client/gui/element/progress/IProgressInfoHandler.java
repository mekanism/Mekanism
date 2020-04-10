package mekanism.client.gui.element.progress;

public interface IProgressInfoHandler {

    double getProgress();

    default boolean isActive() {
        return true;
    }
}