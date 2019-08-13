package mekanism.tools.client;

import mekanism.tools.common.ToolsCommonProxy;

public class ToolsClientProxy extends ToolsCommonProxy {

    @Override
    public void registerItemRenders() {
        //TODO: I am pretty sure this is not needed anymore
        /*for (ToolsItem toolsItem : ToolsItem.values()) {
            ModelLoader.setCustomModelResourceLocation(toolsItem.getItem(), 0, new ModelResourceLocation(toolsItem.getItem().getRegistryName(), "inventory"));
        }*/
    }
}