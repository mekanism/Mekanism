package mekanism.common.util;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IMekWrench;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.GenericWrench;
import mekanism.common.integration.energy.EnergyCompatUtils.EnergyType;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.interfaces.IRedstoneControl;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.util.UnitDisplayUtils.ElectricUnit;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.UpgradeDisplay;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPlayEntityEffectPacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.UsernameCache;
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

    public static final Codec<Direction> DIRECTION_CODEC = IStringSerializable.createEnumCodec(Direction::values, Direction::byName);

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

    /**
     * Gets the left side of a certain orientation.
     *
     * @param orientation Current orientation of the machine
     *
     * @return left side
     */
    public static Direction getLeft(Direction orientation) {
        return orientation.rotateY();
    }

    /**
     * Gets the right side of a certain orientation.
     *
     * @param orientation Current orientation of the machine
     *
     * @return right side
     */
    public static Direction getRight(Direction orientation) {
        return orientation.rotateYCCW();
    }

    public static float fractionUpgrades(IUpgradeTile tile, Upgrade type) {
        if (tile.supportsUpgrades()) {
            return (float) tile.getComponent().getUpgrades(type) / (float) type.getMax();
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
            //If we are full but our difference is less than 0.01 but we want to get our scale all the way up to the target
            // instead of leaving it at a value just under. Note: We also check that are are not empty as we technically may
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

    /**
     * Gets the operating ticks required for a machine via it's upgrades.
     *
     * @param tile - tile containing upgrades
     * @param def  - the original, default ticks required
     *
     * @return required operating ticks
     */
    public static int getTicks(IUpgradeTile tile, int def) {
        if (tile.supportsUpgrades()) {
            return (int) (def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), -fractionUpgrades(tile, Upgrade.SPEED)));
        }
        return def;
    }

    /**
     * Gets the energy required per tick for a machine via it's upgrades.
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
            if (tile.getComponent().supports(Upgrade.GAS)) {
                return Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), 2 * fractionUpgrades(tile, Upgrade.SPEED) - fractionUpgrades(tile, Upgrade.GAS));
            }
            return Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), fractionUpgrades(tile, Upgrade.SPEED));
        }
        return 1;
    }

    /**
     * Gets the maximum energy for a machine via it's upgrades.
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
     * Gets the maximum energy for a machine's item form via it's upgrades.
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
     * Whether or not a certain TileEntity can function with redstone logic. Illogical to use unless the defined TileEntity implements IRedstoneControl.
     *
     * @param tile - TileEntity to check
     *
     * @return if the TileEntity can function with redstone logic
     */
    public static boolean canFunction(TileEntity tile) {
        if (!(tile instanceof IRedstoneControl)) {
            return true;
        }
        IRedstoneControl control = (IRedstoneControl) tile;
        switch (control.getControlType()) {
            case DISABLED:
                return true;
            case HIGH:
                return control.isPowered();
            case LOW:
                return !control.isPowered();
            case PULSE:
                return control.isPowered() && !control.wasPowered();
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
        return rayTrace(player, player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue(), fluidMode);
    }

    public static BlockRayTraceResult rayTrace(PlayerEntity player, double reach) {
        return rayTrace(player, reach, FluidMode.NONE);
    }

    public static BlockRayTraceResult rayTrace(PlayerEntity player, double reach, FluidMode fluidMode) {
        Vector3d headVec = getHeadVec(player);
        Vector3d lookVec = player.getLook(1);
        Vector3d endVec = headVec.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);
        return player.getEntityWorld().rayTraceBlocks(new RayTraceContext(headVec, endVec, BlockMode.OUTLINE, fluidMode, player));
    }

    /**
     * Gets the head vector of a player for a ray trace.
     *
     * @param player - player to check
     *
     * @return head location
     */
    private static Vector3d getHeadVec(PlayerEntity player) {
        double posY = player.getPosY() + player.getEyeHeight();
        if (player.isCrouching()) {
            posY -= 0.08;
        }
        return new Vector3d(player.getPosX(), posY, player.getPosZ());
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
            public boolean canInteractWith(@Nonnull PlayerEntity player) {
                return false;
            }
        };
        return new CraftingInventory(tempContainer, 3, 3);
    }

    /**
     * Finds the output of a brute forced repairing action
     *
     * @param inv   - InventoryCrafting to check
     * @param world - world reference
     *
     * @return output ItemStack
     */
    public static ItemStack findRepairRecipe(CraftingInventory inv, World world) {
        NonNullList<ItemStack> dmgItems = NonNullList.withSize(2, ItemStack.EMPTY);
        ItemStack leftStack = dmgItems.get(0);
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (!inv.getStackInSlot(i).isEmpty()) {
                if (leftStack.isEmpty()) {
                    dmgItems.set(0, leftStack = inv.getStackInSlot(i));
                } else {
                    dmgItems.set(1, inv.getStackInSlot(i));
                    break;
                }
            }
        }

        if (leftStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack rightStack = dmgItems.get(1);
        if (!rightStack.isEmpty() && leftStack.getItem() == rightStack.getItem() && leftStack.getCount() == 1 && rightStack.getCount() == 1 &&
            leftStack.getItem().isRepairable(leftStack)) {
            Item theItem = leftStack.getItem();
            int dmgDiff0 = theItem.getMaxDamage(leftStack) - leftStack.getDamage();
            int dmgDiff1 = theItem.getMaxDamage(leftStack) - rightStack.getDamage();
            int value = dmgDiff0 + dmgDiff1 + theItem.getMaxDamage(leftStack) * 5 / 100;
            int solve = Math.max(0, theItem.getMaxDamage(leftStack) - value);
            ItemStack repaired = new ItemStack(leftStack.getItem());
            repaired.setDamage(solve);
            return repaired;
        }
        return ItemStack.EMPTY;
    }

    /**
     * Gets the wrench if the item is an IMekWrench, or a generic implementation if the item is in the forge wrenches tag
     */
    @Nullable
    public static IMekWrench getWrench(ItemStack it) {
        Item item = it.getItem();
        if (item instanceof IMekWrench) {
            return (IMekWrench) item;
        } else if (item.isIn(MekanismTags.Items.CONFIGURATORS)) {
            return GenericWrench.INSTANCE;
        }
        return null;
    }

    @Nonnull
    public static String getLastKnownUsername(@Nullable UUID uuid) {
        if (uuid == null) {
            return "<???>";
        }
        String ret = UsernameCache.getLastKnownUsername(uuid);
        if (ret == null && !warnedFails.contains(uuid) && EffectiveSide.get().isServer()) { // see if MC/Yggdrasil knows about it?!
            GameProfile gp = ServerLifecycleHooks.getCurrentServer().getPlayerProfileCache().getProfileByUUID(uuid);
            if (gp != null) {
                ret = gp.getName();
            }
        }
        if (ret == null && !warnedFails.contains(uuid)) {
            Mekanism.logger.warn("Failed to retrieve username for UUID {}, you might want to add it to the JSON cache", uuid);
            warnedFails.add(uuid);
        }
        return ret != null ? ret : "<???>";
    }

    /**
     * Copy of LivingEntity#onChangedPotionEffect(EffectInstance, boolean) due to not being able to AT the method as it is protected.
     */
    public static void onChangedPotionEffect(LivingEntity entity, EffectInstance id, boolean reapply) {
        entity.potionsNeedUpdate = true;
        if (reapply && !entity.world.isRemote) {
            Effect effect = id.getPotion();
            effect.removeAttributesModifiersFromEntity(entity, entity.getAttributeManager(), id.getAmplifier());
            effect.applyAttributesModifiersToEntity(entity, entity.getAttributeManager(), id.getAmplifier());
        }
        if (entity instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) entity).connection.sendPacket(new SPlayEntityEffectPacket(entity.getEntityId(), id));
            CriteriaTriggers.EFFECTS_CHANGED.trigger(((ServerPlayerEntity) entity));
        }
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

    public enum ResourceType {
        GUI("gui"),
        GUI_BUTTON("gui/button"),
        GUI_BAR("gui/bar"),
        GUI_HUD("gui/hud"),
        GUI_GAUGE("gui/gauge"),
        GUI_PROGRESS("gui/progress"),
        GUI_SLOT("gui/slot"),
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
}