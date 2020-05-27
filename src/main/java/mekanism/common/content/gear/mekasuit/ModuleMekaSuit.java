package mekanism.common.content.gear.mekasuit;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Action;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.BooleanData;
import mekanism.common.content.gear.ModuleConfigItem.EnumData;
import mekanism.common.content.gear.Modules;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleLocomotiveBoostingUnit.SprintBoost;
import mekanism.common.distribution.target.EnergySaveTarget;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.lib.radiation.RadiationManager.RadiationScale;
import mekanism.common.lib.radiation.capability.IRadiationEntity;
import mekanism.common.particle.custom.BoltEffect;
import mekanism.common.particle.custom.BoltEffect.BoltRenderInfo;
import mekanism.common.particle.custom.BoltEffect.SpawnFunction;
import mekanism.common.particle.custom.BoltEffect.SpawnFunction;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.RadiationUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public abstract class ModuleMekaSuit extends Module {

    public static class ModuleElectrolyticBreathingUnit extends ModuleMekaSuit {

        @Override
        public void tickServer(PlayerEntity player) {
            FloatingLong usage = MekanismConfig.general.FROM_H2.get().multiply(2);
            long maxRate = Math.min(getMaxRate(), getContainerEnergy().divide(usage).intValue());
            long hydrogenUsed = 0;
            GasStack hydrogenStack = new GasStack(MekanismGases.HYDROGEN.get(), maxRate * 2);
            ItemStack chestStack = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            Optional<IGasHandler> capability = MekanismUtils.toOptional(chestStack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
            if (Modules.load(chestStack, Modules.JETPACK_UNIT) != null && capability.isPresent()) {
                hydrogenUsed = maxRate * 2 - capability.get().insertChemical(hydrogenStack, Action.EXECUTE).getAmount();
            }
            long oxygenUsed = Math.min(maxRate, player.getMaxAir() - player.getAir());
            long used = Math.max((int) Math.ceil(hydrogenUsed / 2D), oxygenUsed);
            useEnergy(player, usage.multiply(used));
            player.setAir(player.getAir() + (int) oxygenUsed);
        }

        private int getMaxRate() {
            return (int) Math.pow(2, getInstalledCount());
        }
    }

    public static class ModuleInhalationPurificationUnit extends ModuleMekaSuit {

        @Override
        public void tickServer(PlayerEntity player) {
            for (EffectInstance effect : player.getActivePotionEffects()) {
                if (getContainerEnergy().smallerThan(MekanismConfig.gear.mekaSuitEnergyUsagePotionTick.get())) {
                    break;
                }
                useEnergy(player, MekanismConfig.gear.mekaSuitEnergyUsagePotionTick.get());
                for (int i = 0; i < 9; i++) {
                    effect.tick(player, () -> MekanismUtils.onChangedPotionEffect(player, effect, true));
                }
            }
        }
    }

    public static class ModuleVisionEnhancementUnit extends ModuleMekaSuit {

        @Override
        public void tickServer(PlayerEntity player) {
            super.tickServer(player);
            useEnergy(player, MekanismConfig.gear.mekaSuitEnergyUsageVisionEnhancement.get());
        }

        @Override
        public void addHUDStrings(List<ITextComponent> list) {
            ILangEntry lang = isEnabled() ? MekanismLang.MODULE_ENABLED_LOWER : MekanismLang.MODULE_DISABLED_LOWER;
            list.add(MekanismLang.GENERIC_STORED.translateColored(EnumColor.DARK_GRAY, EnumColor.DARK_GRAY, MekanismLang.MODULE_VISION_ENHANCEMENT,
                  isEnabled() ? EnumColor.BRIGHT_GREEN : EnumColor.DARK_RED, lang.translate()));
        }

        @Override
        public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
            toggleEnabled(player, MekanismLang.MODULE_VISION_ENHANCEMENT.translate());
        }
    }

    public static class ModuleRadiationShieldingUnit extends ModuleMekaSuit {}

    public static class ModuleGravitationalModulatingUnit extends ModuleMekaSuit {

        // we share with locomotive boosting unit
        private ModuleConfigItem<SprintBoost> speedBoost;

        @Override
        public void init() {
            super.init();
            addConfigItem(speedBoost = new ModuleConfigItem<>(this, "speed_boost", MekanismLang.MODULE_SPEED_BOOST, new EnumData<>(SprintBoost.class), SprintBoost.LOW));
        }

        @Override
        public void addHUDStrings(List<ITextComponent> list) {
            ILangEntry lang = isEnabled() ? MekanismLang.MODULE_ENABLED_LOWER : MekanismLang.MODULE_DISABLED_LOWER;
            list.add(MekanismLang.GENERIC_STORED.translateColored(EnumColor.DARK_GRAY, EnumColor.DARK_GRAY, MekanismLang.MODULE_GRAVITATIONAL_MODULATION,
                  isEnabled() ? EnumColor.BRIGHT_GREEN : EnumColor.DARK_RED, lang.translate()));
        }

        @Override
        public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
            toggleEnabled(player, MekanismLang.MODULE_GRAVITATIONAL_MODULATION.translate());
        }

        public float getBoost() {
            return speedBoost.get().getBoost();
        }
    }

    public static class ModuleChargeDistributionUnit extends ModuleMekaSuit {

        private ModuleConfigItem<Boolean> chargeSuit;
        private ModuleConfigItem<Boolean> chargeInventory;

        @Override
        public void init() {
            super.init();
            chargeSuit = addConfigItem(new ModuleConfigItem<>(this, "charge_suit", MekanismLang.MODULE_CHARGE_SUIT, new BooleanData(), true));
            chargeInventory = addConfigItem(new ModuleConfigItem<>(this, "charge_inventory", MekanismLang.MODULE_CHARGE_INVENTORY, new BooleanData(), false));
        }

        @Override
        public void tickServer(PlayerEntity player) {
            super.tickServer(player);
            // charge inventory first
            if (chargeInventory.get()) {
                chargeInventory(player);
            }
            // distribute suit charge next
            if (chargeSuit.get()) {
                chargeSuit(player);
            }
        }

        private void chargeSuit(PlayerEntity player) {
            Set<EnergySaveTarget> saveTargets = new ObjectOpenHashSet<>();
            FloatingLong total = FloatingLong.ZERO;
            for (ItemStack stack : player.inventory.armorInventory) {
                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                if (energyContainer != null) {
                    EnergySaveTarget saveTarget = new EnergySaveTarget();
                    saveTarget.addHandler(Direction.NORTH, energyContainer);
                    saveTargets.add(saveTarget);
                    total = total.plusEqual(energyContainer.getEnergy());
                }
            }
            EmitUtils.sendToAcceptors(saveTargets, saveTargets.size(), total.copy());
            for (EnergySaveTarget saveTarget : saveTargets) {
                saveTarget.save(Direction.NORTH);
            }
        }

        private void chargeInventory(PlayerEntity player) {
            FloatingLong toCharge = MekanismConfig.gear.mekaSuitInventoryChargeRate.get();
            // first try to charge mainhand/offhand item
            toCharge = charge(player, player.getHeldItemMainhand(), toCharge);
            toCharge = charge(player, player.getHeldItemOffhand(), toCharge);

            for (ItemStack stack : player.inventory.mainInventory) {
                if (stack == player.getHeldItemMainhand() || stack == player.getHeldItemOffhand()) {
                    continue;
                }
                if (toCharge.isZero()) {
                    break;
                }
                toCharge = charge(player, stack, toCharge);
            }
        }

        /** return rejects */
        private FloatingLong charge(PlayerEntity player, ItemStack stack, FloatingLong amount) {
            IStrictEnergyHandler handler = EnergyCompatUtils.getStrictEnergyHandler(stack);
            if (handler != null) {
                return handler.insertEnergy(useEnergy(player, amount), Action.EXECUTE);
            }
            return amount;
        }
    }

    public static class ModuleLocomotiveBoostingUnit extends ModuleMekaSuit {

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
                if (!player.onGround) {
                    boost /= 5F; // throttle if we're in the air
                }
                if (player.isInWater()) {
                    boost /= 5F; // throttle if we're in the water
                }
                player.moveRelative(boost, new Vec3d(0, 0, 1));
                useEnergy(player, MekanismConfig.gear.mekaSuitEnergyUsageSprintBoost.get().multiply(getBoost() / 0.1F));
            }
        }

        @Override
        public void tickClient(PlayerEntity player) {
            super.tickClient(player);

            if (canFunction(player)) {
                float boost = getBoost();
                if (!player.onGround) {
                    boost /= 5F; // throttle if we're in the air
                }
                if (player.isInWater()) {
                    boost /= 5F; // throttle if we're in the water
                }
                player.moveRelative(boost, new Vec3d(0, 0, 1));
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

    public static class ModuleHydraulicAbsorptionUnit extends ModuleMekaSuit {}

    public static class ModuleHydraulicPropulsionUnit extends ModuleMekaSuit {

        private ModuleConfigItem<JumpBoost> jumpBoost;

        @Override
        public void init() {
            super.init();
            addConfigItem(jumpBoost = new ModuleConfigItem<>(this, "jump_boost", MekanismLang.MODULE_JUMP_BOOST, new EnumData<>(JumpBoost.class, getInstalledCount() + 1), JumpBoost.LOW));
        }

        public float getBoost() {
            return jumpBoost.get().getBoost();
        }

        public enum JumpBoost implements IHasTextComponent {
            OFF(0),
            LOW(0.5F),
            MED(1F),
            HIGH(3),
            ULTRA(5);

            private final float boost;
            private final ITextComponent label;

            JumpBoost(float boost) {
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

    public static class ModuleSolarRechargingUnit extends ModuleMekaSuit {

        @Override
        public void tickServer(PlayerEntity player) {
            super.tickServer(player);
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(getContainer(), 0);
            if (energyContainer != null && !energyContainer.getNeeded().isZero() && player.world.isDaytime() &&
                player.world.canSeeSky(new BlockPos(player)) && !player.world.isRaining() && !player.world.getDimension().isNether()) {
                FloatingLong rate = MekanismConfig.gear.mekaSuitSolarRechargingRate.get().multiply(getInstalledCount());
                energyContainer.insert(rate, Action.EXECUTE, AutomationType.MANUAL);
            }
        }
    }

    public static class ModuleNutritionalInjectionUnit extends ModuleMekaSuit {

        @Override
        public void tickServer(PlayerEntity player) {
            super.tickServer(player);
            FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsageNutritionalInjection.get();
            if (!player.isCreative() && player.canEat(false) && getContainerEnergy().greaterOrEqual(usage)) {
                ItemMekaSuitArmor item = (ItemMekaSuitArmor) getContainer().getItem();
                long toFeed = Math.min(1, item.getContainedGas(getContainer(), MekanismGases.NUTRITIONAL_PASTE.get()).getAmount() / MekanismConfig.general.nutritionalPasteMBPerFood.get());
                if (toFeed > 0) {
                    useEnergy(player, usage.multiply(toFeed));
                    item.useGas(getContainer(), MekanismGases.NUTRITIONAL_PASTE.get(), toFeed * MekanismConfig.general.nutritionalPasteMBPerFood.get());
                    player.getFoodStats().addStats(1, MekanismConfig.general.nutritionalPasteSaturation.get());
                }
            }
        }

        @Override
        public void addHUDStrings(List<ITextComponent> list) {
            if (!isEnabled()) {
                return;
            }
            GasStack stored = ((ItemMekaSuitArmor) getContainer().getItem()).getContainedGas(getContainer(), MekanismGases.NUTRITIONAL_PASTE.get());
            list.add(MekanismLang.GENERIC_STORED.translateColored(EnumColor.DARK_GRAY, MekanismGases.NUTRITIONAL_PASTE, EnumColor.PINK, stored.getAmount()));
        }
    }

    public static class ModuleDosimeterUnit extends ModuleMekaSuit {

        @Override
        public void addHUDStrings(List<ITextComponent> list) {
            PlayerEntity player = Minecraft.getInstance().player;
            Optional<IRadiationEntity> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(player, Capabilities.RADIATION_ENTITY_CAPABILITY, null));
            if (capability.isPresent()) {
                IRadiationEntity cap = capability.get();
                list.add(MekanismLang.RADIATION_DOSE.translateColored(EnumColor.GRAY, RadiationScale.getSeverityColor(cap.getRadiation()),
                      UnitDisplayUtils.getDisplayShort(cap.getRadiation(), RadiationUnit.SV, 3)));
            }
        }
    }

    public static class ModuleMagneticAttractionUnit extends ModuleMekaSuit {

        private ModuleConfigItem<Range> range;

        @Override
        public void init() {
            super.init();
            addConfigItem(range = new ModuleConfigItem<>(this, "range", MekanismLang.MODULE_RANGE, new EnumData<>(Range.class, getInstalledCount() + 1), Range.LOW));
        }

        @Override
        public void tickClient(PlayerEntity player) {
            super.tickClient(player);
            suckItems(player, true);
        }

        @Override
        public void tickServer(PlayerEntity player) {
            super.tickServer(player);
            suckItems(player, false);
        }

        private void suckItems(PlayerEntity player, boolean client) {
            if (range.get() == Range.OFF) {
                return;
            }
            float size = 4 + range.get().getRange();
            List<ItemEntity> items = player.world.getEntitiesWithinAABB(ItemEntity.class, player.getBoundingBox().grow(size, size, size));
            FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsageItemAttraction.get().multiply(range.get().getRange());
            for (ItemEntity item : items) {
                if (!getContainerEnergy().greaterOrEqual(usage)) {
                    break;
                }
                if (item.getDistance(player) > 0.1) {
                    useEnergy(player, usage);
                    Vec3d diff = player.getPositionVec().subtract(item.getPositionVec());
                    Vec3d motionNeeded = new Vec3d(Math.min(diff.x, 1), Math.min(diff.y, 1), Math.min(diff.z, 1));
                    Vec3d motionDiff = motionNeeded.subtract(player.getMotion());
                    item.setMotion(motionDiff.scale(0.2));
                    if (client) {
                        BoltEffect bolt = new BoltEffect(BoltRenderInfo.ELECTRICITY, player.getPositionVec().add(0, 0.2, 0), item.getPositionVec(), (int) (diff.length() * 4))
                              .size(0.04F).lifespan(8).spawn(SpawnFunction.noise(8, 4));
                        Mekanism.proxy.renderBolt(Objects.hash(player, item), bolt);
                    }
                }
            }
        }

        public enum Range implements IHasTextComponent {
            OFF(0),
            LOW(1F),
            MED(3F),
            HIGH(5),
            ULTRA(10);
            private float range;
            private ITextComponent label;

            Range(float boost) {
                this.range = boost;
                this.label = new StringTextComponent(Float.toString(boost));
            }

            @Override
            public ITextComponent getTextComponent() {
                return label;
            }

            public float getRange() {
                return range;
            }
        }
    }
}
