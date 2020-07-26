package mekanism.common.item;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemEnergized extends Item {

    private final FloatingLongSupplier chargeRateSupplier;
    private final FloatingLongSupplier maxEnergySupplier;
    private final Predicate<@NonNull AutomationType> canExtract;
    private final Predicate<@NonNull AutomationType> canInsert;

    public ItemEnergized(FloatingLongSupplier chargeRateSupplier, FloatingLongSupplier maxEnergySupplier, Properties properties) {
        this(chargeRateSupplier, maxEnergySupplier, BasicEnergyContainer.manualOnly, BasicEnergyContainer.alwaysTrue, properties);
    }

    public ItemEnergized(FloatingLongSupplier chargeRateSupplier, FloatingLongSupplier maxEnergySupplier, Predicate<@NonNull AutomationType> canExtract,
          Predicate<@NonNull AutomationType> canInsert, Properties properties) {
        super(properties.maxStackSize(1));
        this.chargeRateSupplier = chargeRateSupplier;
        this.maxEnergySupplier = maxEnergySupplier;
        this.canExtract = canExtract;
        this.canInsert = canInsert;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return StorageUtils.getEnergyDurabilityForDisplay(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return MekanismConfig.client.energyColor.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        StorageUtils.addStoredEnergy(stack, tooltip, true);
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
        if (isInGroup(group)) {
            items.add(StorageUtils.getFilledEnergyVariant(new ItemStack(this), maxEnergySupplier.get()));
        }
    }

    protected FloatingLong getMaxEnergy(ItemStack stack) {
        return maxEnergySupplier.get();
    }

    protected FloatingLong getChargeRate(ItemStack stack) {
        return chargeRateSupplier.get();
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        //Note: We interact with this capability using "manual" as the automation type, to ensure we can properly bypass the energy limit for extracting
        // Internal is used by the "null" side, which is what will get used for most items
        return new ItemCapabilityWrapper(stack, RateLimitEnergyHandler.create(() -> getChargeRate(stack), () -> getMaxEnergy(stack), canExtract, canInsert));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        //Ignore NBT for energized items causing re-equip animations
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        //Ignore NBT for energized items causing block break reset
        return oldStack.getItem() != newStack.getItem();
    }
}