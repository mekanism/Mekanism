package mekanism.client.integration.gender;

import com.wildfire.api.IGenderArmor;
import com.wildfire.api.data.GenderArmorProvider;
import com.wildfire.api.impl.GenderArmor;
import java.util.concurrent.CompletableFuture;
import mekanism.common.registries.MekanismEquipmentAssets;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

public class MekanismGenderArmorProvider extends GenderArmorProvider {

    public MekanismGenderArmorProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modid) {
        super(output, registries, modid);
    }

    @Override
    protected void addDefaults(HolderLookup.Provider lookupProvider) {
        add(MekanismEquipmentAssets.HAZMAT, new GenderArmor(0.5F, 0.25F));
        add(MekanismEquipmentAssets.JETPACK, IGenderArmor.EMPTY);
        add(MekanismEquipmentAssets.SCUBA_GEAR, IGenderArmor.EMPTY);
        add(MekanismEquipmentAssets.HDPE_ELYTRA, IGenderArmor.EMPTY);
        IGenderArmor hidesBreasts = new IGenderArmor() {
            @Override
            public boolean alwaysHidesBreasts() {
                return true;
            }

            @Override
            public float physicsResistance() {
                return 0;
            }
        };
        add(MekanismEquipmentAssets.ARMORED_JETPACK, hidesBreasts);
        add(MekanismEquipmentAssets.MEKASUIT, hidesBreasts);
    }
}