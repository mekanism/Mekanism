package mekanism.common.item;

import java.util.List;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.attachments.qio.PortableDashboardContents;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.capabilities.security.OwnerObject;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.network.to_client.qio.BulkQIOData;
import mekanism.common.inventory.container.item.PortableQIODashboardContainer;
import mekanism.common.item.interfaces.IColoredItem;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.lib.security.ItemSecurityUtils;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;

public class ItemPortableQIODashboard extends Item implements IFrequencyItem, IGuiItem, IColoredItem, ICapabilityAware {

    public ItemPortableQIODashboard(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE)
              .component(MekanismDataComponents.INSERT_INTO_FREQUENCY, true)
              .component(MekanismDataComponents.QIO_DASHBOARD, PortableDashboardContents.EMPTY)
        );
    }

    @Override
    public void onDestroyed(@NotNull ItemEntity item, @NotNull DamageSource damageSource) {
        InventoryUtils.dropItemContents(item, damageSource);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        IItemSecurityUtils.INSTANCE.addSecurityTooltip(stack, tooltip);
        MekanismUtils.addFrequencyItemTooltip(stack, tooltip);
        tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.hasInventory(stack)));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
        return ItemSecurityUtils.get().claimOrOpenGui(world, player, hand, getContainerType()::tryOpenGui);
    }

    @Override
    public void encodeContainerData(RegistryFriendlyByteBuf buf, ItemStack stack) {
        FrequencyAware<QIOFrequency> frequencyAware = stack.get(getFrequencyComponent());
        BulkQIOData.encodeToPacket(buf, frequencyAware == null ? null : frequencyAware.getFrequency(stack, getFrequencyComponent()));
    }

    @Override
    public ContainerTypeRegistryObject<PortableQIODashboardContainer> getContainerType() {
        return MekanismContainerTypes.PORTABLE_QIO_DASHBOARD;
    }

    @Override
    public FrequencyType<?> getFrequencyType() {
        return FrequencyType.QIO;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide && level.getGameTime() % (5 * SharedConstants.TICKS_PER_SECOND) == 0) {
            syncColorWithFrequency(stack);
        }
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(IItemSecurityUtils.INSTANCE.ownerCapability(), (stack, ctx) -> new OwnerObject(stack), this);
    }

    @NotNull
    @Override
    public DataComponentType<FrequencyAware<QIOFrequency>> getFrequencyComponent() {
        return MekanismDataComponents.QIO_FREQUENCY.get();
    }
}
