package codechicken.multipart.nei;

import codechicken.lib.lang.LangUtil;
import codechicken.microblock.MicroblockClass;
import codechicken.microblock.MicroblockClassRegistry;
import codechicken.microblock.handler.MicroblockProxy;
import codechicken.nei.MultiItemRange;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

public class NEI_MicroblockConfig implements IConfigureNEI
{
    @Override
    public void loadConfig()
    {
        int microID = MicroblockProxy.itemMicro().itemID;
        
        MicroblockClass[] microClasses = MicroblockClassRegistry.classes();
        for(int c = 0; c < microClasses.length; c++)
        {
            MicroblockClass mcrClass = microClasses[c];
            if(mcrClass == null)
                continue;

            addSubset(mcrClass, microID, c<<8|1);
            addSubset(mcrClass, microID, c<<8|2);
            addSubset(mcrClass, microID, c<<8|4);
        }
    }

    private void addSubset(MicroblockClass mcrClass, int microID, int i)
    {
        API.addSetRange("Microblocks."+LangUtil.translateG(mcrClass.getName()+"."+(i&0xFF)+".subset"), 
                new MultiItemRange().add(microID, i, i));
    }

    @Override
    public String getName()
    {
        return "ForgeMultipart";
    }

    @Override
    public String getVersion()
    {
        return "1.0.0.0";
    }
}
