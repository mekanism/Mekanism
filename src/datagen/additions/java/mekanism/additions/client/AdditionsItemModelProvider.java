package mekanism.additions.client;

import java.util.Map;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.client.model.BaseItemModelProvider;
import mekanism.common.registration.INamedEntry;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class AdditionsItemModelProvider extends BaseItemModelProvider {

    public AdditionsItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MekanismAdditions.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        withParent(AdditionsItems.BALLOONS, "item/balloon");
        withParent(AdditionsBlocks.GLOW_PANELS, "item/glow_panel");
        withParent(AdditionsBlocks.PLASTIC_BLOCKS, "block/plastic/block");
        withParent(AdditionsBlocks.SLICK_PLASTIC_BLOCKS, "block/plastic/slick");
        withParent(AdditionsBlocks.PLASTIC_GLOW_BLOCKS, "block/plastic/glow");
        withParent(AdditionsBlocks.REINFORCED_PLASTIC_BLOCKS, "block/plastic/reinforced");
        withParent(AdditionsBlocks.PLASTIC_ROADS, "block/plastic/glow");
        withParent(AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS, "block/plastic/transparent");
        withParent(AdditionsBlocks.PLASTIC_STAIRS, "block/plastic/stairs");
        withParent(AdditionsBlocks.PLASTIC_SLABS, "block/plastic/slab");
        withParent(AdditionsBlocks.PLASTIC_FENCES, "block/plastic/fence_inventory");
        withParent(AdditionsBlocks.PLASTIC_FENCE_GATES, "block/plastic/fence_gate");
        withParent(AdditionsBlocks.PLASTIC_GLOW_STAIRS, "block/plastic/glow_stairs");
        withParent(AdditionsBlocks.PLASTIC_GLOW_SLABS, "block/plastic/glow_slab");
        withParent(AdditionsBlocks.TRANSPARENT_PLASTIC_STAIRS, "block/plastic/transparent_stairs");
        withParent(AdditionsBlocks.TRANSPARENT_PLASTIC_SLABS, "block/plastic/transparent_slab");
    }

    private void withParent(Map<?, ? extends INamedEntry> items, String modelName) {
        ModelFile parent = getExistingFile(modLoc(modelName));
        for (INamedEntry item : items.values()) {
            getBuilder(item.getName()).parent(parent);
        }
    }
}