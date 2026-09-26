package mekanism.client.model;

import java.util.Optional;
import java.util.function.BiConsumer;
import mekanism.common.registries.MekanismEquipmentAssets;
import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;

public class MekanismEquipmentAssetProvider extends EquipmentAssetProvider {

    public MekanismEquipmentAssetProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void registerModels(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> consumer) {
        consumer.accept(MekanismEquipmentAssets.HDPE_ELYTRA, EquipmentClientInfo.builder()
              .addLayers(EquipmentClientInfo.LayerType.WINGS, new EquipmentClientInfo.Layer(MekanismEquipmentAssets.HDPE_ELYTRA.identifier(), Optional.empty(), true))
              .build()
        );
        //TODO - 26.3: Make armor render on humanoid baby mobs. Either by figuring out how to convert the textures, or by adding baby textures
        consumer.accept(MekanismEquipmentAssets.HAZMAT, EquipmentClientInfo.builder().addHumanoidLayers(MekanismEquipmentAssets.HAZMAT.identifier()).build());
    }
}