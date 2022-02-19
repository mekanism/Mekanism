package mekanism.common.util;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.Long2DoubleArrayMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.integration.energy.EnergyCompatUtils.EnergyType;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.util.UnitDisplayUtils.ElectricUnit;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.OwnerDisplay;
import mekanism.common.util.text.UpgradeDisplay;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TripWireBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SPlayEntityEffectPacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.HandSide;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.IItemHandler;

/**
 * Utilities used by Mekanism. All miscellaneous methods are located here.
 *
 * @author AidanBrady
 */
public final class MekanismUtils {

    public static final Codec<Direction> DIRECTION_CODEC = IStringSerializable.fromEnum(Direction::values, Direction::byName);

    public static final float ONE_OVER_ROOT_TWO = (float) (1 / Math.sqrt(2));

    public static final Direction[] SIDE_DIRS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    private static final List<UUID> warnedFails = new ArrayList<>();

    //TODO: Evaluate adding an extra optional param to shrink and grow stack that allows for logging if it is mismatched. Defaults to false
    // Deciding on how to implement it into the API will need more thought as we want to keep overriding implementations as simple as
    // possible, and also ideally would use our normal logger instead of the API logger
    public static void logMismatchedStackSize(long actual, long expected) {
        if (expected != actual) {
            Mekanism.logger.error("Stack size changed by a different amount ({}) than requested ({}).", actual, expected, new Exception());
        }
    }

    public static void logExpectedZero(FloatingLong actual) {
        if (!actual.isZero()) {
            Mekanism.logger.error("Energy value changed by a different amount ({}) than requested (zero).", actual, new Exception());
        }
    }

    public static ITextComponent logFormat(Object message) {
        return logFormat(EnumColor.GRAY, message);
    }

