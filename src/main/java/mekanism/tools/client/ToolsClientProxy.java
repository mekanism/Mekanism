package mekanism.tools.client;

import mekanism.tools.common.ToolsCommonProxy;
import mekanism.tools.common.ToolsItem;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

public class ToolsClientProxy extends ToolsCommonProxy {

    @Override
    public void registerItemRenders() {
        for (ToolsItem toolsItem : ToolsItem.values()) {
            ModelLoader.setCustomModelResourceLocation(toolsItem.getItem(), 0, new ModelResourceLocation(toolsItem.getItem().getRegistryName(), "inventory"));
        }
    }
}