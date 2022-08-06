package mekanism.common.content.gear.mekasuit;

import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

@ParametersAreNotNullByDefault
public class ModuleLocomotiveBoostingUnit implements ICustomModule<ModuleLocomotiveBoostingUnit> {

    private IModuleConfigItem<SprintBoost> sprintBoost;

    @Override
    public void init(IModule<ModuleLocomotiveBoostingUnit> module, ModuleConfigItemCreator configItemCreator) {
        sprintBoost = configItemCreator.createConfigItem("sprint_boost", MekanismLang.MODULE_SPRINT_BOOST,
              new ModuleEnumData<>(SprintBoost.LOW, module.getInstalledCount() + 1));
    }

    @Override
    public void changeMode(IModule<ModuleLocomotiveBoostingUnit> module, Player player, ItemStack stack, int shift, boolean displayChangeMessage) {
        SprintBoost currentMode = sprintBoost.get();
        SprintBoost newMode = currentMode.adjust(shift, v -> v.ordinal() < module.getInstalledCount() + 1);
        if (currentMode != newMode) {
            sprintBoost.set(newMode);
            if (displayChangeMessage) {
                module.displayModeChange(player, MekanismLang.MODULE_SPRINT_BOOST.translate(), newMode);
            }
        }
    }

    @Override
    public void tickServer(IModule<ModuleLocomotiveBoostingUnit> module, Player player) {
        if (tick(module, player)) {
            module.useEnergy(player, MekanismConfig.gear.mekaSuitEnergyUsageSprintBoost.get().multiply(getBoost() / 0.1F));
        }
    }

    @Override
    public void tickClient(IModule<ModuleLocomotiveBoostingUnit> module, Player player) {
        // leave energy usage up to server
        tick(module, player);
    }

    private boolean tick(IModule<ModuleLocomotiveBoostingUnit> module, Player player) {
        if (canFunction(module, player)) {
            float boost = getBoost();
            if (!player.isOnGround()) {
                boost /= 5F; // throttle if we're in the air
            }
            if (player.isInWater()) {
                boost /= 5F; // throttle if we're in the water
            }
            player.moveRelative(boost, new Vec3(0, 0, 1));
            return true;
        }
        return false;
    }

    public boolean canFunction(IModule<ModuleLocomotiveBoostingUnit> module, Player player) {
        //Don't allow boosting unit to work when flying with the elytra, a jetpack should be used instead
        return !player.isFallFlying() && player.isSprinting() && module.canUseEnergy(player,
              MekanismConfig.gear.mekaSuitEnergyUsageSprintBoost.get().multiply(getBoost() / 0.1F));
    }

    public float getBoost() {
        return sprintBoost.get().getBoost();
    }

    @NothingNullByDefault
    public enum SprintBoost implements IHasTextComponent, IIncrementalEnum<SprintBoost> {
        OFF(0),
        LOW(0.05F),
        MED(0.1F),
        HIGH(0.25F),
        ULTRA(0.5F);

        private static final SprintBoost[] MODES = values();

        private final float boost;
        private final Component label;

        SprintBoost(float boost) {
            this.boost = boost;
            this.label = TextComponentUtil.getString(Float.toString(boost));
        }

        @Override
        public SprintBoost byIndex(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }

        @Override
        public Component getTextComponent() {
            return label;
        }

        public float getBoost() {
            return boost;
        }
    }
}