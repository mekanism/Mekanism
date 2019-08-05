package mekanism.tools.client;

import mekanism.client.render.MekanismRenderer;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.ToolsCommonProxy;
import mekanism.tools.common.ToolsItem;

public class ToolsClientProxy extends ToolsCommonProxy {

    @Override
    public void registerItemRenders() {
        for (ToolsItem toolsItem : ToolsItem.values()) {
            MekanismRenderer.registerItemRender(MekanismTools.MODID, toolsItem.getItem());
        }
    }
}