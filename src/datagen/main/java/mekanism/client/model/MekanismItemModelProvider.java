package mekanism.client.model;

import com.google.common.collect.Table.Cell;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;

public class MekanismItemModelProvider extends BaseItemModelProvider {

    public MekanismItemModelProvider(PackOutput output, ResourceManager clientResources) {
        super(output, Mekanism.MODID, clientResources);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        registerBuckets(MekanismFluids.FLUIDS);
        registerModules(MekanismItems.ITEMS);
        for (Cell<ResourceType, PrimaryResource, ItemRegistryObject<Item>> item : MekanismItems.PROCESSED_RESOURCES.cellSet()) {
            Identifier texture = itemTexture(item.getValue());
            if (textureExists(texture)) {
                generated(item.getValue(), texture);
            } else {
                //If the texture does not exist fallback to the default texture
                resource(item.getValue(), item.getRowKey().getRegistryPrefix());
            }
        }
    }
}