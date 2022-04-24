package mekanism.client.gui.element.tab;

import mekanism.client.render.lib.ColorAtlas.ColorRegistryObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface TabType<TILE extends BlockEntity> {

    ResourceLocation getResource();

    void onClick(TILE tile);

    Component getDescription();

    default int getYPos() {
        return 6;
    }

    ColorRegistryObject getTabColor();
}