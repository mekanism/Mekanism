package mekanism.client.model;

import com.google.common.collect.Table.Cell;
import java.util.Map;
import mekanism.common.Mekanism;
import mekanism.common.content.gear.Modules.ModuleData;
import mekanism.common.item.ItemModule;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

public class MekanismItemModelProvider extends BaseItemModelProvider {

    public MekanismItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Mekanism.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //Buckets
        MekanismFluids.FLUIDS.getAllFluids().forEach(this::registerBucket);

        for (Cell<ResourceType, PrimaryResource, ItemRegistryObject<Item>> item : MekanismItems.PROCESSED_RESOURCES.cellSet()) {
            ResourceLocation texture = itemTexture(item.getValue());
            if (textureExists(texture)) {
                generated(item.getValue(), texture);
            } else {
                //If the texture does not exist fallback to the default texture
                resource(item.getValue(), item.getRowKey().getRegistryPrefix());
            }
        }

        for (Map.Entry<ModuleData<?>, ItemRegistryObject<? extends ItemModule>> entry : MekanismItems.MODULES.entrySet()) {
            generated(entry.getValue());
        }
    }
}