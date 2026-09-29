package mekanism.tools.client;

import java.util.function.BiConsumer;
import mekanism.tools.client.ToolsModelProvider.PaletteTransformData;
import mekanism.tools.common.material.MaterialType;
import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.resources.model.EquipmentClientInfo.Builder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;

public class ToolsEquipmentAssetProvider extends EquipmentAssetProvider {

    public ToolsEquipmentAssetProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void registerModels(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> output) {
        for (MaterialType material : MaterialType.VALUES) {
            ResourceKey<EquipmentAsset> equipmentAsset = material.material.equipmentAsset();
            Builder builder = humanoidAndMountArmor(equipmentAsset.identifier().toString());
            PaletteTransformData transform = ToolsModelProvider.getTransform(material);
            if (transform != null) {
                builder.replaceTrimPalette(transform.material(), transform.replacement());
            }
            output.accept(equipmentAsset, builder.build());
        }
    }
}