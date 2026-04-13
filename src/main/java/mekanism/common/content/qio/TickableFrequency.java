package mekanism.common.content.qio;

public interface TickableFrequency {
    /// @return `true` if persistent data was changed and the frequency needs to be saved.
    boolean tick(boolean tickingNormally);
}
