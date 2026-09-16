package mekanism.tools.client.integration.gender;

import com.wildfire.api.data.GenderArmorProvider;
import com.wildfire.api.impl.GenderArmor;
import java.util.concurrent.CompletableFuture;
import mekanism.tools.common.material.MaterialType;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

public class ToolsGenderArmorProvider extends GenderArmorProvider {

    public ToolsGenderArmorProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modid) {
        super(output, registries, modid);
    }

    @Override
    protected void addDefaults(HolderLookup.Provider lookupProvider) {
        add(MaterialType.BRONZE.material.equipmentAsset(), new GenderArmor(0.9F, 0, true));
        add(MaterialType.LAPIS_LAZULI.material.equipmentAsset(), new GenderArmor(0.6F, 0.1F));
        add(MaterialType.OSMIUM.material.equipmentAsset(), new GenderArmor(1, 0));
        add(MaterialType.REFINED_GLOWSTONE.material.equipmentAsset(), new GenderArmor(0.95F, 0, true));
        add(MaterialType.REFINED_OBSIDIAN.material.equipmentAsset(), new GenderArmor(1, 0));
        add(MaterialType.STEEL.material.equipmentAsset(), new GenderArmor(1, 0));
    }
}