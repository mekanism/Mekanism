package mekanism.client.recipe_viewer.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tags.MekanismTags.BlockItems;
import mekanism.common.tier.FactoryTier;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

public interface IRecipeViewerRecipeType<RECIPE> extends IHasTextComponent {

    Identifier id();

    Class<? extends RECIPE> recipeClass();

    boolean requiresHolder();

    ItemStack iconStack();

    @Nullable
    Identifier icon();

    /// Offset to return the recipe viewer screen to a 0, 0 based coordinates compared to the normal mekanism gui. This number is almost always negative
    int xOffset();

    /// Offset to return the recipe viewer screen to a 0, 0 based coordinates compared to the normal mekanism gui. This number is almost always negative
    int yOffset();

    int width();

    int height();

    List<SlotDisplay> workstations();

    static List<SlotDisplay> asWorkStations(ItemLike... items) {
        List<SlotDisplay> allDisplays = new ArrayList<>();
        for (ItemLike itemLike : items) {
            Item item = itemLike.asItem();
            List<SlotDisplay> displays = new ArrayList<>();
            FactoryType factoryType = item.components().get(MekanismDataComponents.FACTORY_TYPE);
            if (factoryType != null) {
                Optional<Named<Item>> tag = Optional.ofNullable(BlockItems.FACTORY_SUPPORTED.get(factoryType))
                      .map(BlockItemTagId::item)
                      .flatMap(BuiltInRegistries.ITEM::get);
                if (tag.isPresent()) {
                    displays.add(new SlotDisplay.TagSlotDisplay(tag.get()));
                } else {
                    displays.add(new SlotDisplay.ItemSlotDisplay(item));
                    for (FactoryTier tier : FactoryTier.VALUES) {
                        displays.add(new SlotDisplay.ItemSlotDisplay(MekanismBlocks.getFactory(tier, factoryType).getItemHolder()));
                    }
                }
            } else {
                displays.add(new SlotDisplay.ItemSlotDisplay(item));
            }
            allDisplays.add(MekanismUtils.compactDisplay(displays));
        }
        return List.copyOf(allDisplays);
    }
}