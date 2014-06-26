package rebelkeithy.mods.metallurgy.api;

import java.util.Map;

public interface IMetalSet
{
    public IOreInfo getOreInfo(int metadata);

    public IOreInfo getOreInfo(String name);

    public Map<String, IOreInfo> getOreList();
}
