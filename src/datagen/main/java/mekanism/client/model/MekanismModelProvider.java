package mekanism.client.model;

import com.google.common.collect.Table;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemModule;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MekanismModelProvider extends BaseModelProvider {

    public MekanismModelProvider(PackOutput packOutput, ResourceManager clientResources) {
        super(packOutput, Mekanism.MODID, clientResources);
    }

    protected void resource(Holder<Item> item, String type, int tint, ItemModelGenerators itemModels) {
        Material base = new Material(modLoc("item/" + type));
        Material overlay = new Material(modLoc("item/" + type + "_overlay"));
        boolean hasOverlay = textureExists(overlay);
        ModelTemplate template = hasOverlay ? ModelTemplates.TWO_LAYERED_ITEM : ModelTemplates.FLAT_ITEM;
        Item item1 = item.value();
        TextureMapping textures = hasOverlay ? TextureMapping.layered(base, overlay) : TextureMapping.layer0(base);

        Identifier generatedModel = template.create(ModelLocationUtils.getModelLocation(item1), textures, itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item1, ItemModelUtils.tintedModel(generatedModel, new Constant(tint)));
    }

    protected void registerModules(ItemDeferredRegister register, ItemModelGenerators itemModels) {
        for (Holder<Item> itemProvider : register.getEntries()) {
            if (itemProvider.value() instanceof ItemModule module) {
                itemModels.generateFlatItem(module, ModelTemplates.FLAT_ITEM);
            }
        }
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        registerBuckets(MekanismFluids.FLUIDS, itemModels);
        registerModules(MekanismItems.ITEMS, itemModels);
        for (Table.Cell<ResourceType, PrimaryResource, ItemRegistryObject<Item>> item : MekanismItems.PROCESSED_RESOURCES.cellSet()) {
            ItemRegistryObject<Item> itemValue = item.getValue();
            Identifier texture = itemTexture(itemValue);
            //NB: as at time of creation, all our base materials have an override
            if (textureExists(texture)) {
                itemModels.generateFlatItem(itemValue.asItem(), ModelTemplates.FLAT_ITEM);
            } else {
                //If the texture does not exist fallback to the default texture
                resource(itemValue, item.getRowKey().getRegistryPrefix(), item.getColumnKey().getTint(), itemModels);
            }
        }
    }
}
