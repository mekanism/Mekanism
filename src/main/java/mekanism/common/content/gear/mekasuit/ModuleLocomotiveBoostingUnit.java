package mekanism.common.content.gear.mekasuit;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.IIncrementalEnum;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;

@ParametersAreNonnullByDefault
public class ModuleLocomotiveBoostingUnit implements ICustomModule<ModuleLocomotiveBoostingUnit> {

    private IModuleConfigItem<SprintBoost> sprintBoost;

    @Override
    public void init(IModule<ModuleLocomotiveBoostingUnit> module, ModuleConfigItemCreator configItemCreator) {
        sprintBoost = configItemCreator.createConfigItem("sprint_boost", MekanismLang.MODULE_SPRINT_BOOST,
              new ModuleEnumData<>(SprintBoost.class, module.getInstalledCount() + 1, SprintBoost.LOW));
    }

    @Override
    public void changeMode(IModule<ModuleLocomotiveBoostingUnit> module, PlayerEntity player, ItemStack stack, int shift, boolean displayChangeMessage) {
        if (module.isEnabled()) {
            SprintBoost newMode = sprintBoost.get().adjust(shift);
            if (sprintBoost.get() != newMode) {
                sprintBoost.set(newMode);
                if (displayChangeMessage) {
                    module.displayModeChange(player, MekanismLang.MODULE_SPRINT_BOOST.translate(), newMode);
                }
            }
        }
    }

    @Override
    public void tickServer(IModule<ModuleLocomotiveBoostingUnit> module, PlayerEntity player) {
        if (canFunction(module, player)) {
            float boost = getBoost();
            if (!player.isOnGround()) {
                boost /= 5F; // throttle if we're in the air
            }
            if (player.isInWater()) {
                boost /= 5F; // throttle if we're in the water
            }
            player.moveRelative(boost, new Vector3d(0, 0, 1));
            module.useEnergy(player, MekanismConfig.gear.mekaSuitEnergyUsageSprintBoost.get().multiply(getBoost() / 0.1F));
        }
    }

    @Override
    public void tickClient(IModule<ModuleLocomotiveBoostingUnit> module, PlayerEntity player) {
        if (canFunction(module, player)) {
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

    public boolean canFunction(IModule<ModuleLocomotiveBoostingUnit> module, PlayerEntity player) {
        FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsageSprintBoost.get().multiply(getBoost() / 0.1F);
        return player.isSprinting() && module.canUseEnergy(player, usage);
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
            this.label = TextComponentUtil.getString(Float.toString(boost));
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