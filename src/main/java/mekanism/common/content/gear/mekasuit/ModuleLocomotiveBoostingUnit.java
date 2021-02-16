package mekanism.common.content.gear.mekasuit;

import javax.annotation.Nonnull;
import mekanism.api.IIncrementalEnum;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.EnumData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
    public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        if (!isEnabled()) {
            return;
        }
        SprintBoost newMode = sprintBoost.get().adjust(shift);
        if (sprintBoost.get() != newMode) {
            sprintBoost.set(newMode, null);
            if (displayChangeMessage) {
                displayModeChange(player, MekanismLang.MODULE_SPRINT_BOOST.translate(), newMode);
            }
        }
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

    public enum SprintBoost implements IHasTextComponent, IIncrementalEnum<SprintBoost> {
        OFF(0),
        LOW(0.05F),
        MED(0.1F),
        HIGH(0.25F),
        ULTRA(0.5F);

        private static final SprintBoost[] MODES = values();

        private final float boost;
        private final ITextComponent label;

        SprintBoost(float boost) {
            this.boost = boost;
            this.label = new StringTextComponent(Float.toString(boost));
        }

        @Nonnull
        @Override
        public SprintBoost byIndex(int index) {
            return MathUtils.getByIndexMod(MODES, index);
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