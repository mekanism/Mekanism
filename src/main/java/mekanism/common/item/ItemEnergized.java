package mekanism.common.item;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.util.StorageUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;

public class ItemEnergized extends Item implements ICustomCreativeTabContents, ICapabilityAware {

    private final FloatingLongSupplier chargeRateSupplier;
    private final FloatingLongSupplier maxEnergySupplier;
    private final Predicate<@NotNull AutomationType> canExtract;
    private final Predicate<@NotNull AutomationType> canInsert;

    public ItemEnergized(FloatingLongSupplier chargeRateSupplier, FloatingLongSupplier maxEnergySupplier, Properties properties) {
        this(chargeRateSupplier, maxEnergySupplier, BasicEnergyContainer.manualOnly, BasicEnergyContainer.alwaysTrue, properties);
    }

    public ItemEnergized(FloatingLongSupplier chargeRateSupplier, FloatingLongSupplier maxEnergySupplier, Predicate<@NotNull AutomationType> canExtract,
          Predicate<@NotNull AutomationType> canInsert, Properties properties) {
        super(properties.stacksTo(1));
        this.chargeRateSupplier = chargeRateSupplier;
        this.maxEnergySupplier = maxEnergySupplier;
        this.canExtract = canExtract;
        this.canInsert = canInsert;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getEnergyBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return MekanismConfig.client.energyColor.get();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        StorageUtils.addStoredEnergy(stack, tooltip, true);
    }

    @Override
    public void addItems(CreativeModeTab.Output tabOutput) {
        if (maxEnergySupplier instanceof CachedFloatingLongValue configValue) {
            tabOutput.accept(StorageUtils.getFilledEnergyVariant(new ItemStack(this), configValue));
        } else {
            tabOutput.accept(StorageUtils.getFilledEnergyVariant(new ItemStack(this), maxEnergySupplier.get()));
        }
    }

    protected FloatingLong getMaxEnergy(ItemStack stack) {
        return maxEnergySupplier.get();
    }

    protected FloatingLong getChargeRate(ItemStack stack) {
        return chargeRateSupplier.get();
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        EnergyCompatUtils.registerItemCapabilities(event, this, (stack, ctx) -> {
            if (!MekanismConfig.gear.isLoaded()) {//Only expose the capabilities if the required configs are loaded
                return null;
            }
            //Note: We interact with this capability using "manual" as the automation type, to ensure we can properly bypass the energy limit for extracting
            // Internal is used by the "null" side, which is what will get used for most items
            //TODO - 1.20.2: Figure out if we can inline the calculating of charge rate and max energy from stack to here or if it is still good to have them be suppliers
            return RateLimitEnergyHandler.create(stack, () -> getChargeRate(stack), () -> getMaxEnergy(stack), canExtract, canInsert);
        });
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        //Ignore NBT for energized items causing re-equip animations
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        //Ignore NBT for energized items causing block break reset
        return oldStack.getItem() != newStack.getItem();
    }
}