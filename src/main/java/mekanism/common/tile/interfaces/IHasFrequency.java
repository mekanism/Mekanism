package mekanism.common.tile.interfaces;

public interface IHasFrequency {

    void setFrequency(String name, boolean publicFreq);

    void removeFrequency(String name, boolean publicFreq);
}