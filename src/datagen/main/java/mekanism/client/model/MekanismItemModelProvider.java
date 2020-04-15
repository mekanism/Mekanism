package mekanism.client.model;

import java.util.Map;
import com.google.common.collect.Table.Cell;
import mekanism.common.Mekanism;
import mekanism.common.content.gear.Modules.ModuleData;
import mekanism.common.item.ItemModule;
import mekanism.common.item.ItemProcessedResource;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

public class MekanismItemModelProvider extends BaseItemModelProvider {

    public MekanismItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Mekanism.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //Buckets
        MekanismFluids.FLUIDS.getAllFluids().forEach(this::registerBucket);

        for (Cell<ResourceType, PrimaryResource, ItemRegistryObject<? extends ItemProcessedResource>> item : MekanismItems.PROCESSED_RESOURCES.cellSet()) {
            if (!item.getColumnKey().hasTextureOverride()) {
                generated(item.getValue(), modLoc("item/" + item.getRowKey().getRegistryPrefix()));
            } else {
                generated(item.getValue());
            }
        }

        for (Map.Entry<ModuleData<?>, ItemRegistryObject<? extends ItemModule>> entry : MekanismItems.MODULES.entrySet()) {
            generated(entry.getValue());
        }
    }
}