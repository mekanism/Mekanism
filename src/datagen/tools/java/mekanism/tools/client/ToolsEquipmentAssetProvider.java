package mekanism.tools.client;

import java.util.function.BiConsumer;
import mekanism.tools.common.material.MaterialType;
import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;

public class ToolsEquipmentAssetProvider extends EquipmentAssetProvider {

    public ToolsEquipmentAssetProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void registerModels(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> output) {
        //TODO - 26.3: Make armor render on humanoid baby mobs. Either by figuring out how to convert the textures, or by adding baby textures
        for (MaterialType material : MaterialType.VALUES) {
            ResourceKey<EquipmentAsset> equipmentAsset = material.material.equipmentAsset();
            output.accept(equipmentAsset, EquipmentClientInfo.builder().addHumanoidLayers(equipmentAsset.identifier()).build());
        }
    }
}