package mekanism.common.item;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.attachments.IAttachmentAware;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.util.StorageUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.NotNull;

public class ItemEnergized extends Item implements ICustomCreativeTabContents, IAttachmentAware {

    private final FloatingLongSupplier chargeRateSupplier;
    private final FloatingLongSupplier maxEnergySupplier;
    protected final Predicate<@NotNull AutomationType> canExtract;
    protected final Predicate<@NotNull AutomationType> canInsert;

    public ItemEnergized(FloatingLongSupplier chargeRateSupplier, FloatingLongSupplier maxEnergySupplier, Properties properties) {
        this(chargeRateSupplier, maxEnergySupplier, BasicEnergyContainer.manualOnly, BasicEnergyContainer.alwaysTrue, properties.stacksTo(1));
    }

    public ItemEnergized(FloatingLongSupplier chargeRateSupplier, FloatingLongSupplier maxEnergySupplier, Predicate<@NotNull AutomationType> canExtract,
          Predicate<@NotNull AutomationType> canInsert, Properties properties) {
        super(properties);
        this.chargeRateSupplier = chargeRateSupplier;
        this.maxEnergySupplier = maxEnergySupplier;
        this.canExtract = canExtract;
        this.canInsert = canInsert;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        //If we are currently stacked, don't display the bar as it will overlap the stack count
        return stack.getCount() == 1;
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
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        StorageUtils.addStoredEnergy(stack, tooltip, true);
    }

    @Override
    public void addItems(CreativeModeTab.Output tabOutput) {
        tabOutput.accept(StorageUtils.getFilledEnergyVariant(new ItemStack(this)));
    }

    protected IEnergyContainer getDefaultEnergyContainer(ItemStack stack) {
        return RateLimitEnergyContainer.create(chargeRateSupplier, maxEnergySupplier, canExtract, canInsert);
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        //Note: We interact with this capability using "manual" as the automation type, to ensure we can properly bypass the energy limit for extracting
        // Internal is used by the "null" side, which is what will get used for most items
        ContainerType.ENERGY.addDefaultContainer(eventBus, this, this::getDefaultEnergyContainer, MekanismConfig.gear);
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