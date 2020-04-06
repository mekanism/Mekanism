package mekanism.tools.client;

import mekanism.api.providers.IItemProvider;
import mekanism.client.model.BaseItemModelProvider;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.item.ItemMekanismPaxel;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

public class ToolsItemModelProvider extends BaseItemModelProvider {

    public ToolsItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, MekanismTools.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //Armor items are generated textures, all other tools module items are handheld
        for (IItemProvider itemProvider : ToolsItems.ITEMS.getAllItems()) {
            ResourceLocation texture;
            if (isVanilla(itemProvider)) {
                texture = itemTexture(itemProvider);
            } else {
                String name = itemProvider.getName();
                int index = name.lastIndexOf('_');
                texture = modLoc("item/" + name.substring(0, index) + '/' + name.substring(index + 1));
            }
            if (itemProvider.getItem() instanceof ArmorItem) {
                generated(itemProvider, texture);
            } else {
                handheld(itemProvider, texture);
            }
        }
    }

    private boolean isVanilla(IItemProvider itemProvider) {
        if (itemProvider.getItem() instanceof ItemMekanismPaxel) {
            String name = itemProvider.getName();
            return name.startsWith("diamond") || name.startsWith("gold") || name.startsWith("iron") || name.startsWith("stone") || name.startsWith("wood");
        }
        return false;
    }
}