package rebelkeithy.mods.metallurgy.api;

public enum OreType
{

    ORE(true), CATALYST(true), ALLOY(false), RESPAWN(true), DROP(true);

    private boolean generates;

    OreType(boolean generates)
    {
        this.generates = generates;
    }

    public boolean generates()
    {
        return generates;
    }
}
