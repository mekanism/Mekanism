package mekanism.client.gui.element.progress;

public interface IProgressInfoHandler {

    float getProgress();

    default boolean isActive() {
        return true;
    }

    interface IBooleanProgressInfoHandler extends IProgressInfoHandler {

        boolean fillProgressBar();

        @Override
        default float getProgress() {
            return fillProgressBar() ? 1 : 0;
        }
    }
}