    public static ITextComponent logFormat(EnumColor messageColor, Object message) {
        return MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, messageColor, message);
    }

    /**
     * Gets the creator's modid if it exists, or falls back to the registry name.
     *
     * @implNote While the default implementation of getCreatorModId falls back to the registry name, it is possible someone is overriding this and not falling back.
     */
    @Nonnull
    public static String getModId(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        String modid = item.getCreatorModId(stack);
        if (modid == null) {
            ResourceLocation registryName = item.getRegistryName();
            if (registryName == null) {
                Mekanism.logger.error("Unexpected null registry name for item of class type: {}", item.getClass().getSimpleName());
                return "";
            }
            return registryName.getNamespace();
        }
        return modid;
    }

    public static ItemStack getItemInHand(LivingEntity entity, HandSide side) {
        if (entity instanceof PlayerEntity) {
            return getItemInHand((PlayerEntity) entity, side);
        } else if (side == HandSide.RIGHT) {
            return entity.getMainHandItem();
        }
        return entity.getOffhandItem();
    }

    public static ItemStack getItemInHand(PlayerEntity player, HandSide side) {
        if (player.getMainArm() == side) {
            return player.getMainHandItem();
        }
        return player.getOffhandItem();
    }

    /**
     * Gets the left side of a certain orientation.
     *
     * @param orientation Current orientation of the machine
     *
     * @return left side
     */
    public static Direction getLeft(Direction orientation) {
        return orientation.getClockWise();
    }

    /**
     * Gets the right side of a certain orientation.
     *
     * @param orientation Current orientation of the machine
     *
     * @return right side
     */
    public static Direction getRight(Direction orientation) {
        return orientation.getCounterClockWise();
    }

    public static double fractionUpgrades(IUpgradeTile tile, Upgrade type) {
        if (tile.supportsUpgrade(type)) {
            return tile.getComponent().getUpgrades(type) / (double) type.getMax();
        }
        return 0;
    }

    public static float getScale(float prevScale, IExtendedFluidTank tank) {
        return getScale(prevScale, tank.getFluidAmount(), tank.getCapacity(), tank.isEmpty());
    }

    public static float getScale(float prevScale, IChemicalTank<?, ?> tank) {
        return getScale(prevScale, tank.getStored(), tank.getCapacity(), tank.isEmpty());
    }

    public static float getScale(float prevScale, int stored, int capacity, boolean empty) {
        return getScale(prevScale, capacity == 0 ? 0 : stored / (float) capacity, empty, stored == capacity);
    }

    public static float getScale(float prevScale, long stored, long capacity, boolean empty) {
        return getScale(prevScale, capacity == 0 ? 0 : (float) (stored / (double) capacity), empty, stored == capacity);
    }


    public static float getScale(float prevScale, IEnergyContainer container) {
        float targetScale;
        FloatingLong stored = container.getEnergy();
        FloatingLong capacity = container.getMaxEnergy();
        if (capacity.isZero()) {
            targetScale = 0;
        } else {
            targetScale = stored.divide(capacity).floatValue();
        }
        return getScale(prevScale, targetScale, container.isEmpty(), stored.equals(capacity));
    }

    public static float getScale(float prevScale, float targetScale, boolean empty, boolean full) {
        float difference = Math.abs(prevScale - targetScale);
        if (difference > 0.01) {
            return (9 * prevScale + targetScale) / 10;
        } else if (!empty && full && difference > 0) {
            //If we are full but our difference is less than 0.01, but we want to get our scale all the way up to the target
            // instead of leaving it at a value just under. Note: We also check that we are not empty as we technically may
            // be both empty and full if the current capacity is zero
            return targetScale;
        } else if (!empty && prevScale == 0) {
            //If we have any contents make sure we end up rendering it
            return targetScale;
        }
        if (empty && prevScale < 0.01) {
            //If we are empty and have a very small amount just round it down to empty
            return 0;
        }
        return prevScale;
    }

    public static long getBaseUsage(IUpgradeTile tile, int def) {
        if (tile.supportsUpgrades()) {
            //getGasPerTickMean * required ticks (not rounded)
            if (tile.supportsUpgrade(Upgrade.GAS)) {
                // def * (upgradeMultiplier ^ ((2 * speed - gas) / 8)) * (upgradeMultiplier ^ (-speed / 8)) =
                // def * upgradeMultiplier ^ ((speed - gas) / 8)
                //TODO: We may want to validate this provides the numbers we desire if we ever end up with any machines
                // that use this that are not statistical and have gas upgrades so would go through this code path
                return Math.round(def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(),
                      fractionUpgrades(tile, Upgrade.SPEED) - fractionUpgrades(tile, Upgrade.GAS)));
            }
            //If it doesn't support gas upgrades, we can fall through to the default value as the math would be:
            // def * (upgradeMultiplier ^ (speed / 8)) * (upgradeMultiplier ^ (-speed / 8)) =
            // def * 1
        }
        return def;
    }

    /**
     * Gets the operating ticks required for a machine via its upgrades.
     *
     * @param tile - tile containing upgrades
     * @param def  - the original, default ticks required
     *
     * @return required operating ticks
     */
    public static int getTicks(IUpgradeTile tile, int def) {
        if (tile.supportsUpgrades()) {
            return MathUtils.clampToInt(def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), -fractionUpgrades(tile, Upgrade.SPEED)));
        }
        return def;
    }

    /**
     * Gets the energy required per tick for a machine via its upgrades.
     *
     * @param tile - tile containing upgrades
     * @param def  - the original, default energy required
     *
     * @return required energy per tick
     */
    public static FloatingLong getEnergyPerTick(IUpgradeTile tile, FloatingLong def) {
        if (tile.supportsUpgrades()) {
            return def.multiply(Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), 2 * fractionUpgrades(tile, Upgrade.SPEED) - fractionUpgrades(tile, Upgrade.ENERGY)));
        }
        return def;
    }

    /**
     * Gets the secondary energy multiplier required per tick for a machine via upgrades.
     *
     * @param tile - tile containing upgrades
     *
     * @return max secondary energy per tick
     */
    public static double getGasPerTickMeanMultiplier(IUpgradeTile tile) {
        if (tile.supportsUpgrades()) {
            if (tile.supportsUpgrade(Upgrade.GAS)) {
                return Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), 2 * fractionUpgrades(tile, Upgrade.SPEED) - fractionUpgrades(tile, Upgrade.GAS));
            }
            return Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), fractionUpgrades(tile, Upgrade.SPEED));
        }
        return 1;
    }

    /**
     * Gets the maximum energy for a machine via its upgrades.
     *
     * @param tile - tile containing upgrades
     * @param def  - original, default max energy
     *
     * @return max energy
     */
    public static FloatingLong getMaxEnergy(IUpgradeTile tile, FloatingLong def) {
        if (tile.supportsUpgrades()) {
            return def.multiply(Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), fractionUpgrades(tile, Upgrade.ENERGY)));
        }
        return def;
    }

    /**
     * Gets the maximum energy for a machine's item form via its upgrades.
     *
     * @param stack - stack holding energy upgrades
     * @param def   - original, default max energy
     *
     * @return max energy
     */
    public static FloatingLong getMaxEnergy(ItemStack stack, FloatingLong def) {
        float numUpgrades = 0;
        if (ItemDataUtils.hasData(stack, NBTConstants.COMPONENT_UPGRADE, NBT.TAG_COMPOUND)) {
            Map<Upgrade, Integer> upgrades = Upgrade.buildMap(ItemDataUtils.getCompound(stack, NBTConstants.COMPONENT_UPGRADE));
            if (upgrades.containsKey(Upgrade.ENERGY)) {
                numUpgrades = upgrades.get(Upgrade.ENERGY);
            }
        }
        return def.multiply(Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), numUpgrades / Upgrade.ENERGY.getMax()));
    }

    /**
     * Gets a ResourceLocation with a defined resource type and name.
     *
     * @param type - type of resource to retrieve
     * @param name - simple name of file to retrieve as a ResourceLocation
     *
     * @return the corresponding ResourceLocation
     */
    public static ResourceLocation getResource(ResourceType type, String name) {
        return Mekanism.rl(type.getPrefix() + name);
    }

    /**
     * Whether a certain Mekanism TileEntity can function with redstone logic. Illogical to use unless the defined TileEntity supports redstone.
     *
     * @param tile - TileEntity to check
     *
     * @return if the TileEntity can function with redstone logic
     */
    public static boolean canFunction(TileEntityMekanism tile) {
        if (!tile.supportsRedstone()) {
            return true;
        }
        switch (tile.getControlType()) {
            case DISABLED:
                return true;
            case HIGH:
                return tile.isPowered();
            case LOW:
                return !tile.isPowered();
            case PULSE:
                return tile.isPowered() && !tile.wasPowered();
        }
        return false;
    }

    /**
     * Ray-traces what block a player is looking at.
     *
     * @param player - player to raytrace
     *
     * @return raytraced value
     */
    public static BlockRayTraceResult rayTrace(PlayerEntity player) {
        return rayTrace(player, FluidMode.NONE);
    }

    public static BlockRayTraceResult rayTrace(PlayerEntity player, FluidMode fluidMode) {
        return rayTrace(player, player.getAttributeValue(ForgeMod.REACH_DISTANCE.get()), fluidMode);
    }

    public static BlockRayTraceResult rayTrace(PlayerEntity player, double reach) {
        return rayTrace(player, reach, FluidMode.NONE);
    }

    public static BlockRayTraceResult rayTrace(PlayerEntity player, double reach, FluidMode fluidMode) {
        Vector3d headVec = getHeadVec(player);
        Vector3d lookVec = player.getViewVector(1);
        Vector3d endVec = headVec.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);
        return player.getCommandSenderWorld().clip(new RayTraceContext(headVec, endVec, BlockMode.OUTLINE, fluidMode, player));
    }

    /**
     * Gets the head vector of a player for a ray trace.
     *
     * @param player - player to check
     *
     * @return head location
     */
    private static Vector3d getHeadVec(PlayerEntity player) {
        double posY = player.getY() + player.getEyeHeight();
        if (player.isCrouching()) {
            posY -= 0.08;
        }
        return new Vector3d(player.getX(), posY, player.getZ());
    }

    /**
     * @apiNote Only call on the client.
     */
    public static void addFrequencyToTileTooltip(ItemStack stack, FrequencyType<?> frequencyType, List<ITextComponent> tooltip) {
        if (ItemDataUtils.hasData(stack, NBTConstants.COMPONENT_FREQUENCY, NBT.TAG_COMPOUND)) {
            CompoundNBT frequencyComponent = ItemDataUtils.getCompound(stack, NBTConstants.COMPONENT_FREQUENCY);
            if (frequencyComponent.contains(frequencyType.getName(), NBT.TAG_COMPOUND)) {
                Frequency frequency = frequencyType.create(frequencyComponent.getCompound(frequencyType.getName()));
                frequency.setValid(false);
                tooltip.add(MekanismLang.FREQUENCY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, frequency.getName()));
                if (frequency.getOwner() != null) {
                    String owner = OwnerDisplay.getOwnerName(MekanismClient.tryGetClientPlayer(), frequency.getOwner(), frequency.getClientOwner());
                    if (owner != null) {
                        tooltip.add(MekanismLang.OWNER.translateColored(EnumColor.INDIGO, EnumColor.GRAY, owner));
                    }
                }
                tooltip.add(MekanismLang.MODE.translateColored(EnumColor.INDIGO, EnumColor.GRAY, frequency.isPublic() ? MekanismLang.PUBLIC : MekanismLang.PRIVATE));
            }
        }
    }

    /**
     * @apiNote Only call on the client.
     */
    public static void addFrequencyItemTooltip(ItemStack stack, List<ITextComponent> tooltip) {
        FrequencyIdentity frequency = ((IFrequencyItem) stack.getItem()).getFrequencyIdentity(stack);
        if (frequency != null) {
            tooltip.add(MekanismLang.FREQUENCY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, frequency.getKey()));
            CompoundNBT frequencyCompound = ItemDataUtils.getCompound(stack, NBTConstants.FREQUENCY);
            if (frequencyCompound.hasUUID(NBTConstants.OWNER_UUID)) {
                String owner = OwnerDisplay.getOwnerName(MekanismClient.tryGetClientPlayer(), frequencyCompound.getUUID(NBTConstants.OWNER_UUID), null);
                if (owner != null) {
                    tooltip.add(MekanismLang.OWNER.translateColored(EnumColor.INDIGO, EnumColor.GRAY, owner));
                }
            }
            tooltip.add(MekanismLang.MODE.translateColored(EnumColor.INDIGO, EnumColor.GRAY, frequency.isPublic() ? MekanismLang.PUBLIC : MekanismLang.PRIVATE));
        }
    }

    public static void addUpgradesToTooltip(ItemStack stack, List<ITextComponent> tooltip) {
        if (ItemDataUtils.hasData(stack, NBTConstants.COMPONENT_UPGRADE, NBT.TAG_COMPOUND)) {
            Map<Upgrade, Integer> upgrades = Upgrade.buildMap(ItemDataUtils.getCompound(stack, NBTConstants.COMPONENT_UPGRADE));
            for (Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
                tooltip.add(UpgradeDisplay.of(entry.getKey(), entry.getValue()).getTextComponent());
            }
        }
    }

    public static ITextComponent getEnergyDisplayShort(FloatingLong energy) {
        switch (MekanismConfig.general.energyUnit.get()) {
            case J:
                return UnitDisplayUtils.getDisplayShort(energy, ElectricUnit.JOULES);
            case FE:
                return UnitDisplayUtils.getDisplayShort(EnergyType.FORGE.convertToAsFloatingLong(energy), ElectricUnit.FORGE_ENERGY);
            case EU:
                return UnitDisplayUtils.getDisplayShort(EnergyType.EU.convertToAsFloatingLong(energy), ElectricUnit.ELECTRICAL_UNITS);
        }
        return MekanismLang.ERROR.translate();
    }

    /**
     * Convert from the unit defined in the configuration to joules.
     *
     * @param energy - energy to convert
     *
     * @return energy converted to joules
     */
    public static FloatingLong convertToJoules(FloatingLong energy) {
        switch (MekanismConfig.general.energyUnit.get()) {
            case FE:
                return EnergyType.FORGE.convertFrom(energy);
            case EU:
                return EnergyType.EU.convertFrom(energy);
            default:
                return energy;
        }
    }

    /**
     * Convert from joules to the unit defined in the configuration.
     *
     * @param energy - energy to convert
     *
     * @return energy converted to configured unit
     */
    public static FloatingLong convertToDisplay(FloatingLong energy) {
        switch (MekanismConfig.general.energyUnit.get()) {
            case FE:
                return EnergyType.FORGE.convertToAsFloatingLong(energy);
            case EU:
                return EnergyType.EU.convertToAsFloatingLong(energy);
            default:
                return energy;
        }
    }

    /**
     * Gets a rounded energy display of a defined amount of energy.
     *
     * @param temp - temperature to display
     *
     * @return rounded energy display
     */
    public static ITextComponent getTemperatureDisplay(double temp, TemperatureUnit unit, boolean shift) {
        double tempKelvin = unit.convertToK(temp, true);
        switch (MekanismConfig.general.tempUnit.get()) {
            case K:
                return UnitDisplayUtils.getDisplayShort(tempKelvin, TemperatureUnit.KELVIN, shift);
            case C:
                return UnitDisplayUtils.getDisplayShort(tempKelvin, TemperatureUnit.CELSIUS, shift);
            case R:
                return UnitDisplayUtils.getDisplayShort(tempKelvin, TemperatureUnit.RANKINE, shift);
            case F:
                return UnitDisplayUtils.getDisplayShort(tempKelvin, TemperatureUnit.FAHRENHEIT, shift);
            case STP:
                return UnitDisplayUtils.getDisplayShort(tempKelvin, TemperatureUnit.AMBIENT, shift);
        }
        return MekanismLang.ERROR.translate();
    }

    public static CraftingInventory getDummyCraftingInv() {
        Container tempContainer = new Container(ContainerType.CRAFTING, 1) {
            @Override
            public boolean stillValid(@Nonnull PlayerEntity player) {
                return false;
            }
        };
        return new CraftingInventory(tempContainer, 3, 3);
    }

    /**
     * Gets the wrench if the item is an IMekWrench, or a generic implementation if the item is in the forge wrenches tag
     */
    public static boolean canUseAsWrench(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (item instanceof ItemConfigurator) {
            return ((ItemConfigurator) item).getMode(stack) == ConfiguratorMode.WRENCH;
        }
        return item.is(MekanismTags.Items.CONFIGURATORS);
    }

    @Nonnull
    public static String getLastKnownUsername(@Nullable UUID uuid) {
        if (uuid == null) {
            return "<???>";
        }
        String ret = UsernameCache.getLastKnownUsername(uuid);
        if (ret == null && !warnedFails.contains(uuid) && EffectiveSide.get().isServer()) { // see if MC/Yggdrasil knows about it?!
            GameProfile gp = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(uuid);
            if (gp != null) {
                ret = gp.getName();
            }
        }
        if (ret == null && !warnedFails.contains(uuid)) {
            Mekanism.logger.warn("Failed to retrieve username for UUID {}, you might want to add it to the JSON cache", uuid);
            warnedFails.add(uuid);
        }
        return ret != null ? ret : "<" + uuid + ">";
    }

    /**
     * Copy of {@link EffectInstance#tick(LivingEntity, Runnable)}, but modified to not apply the effect to avoid extra damage and the like.
     */
    public static void speedUpEffectSafely(LivingEntity entity, EffectInstance effectInstance) {
        if (effectInstance.getDuration() > 0) {
            int remainingDuration = effectInstance.tickDownDuration();
            if (remainingDuration == 0 && effectInstance.hiddenEffect != null) {
                effectInstance.setDetailsFrom(effectInstance.hiddenEffect);
                effectInstance.hiddenEffect = effectInstance.hiddenEffect.hiddenEffect;
                onChangedPotionEffect(entity, effectInstance, true);
            }
        }
    }

    /**
     * Copy of LivingEntity#onChangedPotionEffect(EffectInstance, boolean) due to not being able to AT the method as it is protected.
     */
    private static void onChangedPotionEffect(LivingEntity entity, EffectInstance effectInstance, boolean reapply) {
        entity.effectsDirty = true;
        if (reapply && !entity.level.isClientSide) {
            Effect effect = effectInstance.getEffect();
            effect.removeAttributeModifiers(entity, entity.getAttributes(), effectInstance.getAmplifier());
            effect.addAttributeModifiers(entity, entity.getAttributes(), effectInstance.getAmplifier());
        }
        if (entity instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) entity).connection.send(new SPlayEntityEffectPacket(entity.getId(), effectInstance));
            CriteriaTriggers.EFFECTS_CHANGED.trigger(((ServerPlayerEntity) entity));
        }
    }

    public static boolean isSameTypeFactory(Block block, TileEntityType<?> factoryTileType) {
        AttributeFactoryType attribute = Attribute.get(block, AttributeFactoryType.class);
        if (attribute == null) {
            return false;
        }
        FactoryType factoryType = attribute.getFactoryType();
        //Check all factory types
        for (FactoryTier factoryTier : EnumUtils.FACTORY_TIERS) {
            if (MekanismTileEntityTypes.getFactoryTile(factoryTier, factoryType).get() == factoryTileType) {
                return true;
            }
        }
        return false;
    }

    /**
     * Performs a set of actions, until we find a success or run out of actions.
     *
     * @implNote Only returns that we failed if all the tested actions failed.
     */
    @SafeVarargs
    public static ActionResultType performActions(ActionResultType firstAction, Supplier<ActionResultType>... secondaryActions) {
        if (firstAction == ActionResultType.SUCCESS) {
            return ActionResultType.SUCCESS;
        }
        ActionResultType result = firstAction;
        boolean hasFailed = result == ActionResultType.FAIL;
        for (Supplier<ActionResultType> secondaryAction : secondaryActions) {
            result = secondaryAction.get();
            if (result == ActionResultType.SUCCESS) {
                //If we were successful
                return ActionResultType.SUCCESS;
            }
            hasFailed &= result == ActionResultType.FAIL;
        }
        if (hasFailed) {
            //If at least one step failed, consider ourselves unsuccessful
            return ActionResultType.FAIL;
        }
        return ActionResultType.PASS;
    }

    /**
     * @param amount   Amount currently stored
     * @param capacity Total amount that can be stored.
     *
     * @return A redstone level based on the percentage of the amount stored.
     */
    public static int redstoneLevelFromContents(long amount, long capacity) {
        double fractionFull = capacity == 0 ? 0 : amount / (double) capacity;
        return MathHelper.floor((float) (fractionFull * 14.0F)) + (fractionFull > 0 ? 1 : 0);
    }

    /**
     * Calculates the redstone level based on the percentage of amount stored.
     *
     * @param amount   Amount currently stored
     * @param capacity Total amount that can be stored.
     *
     * @return A redstone level based on the percentage of the amount stored.
     */
    public static int redstoneLevelFromContents(FloatingLong amount, FloatingLong capacity) {
        if (capacity.isZero() || amount.isZero()) {
            return 0;
        }
        return 1 + amount.divide(capacity).multiply(14).intValue();
    }

    /**
     * Calculates the redstone level based on the percentage of amount stored. Like {@link net.minecraftforge.items.ItemHandlerHelper#calcRedstoneFromInventory(IItemHandler)}
     * except without limiting slots to the max stack size of the item to allow for better support for bins
     *
     * @return A redstone level based on the percentage of the amount stored.
     */
    public static int redstoneLevelFromContents(List<IInventorySlot> slots) {
        long totalCount = 0;
        long totalLimit = 0;
        for (IInventorySlot slot : slots) {
            if (slot.isEmpty()) {
                totalLimit += slot.getLimit(ItemStack.EMPTY);
            } else {
                totalCount += slot.getCount();
                totalLimit += slot.getLimit(slot.getStack());
            }
        }
        return redstoneLevelFromContents(totalCount, totalLimit);
    }

    /**
     * Checks whether the player is in creative or spectator mode.
     *
     * @param player the player to check.
     *
     * @return true if the player is neither in creative mode, nor in spectator mode.
     */
    public static boolean isPlayingMode(PlayerEntity player) {
        return !player.isCreative() && !player.isSpectator();
    }

    /**
     * Similar in concept to {@link net.minecraft.entity.Entity#updateFluidHeightAndDoFluidPushing(ITag, double)} except calculates if a given portion of the player is in
     * the fluids.
     */
    public static Map<Fluid, FluidInDetails> getFluidsIn(PlayerEntity player, UnaryOperator<AxisAlignedBB> modifyBoundingBox) {
        AxisAlignedBB bb = modifyBoundingBox.apply(player.getBoundingBox().deflate(0.001));
        int xMin = MathHelper.floor(bb.minX);
        int xMax = MathHelper.ceil(bb.maxX);
        int yMin = MathHelper.floor(bb.minY);
        int yMax = MathHelper.ceil(bb.maxY);
        int zMin = MathHelper.floor(bb.minZ);
        int zMax = MathHelper.ceil(bb.maxZ);
        if (!player.level.hasChunksAt(xMin, yMin, zMin, xMax, yMax, zMax)) {
            //If the position isn't actually loaded, just return there isn't any fluids
            return Collections.emptyMap();
        }
        Map<Fluid, FluidInDetails> fluidsIn = new HashMap<>();
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        for (int x = xMin; x < xMax; ++x) {
            for (int y = yMin; y < yMax; ++y) {
                for (int z = zMin; z < zMax; ++z) {
                    mutablePos.set(x, y, z);
                    FluidState fluidState = player.level.getFluidState(mutablePos);
                    if (!fluidState.isEmpty()) {
                        double fluidY = y + fluidState.getHeight(player.level, mutablePos);
                        if (bb.minY <= fluidY) {
                            //The fluid intersects the bounding box
                            Fluid fluid = fluidState.getType();
                            if (fluid instanceof FlowingFluid) {
                                //Almost always will be flowing fluid but check just in case
                                // and if it is grab the source state to not have duplicates
                                fluid = ((FlowingFluid) fluid).getSource();
                            }
                            FluidInDetails details = fluidsIn.computeIfAbsent(fluid, f -> new FluidInDetails());
                            details.positions.add(mutablePos.immutable());
                            double actualFluidHeight;
                            if (fluidY > bb.maxY) {
                                //Fluid goes past the top of the bounding box, limit it to the top
                                // We do the max of the bottom of the bounding box and our current block so that
                                // if we are floating above the bottom we don't take the area below us into account
                                actualFluidHeight = bb.maxY - Math.max(bb.minY, y);
                            } else {
                                // We do the max of the bottom of the bounding box and our current block so that
                                // if we are floating above the bottom we don't take the area below us into account
                                actualFluidHeight = fluidY - Math.max(bb.minY, y);
                            }
                            details.heights.merge(ChunkPos.asLong(x, z), actualFluidHeight, Double::sum);
                        }
                    }
                }
            }
        }
        return fluidsIn;
    }

    public static void veinMineArea(IEnergyContainer energyContainer, World world, BlockPos pos, ServerPlayerEntity player, ItemStack stack, Item usedTool,
          Collection<BlockPos> found, boolean shears, Function<Float, FloatingLong> destroyEnergyFunction, DoubleUnaryOperator distanceMultiplier,
          BlockState sourceState) {
        FloatingLong energyUsed = FloatingLong.ZERO;
        FloatingLong energyAvailable = energyContainer.getEnergy();
        //Subtract from our available energy the amount that we will require to break the target block
        energyAvailable = energyAvailable.subtract(destroyEnergyFunction.apply(sourceState.getDestroySpeed(world, pos)));
        for (BlockPos foundPos : found) {
            if (pos.equals(foundPos)) {
                continue;
            }
            BlockState targetState = world.getBlockState(foundPos);
            FloatingLong destroyEnergy = destroyEnergyFunction.apply(targetState.getDestroySpeed(world, foundPos))
                  .multiply(distanceMultiplier.applyAsDouble(WorldUtils.distanceBetween(pos, foundPos)));
            if (energyUsed.add(destroyEnergy).greaterThan(energyAvailable)) {
                //If we don't have energy to break the block continue
                //Note: We do not break as given the energy scales with hardness, so it is possible we still have energy to break another block
                // Given we validate the blocks are the same but their block states may be different thus making them have different
                // block hardness values in a modded context
                continue;
            }
            int exp = ForgeHooks.onBlockBreakEvent(world, player.gameMode.getGameModeForPlayer(), player, foundPos);
            if (exp == -1) {
                //If we can't actually break the block continue (this allows mods to stop us from vein mining into protected land)
                continue;
            }
            //If we have the shears module installed, and it is a tripwire, disarm it first
            if (shears && targetState.is(Blocks.TRIPWIRE) && !targetState.getValue(TripWireBlock.DISARMED)) {
                targetState = targetState.setValue(TripWireBlock.DISARMED, true);
                world.setBlock(foundPos, targetState, BlockFlags.NO_RERENDER);
            }
            //Otherwise, break the block
            Block block = targetState.getBlock();
            //Get the tile now so that we have it for when we try to harvest the block
            TileEntity tileEntity = WorldUtils.getTileEntity(world, foundPos);
            //Remove the block
            if (targetState.removedByPlayer(world, foundPos, player, true, targetState.getFluidState())) {
                block.destroy(world, foundPos, targetState);
                //Harvest the block allowing it to handle block drops, incrementing block mined count, and adding exhaustion
                block.playerDestroy(world, player, foundPos, targetState, tileEntity, stack);
                player.awardStat(Stats.ITEM_USED.get(usedTool));
                if (exp > 0) {
                    //If we have xp drop it
                    block.popExperience((ServerWorld) world, foundPos, exp);
                }
                //Mark that we used that portion of the energy
                energyUsed = energyUsed.plusEqual(destroyEnergy);
            }
        }
        energyContainer.extract(energyUsed, Action.EXECUTE, AutomationType.MANUAL);
    }

    public enum ResourceType {
        GUI("gui"),
        GUI_BUTTON("gui/button"),
        GUI_BAR("gui/bar"),
        GUI_HUD("gui/hud"),
        GUI_GAUGE("gui/gauge"),
        GUI_PROGRESS("gui/progress"),
        GUI_SLOT("gui/slot"),
        GUI_TAB("gui/tabs"),
        SOUND("sound"),
        RENDER("render"),
        TEXTURE_BLOCKS("textures/block"),
        TEXTURE_ITEMS("textures/item"),
        MODEL("models"),
        INFUSE("infuse"),
        PIGMENT("pigment"),
        SLURRY("slurry");

        private final String prefix;

        ResourceType(String s) {
            prefix = s;
        }

        public String getPrefix() {
            return prefix + "/";
        }
    }

    public static class FluidInDetails {

        private final List<BlockPos> positions = new ArrayList<>();
        private final Long2DoubleMap heights = new Long2DoubleArrayMap();

        public List<BlockPos> getPositions() {
            return positions;
        }

        public double getMaxHeight() {
            return heights.values().stream().mapToDouble(value -> value).max().orElse(0);
        }
    }
}