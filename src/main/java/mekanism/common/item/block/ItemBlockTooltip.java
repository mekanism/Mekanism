package mekanism.common.item.block;

import java.util.List;
import java.util.Map.Entry;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.Upgrade;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.IAttachmentAware;
import mekanism.common.attachments.component.UpgradeAware;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.energy.ComponentBackedNoClampEnergyContainer;
import mekanism.common.attachments.containers.energy.EnergyContainersBuilder;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.attribute.AttributeHasBounding;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.Attributes.AttributeInventory;
import mekanism.common.block.attribute.Attributes.AttributeSecurity;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.security.SecurityObject;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.TextUtils;
import mekanism.common.util.text.UpgradeDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class ItemBlockTooltip<BLOCK extends Block & IHasDescription> extends ItemBlockMekanism<BLOCK> implements ICapabilityAware, IAttachmentAware {

    private final boolean hasDetails;

    public ItemBlockTooltip(BLOCK block, Item.Properties properties) {
        this(block, false, properties);
    }

    public ItemBlockTooltip(BLOCK block, boolean hasDetails, Properties properties) {
        super(block, properties);
        this.hasDetails = hasDetails;
    }

    @Override
    public void onDestroyed(@NotNull ItemEntity item, @NotNull DamageSource damageSource) {
        //Try to drop the inventory contents if we are a block item that persists our inventory
        InventoryUtils.dropItemContents(item, damageSource);
    }

    @Override
    public boolean placeBlock(@NotNull BlockPlaceContext context, @NotNull BlockState state) {
        AttributeHasBounding hasBounding = Attribute.get(state, AttributeHasBounding.class);
        if (hasBounding == null) {
            return super.placeBlock(context, state);
        }

        return hasBounding.handle(context.getLevel(), context.getClickedPos(), state, context, (level, pos, ctx) -> WorldUtils.isValidReplaceableBlock(level, ctx, pos)) && super.placeBlock(context, state);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.descriptionKey)) {
            tooltip.add(getBlock().getDescription().translate());
        } else if (hasDetails && MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            addDetails(stack, context, tooltip, flag);
        } else {
            addStats(stack, context, tooltip, flag);
            if (hasDetails) {
                tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
            }
            tooltip.add(MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(EnumColor.GRAY, EnumColor.AQUA, MekanismKeyHandler.descriptionKey.getTranslatedKeyMessage()));
        }
    }

    protected void addStats(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
    }

    protected void addDetails(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        //Note: Security and owner info gets skipped if the stack doesn't expose them
        IItemSecurityUtils.INSTANCE.addSecurityTooltip(stack, tooltip);
        addTypeDetails(stack, context, tooltip, flag);
        //TODO: Make this support "multiple" fluid types (and maybe display multiple tanks of the same fluid)
        FluidStack fluidStack = StorageUtils.getStoredFluidFromAttachment(stack);
        if (!fluidStack.isEmpty()) {
            tooltip.add(MekanismLang.GENERIC_STORED_MB.translateColored(EnumColor.PINK, fluidStack, EnumColor.GRAY, TextUtils.format(fluidStack.getAmount())));
        }
        if (Attribute.has(getBlock(), AttributeInventory.class) && ContainerType.ITEM.supports(stack)) {
            tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.hasInventory(stack)));
        }
        if (Attribute.has(getBlock(), AttributeUpgradeSupport.class)) {
            UpgradeAware upgradeAware = stack.get(MekanismDataComponents.UPGRADES);
            if (upgradeAware != null) {
                for (Entry<Upgrade, Integer> entry : upgradeAware.upgrades().entrySet()) {
                    tooltip.add(UpgradeDisplay.of(entry.getKey(), entry.getValue()).getTextComponent());
                }
            }
        }
    }

    protected void addTypeDetails(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        //Put this here so that energy cubes can skip rendering energy here
        if (exposesEnergyCapOrTooltips()) {
            StorageUtils.addStoredEnergy(stack, tooltip, false);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        if (exposesEnergyCapOrTooltips()) {
            //Ignore NBT for energized items causing re-equip animations
            //TODO: Only ignore the energy attachment?
            return slotChanged || oldStack.getItem() != newStack.getItem();
        }
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(@NotNull ItemStack oldStack, @NotNull ItemStack newStack) {
        if (exposesEnergyCapOrTooltips()) {
            //Ignore NBT for energized items causing block break reset
            //TODO: Only ignore the energy attachment?
            return oldStack.getItem() != newStack.getItem();
        }
        return super.shouldCauseBlockBreakReset(oldStack, newStack);
    }

    protected Predicate<@NotNull AutomationType> getEnergyCapInsertPredicate() {
        return ConstantPredicates.alwaysTrue();
    }

    protected boolean exposesEnergyCap() {
        return exposesEnergyCapOrTooltips();
    }

    protected boolean exposesEnergyCapOrTooltips() {
        return Attribute.has(getBlock(), AttributeEnergy.class);
    }

    protected EnergyContainersBuilder addDefaultEnergyContainers(EnergyContainersBuilder builder) {
        BLOCK block = getBlock();
        AttributeEnergy attributeEnergy = Attribute.get(block, AttributeEnergy.class);
        if (attributeEnergy == null) {
            throw new IllegalStateException("Expected block " + block + " to have the energy attribute");
        }
        LongSupplier maxEnergy = attributeEnergy::getStorage;
        if (Attribute.matches(block, AttributeUpgradeSupport.class, attribute -> attribute.supportedUpgrades().contains(Upgrade.ENERGY))) {
            return builder.addContainer((type, attachedTo, containerIndex) -> {
                //If our block supports energy upgrades, make a more dynamically updating cache for our item's max energy
                LongSupplier capacity = new UpgradeBasedUnsignedLongCache(attachedTo, maxEnergy);
                return new ComponentBackedNoClampEnergyContainer(attachedTo, containerIndex, BasicEnergyContainer.manualOnly, getEnergyCapInsertPredicate(),
                      () -> MekanismUtils.calculateUsage(capacity.getAsLong()), capacity);
            });
        }
        //If we don't support energy upgrades, our max energy isn't dependent on another attachment, we can safely clamp to the config values
        return builder.addBasic(BasicEnergyContainer.manualOnly, getEnergyCapInsertPredicate(), () -> MekanismUtils.calculateUsage(maxEnergy.getAsLong()), maxEnergy);
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        if (Attribute.has(getBlock(), AttributeSecurity.class)) {
            event.registerItem(IItemSecurityUtils.INSTANCE.ownerCapability(), (stack, ctx) -> new SecurityObject(stack), this);
            event.registerItem(IItemSecurityUtils.INSTANCE.securityCapability(), (stack, ctx) -> new SecurityObject(stack), this);
        }
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        if (Attribute.has(getBlock(), AttributeEnergy.class)) {
            //Only expose the capability the required configs are loaded and the item wants to
            IEventBus energyEventBus = exposesEnergyCap() ? eventBus : null;
            ContainerType.ENERGY.addDefaultCreators(energyEventBus, this, () -> addDefaultEnergyContainers(EnergyContainersBuilder.builder()).build(),
                  MekanismConfig.storage, MekanismConfig.usage);
        }
    }

    private static class UpgradeBasedUnsignedLongCache implements LongSupplier {

        //TODO: Eventually fix this, ideally we want this to update the overall cached value if this changes because of the config
        // for how much energy a machine can store changes
        private final LongSupplier baseStorage;
        private final ItemStack stack;
        private int lastInstalled;
        private long value;

        private UpgradeBasedUnsignedLongCache(ItemStack stack, LongSupplier baseStorage) {
            this.stack = stack;
            UpgradeAware upgradeAware = this.stack.getOrDefault(MekanismDataComponents.UPGRADES, UpgradeAware.EMPTY);
            this.lastInstalled = upgradeAware.getUpgradeCount(Upgrade.ENERGY);
            this.baseStorage = baseStorage;
            this.value = MekanismUtils.getMaxEnergy(this.lastInstalled, this.baseStorage.getAsLong());
        }

        @Override
        public long getAsLong() {
            UpgradeAware upgradeAware = stack.getOrDefault(MekanismDataComponents.UPGRADES, UpgradeAware.EMPTY);
            int installed = upgradeAware.getUpgradeCount(Upgrade.ENERGY);
            if (installed != lastInstalled) {
                lastInstalled = installed;
                value = MekanismUtils.getMaxEnergy(this.lastInstalled, baseStorage.getAsLong());
            }
            return value;
        }
    }
}
