package mekanism.common.content.gear.mekasuit;

import mekanism.api.math.FloatingLong;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.EnumData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ModuleLocomotiveBoostingUnit extends ModuleMekaSuit {

    private ModuleConfigItem<SprintBoost> sprintBoost;

    @Override
    public void init() {
        super.init();
        addConfigItem(sprintBoost = new ModuleConfigItem<>(this, "sprint_boost", MekanismLang.MODULE_SPRINT_BOOST, new EnumData<>(SprintBoost.class, getInstalledCount() + 1), SprintBoost.LOW));
    }

    @Override
    public void tickServer(PlayerEntity player) {
        super.tickServer(player);

        if (canFunction(player)) {
            float boost = getBoost();
            if (!player.isOnGround()) {
                boost /= 5F; // throttle if we're in the air
            }
            if (player.isInWater()) {
                boost /= 5F; // throttle if we're in the water
            }
            player.moveRelative(boost, new Vector3d(0, 0, 1));
            useEnergy(player, MekanismConfig.gear.mekaSuitEnergyUsageSprintBoost.get().multiply(getBoost() / 0.1F));
        }
    }

    @Override
    public void tickClient(PlayerEntity player) {
        super.tickClient(player);

        if (canFunction(player)) {
            float boost = getBoost();
            if (!player.isOnGround()) {
                boost /= 5F; // throttle if we're in the air
            }
            if (player.isInWater()) {
                boost /= 5F; // throttle if we're in the water
            }
            player.moveRelative(boost, new Vector3d(0, 0, 1));
            // leave energy usage up to server
        }
    }

    public boolean canFunction(PlayerEntity player) {
        FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsageSprintBoost.get().multiply(getBoost() / 0.1F);
        return player.isSprinting() && getContainerEnergy().greaterOrEqual(usage);
    }

    public float getBoost() {
        return sprintBoost.get().getBoost();
    }

    public enum SprintBoost implements IHasTextComponent {
        OFF(0),
        LOW(0.05F),
        MED(0.1F),
        HIGH(0.25F),
        ULTRA(0.5F);

        private final float boost;
        private final ITextComponent label;

        SprintBoost(float boost) {
            this.boost = boost;
            this.label = new StringTextComponent(Float.toString(boost));
        }

        @Override
        public ITextComponent getTextComponent() {
            return label;
        }

        public float getBoost() {
            return boost;
        }
    }
}