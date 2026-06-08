package mekanism.common.item.block;

import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.Upgrade;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.resource.LargeResourceStack;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.IAttachmentAware;
import mekanism.common.attachments.component.UpgradeAware;
import mekanism.common.attachments.containers.creator.IContainerCreator;
import mekanism.common.attachments.containers.energy.ComponentBackedEnergyContainer;
import mekanism.common.attachments.containers.energy.EnergyContainersBuilder;
import mekanism.common.attachments.containers.type.ContainerType;
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
import mekanism.common.util.ItemAccessUtils;
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
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
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
    @Deprecated
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.descriptionKey)) {
            tooltipAdder.accept(getBlock().getDescription().translate());
        } else if (hasDetails && MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            addDetails(stack, ItemAccessUtils.sideEffectFreeAccess(stack), context, tooltipDisplay, tooltipAdder, flag);
        } else {
            addStats(stack, ItemAccessUtils.sideEffectFreeAccess(stack), context, tooltipDisplay, tooltipAdder, flag);
            if (hasDetails) {
                tooltipAdder.accept(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
            }
            tooltipAdder.accept(MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(EnumColor.GRAY, EnumColor.AQUA, MekanismKeyHandler.descriptionKey.getTranslatedKeyMessage()));
        }
    }

    protected void addStats(@NotNull ItemStack stack, @NotNull ItemAccess itemAccess, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay,
          @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
    }

    protected void addDetails(@NotNull ItemStack stack, @NotNull ItemAccess itemAccess, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay,
          @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        //Note: Security and owner info gets skipped if the stack doesn't expose them
        IItemSecurityUtils.INSTANCE.addSecurityTooltip(itemAccess, tooltipAdder);
        addTypeDetails(stack, itemAccess, context, tooltipDisplay, tooltipAdder, flag);
        //TODO: Make this support "multiple" fluid types (and maybe display multiple tanks of the same fluid)
        LargeResourceStack<FluidResource> fluidStack = ContainerType.FLUID.getStoredContentsFromAttachment(itemAccess);
        if (!fluidStack.isEmpty()) {
            tooltipAdder.accept(MekanismLang.GENERIC_STORED_MB.translateColored(EnumColor.PINK, fluidStack.resource(), EnumColor.GRAY, TextUtils.format(fluidStack.amount())));
        }
        if (Attribute.has(getBlock(), AttributeInventory.class) && ContainerType.ITEM.supports(itemAccess.getResource())) {
            tooltipAdder.accept(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.hasInventory(itemAccess)));
        }
        if (Attribute.has(getBlock(), AttributeUpgradeSupport.class)) {
            UpgradeAware upgradeAware = itemAccess.getResource().get(MekanismDataComponents.UPGRADES);
            if (upgradeAware != null) {
                for (Entry<Upgrade, Integer> entry : upgradeAware.upgrades().entrySet()) {
                    tooltipAdder.accept(UpgradeDisplay.of(entry.getKey(), entry.getValue()).getTextComponent());
                }
            }
        }
    }

    protected void addTypeDetails(@NotNull ItemStack stack, @NotNull ItemAccess itemAccess, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay,
          @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        //Put this here so that energy cubes can skip rendering energy here
        if (exposesEnergyCapOrTooltips()) {
            StorageUtils.addStoredEnergy(itemAccess, tooltipAdder, false);
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

    protected IContainerCreator<IEnergyContainer, Long> getDefaultEnergyContainer() {
        BLOCK block = getBlock();
        AttributeEnergy attributeEnergy = Attribute.get(block, AttributeEnergy.class);
        if (attributeEnergy == null) {
            throw new IllegalStateException("Expected block " + block + " to have the energy attribute");
        }
        LongSupplier maxEnergy = attributeEnergy::getStorage;
        if (Attribute.matches(block, AttributeUpgradeSupport.class, attribute -> attribute.supportedUpgrades().contains(Upgrade.ENERGY))) {
            return EnergyContainersBuilder.creator(attachedAccess -> {
                //If our block supports energy upgrades, make a more dynamically updating cache for our item's max energy
                LongSupplier capacity = new UpgradeBasedUnsignedLongCache(attachedAccess, maxEnergy);
                return new ComponentBackedEnergyContainer(attachedAccess, BasicEnergyContainer.manualOnly, getEnergyCapInsertPredicate(),
                      capacity, () -> MekanismUtils.calculateUsage(capacity.getAsLong()));
            });
        }
        //If we don't support energy upgrades, our max energy isn't dependent on another attachment, we can safely clamp to the config values
        return EnergyContainersBuilder.basicCreator(BasicEnergyContainer.manualOnly, getEnergyCapInsertPredicate(), () -> MekanismUtils.calculateUsage(maxEnergy.getAsLong()), maxEnergy);
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        if (Attribute.has(getBlock(), AttributeSecurity.class)) {
            event.registerItem(IItemSecurityUtils.INSTANCE.ownerCapability(), (_, itemAccess) -> new SecurityObject(itemAccess), this);
            event.registerItem(IItemSecurityUtils.INSTANCE.securityCapability(), (_, itemAccess) -> new SecurityObject(itemAccess), this);
        }
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        if (Attribute.has(getBlock(), AttributeEnergy.class)) {
            //Only expose the capability the required configs are loaded and the item wants to
            IEventBus energyEventBus = exposesEnergyCap() ? eventBus : null;
            ContainerType.ENERGY.addDefaultCreators(energyEventBus, this, this::getDefaultEnergyContainer,
                  MekanismConfig.storage, MekanismConfig.usage);
        }
    }

    private static class UpgradeBasedUnsignedLongCache implements LongSupplier {

        //TODO: Eventually fix this, ideally we want this to update the overall cached value if this changes because of the config
        // for how much energy a machine can store changes
        private final LongSupplier baseStorage;
        private final ItemAccess attachedAccess;
        private int lastInstalled;
        private long value;

        private UpgradeBasedUnsignedLongCache(ItemAccess attachedAccess, LongSupplier baseStorage) {
            this.attachedAccess = attachedAccess;
            UpgradeAware upgradeAware = this.attachedAccess.getResource().getOrDefault(MekanismDataComponents.UPGRADES, UpgradeAware.EMPTY);
            this.lastInstalled = upgradeAware.getUpgradeCount(Upgrade.ENERGY);
            this.baseStorage = baseStorage;
            this.value = MekanismUtils.getMaxEnergy(this.lastInstalled, this.baseStorage.getAsLong());
        }

        @Override
        public long getAsLong() {
            UpgradeAware upgradeAware = attachedAccess.getResource().getOrDefault(MekanismDataComponents.UPGRADES, UpgradeAware.EMPTY);
            int installed = upgradeAware.getUpgradeCount(Upgrade.ENERGY);
            if (installed != lastInstalled) {
                lastInstalled = installed;
                value = MekanismUtils.getMaxEnergy(this.lastInstalled, baseStorage.getAsLong());
            }
            return value;
        }
    }
}
