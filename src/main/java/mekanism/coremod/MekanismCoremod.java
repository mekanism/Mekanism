package mekanism.coremod;

import java.util.Map;
import javax.annotation.Nullable;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;

@SortingIndex(9999)//must be > 1000 so we're after the srg transformer
public class MekanismCoremod implements IFMLLoadingPlugin {

    private static final String[] transformers = {
          "mekanism.coremod.KeybindingMigrationHelper"
    };

    @Override
    public String[] getASMTransformerClass() {
        return transformers;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
