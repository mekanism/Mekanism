package mekanism.common.item;

import java.util.List;
import java.util.UUID;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.robit.RobitSkin;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.security.ISecurityObject;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.entity.EntityRobit;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.security.PacketSyncSecurity;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;

public class ItemRobit extends ItemEnergized implements IItemSustainedInventory, ICapabilityAware {

    public ItemRobit(Properties properties) {
        super(() -> EntityRobit.MAX_ENERGY.multiply(0.005), () -> EntityRobit.MAX_ENERGY, properties.rarity(Rarity.RARE));
    }

    @Override
    public void onDestroyed(@NotNull ItemEntity item, @NotNull DamageSource damageSource) {
        InventoryUtils.dropItemContents(item, damageSource);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.add(MekanismLang.ROBIT_NAME.translateColored(EnumColor.INDIGO, EnumColor.GRAY, stack.getData(MekanismAttachmentTypes.ROBIT_NAME)));
        tooltip.add(MekanismLang.ROBIT_SKIN.translateColored(EnumColor.INDIGO, EnumColor.GRAY, RobitSkin.getTranslatedName(stack.getData(MekanismAttachmentTypes.ROBIT_SKIN))));
        IItemSecurityUtils.INSTANCE.addSecurityTooltip(stack, tooltip);
        tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(hasSustainedInventory(stack))));
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        TileEntityMekanism chargepad = WorldUtils.getTileEntity(TileEntityChargepad.class, world, pos);
        if (chargepad != null && !chargepad.getActive()) {
            if (!world.isClientSide) {
                ItemStack stack = context.getItemInHand();
                EntityRobit robit = EntityRobit.create(world, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);
                if (robit == null) {
                    return InteractionResult.FAIL;
                }
                robit.setHome(chargepad.getTileGlobalPos());
                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                if (energyContainer != null) {
                    robit.getEnergyContainer().setEnergy(energyContainer.getEnergy());
                }
                UUID ownerUUID = IItemSecurityUtils.INSTANCE.getOwnerUUID(stack);
                if (ownerUUID == null) {
                    robit.setOwnerUUID(player.getUUID());
                    //If the robit doesn't already have an owner, make sure we portray this
                    PacketUtils.sendToAll(new PacketSyncSecurity(player.getUUID()));
                } else {
                    robit.setOwnerUUID(ownerUUID);
                }
                robit.setSustainedInventory(getSustainedInventory(stack));
                robit.setCustomName(stack.getData(MekanismAttachmentTypes.ROBIT_NAME));
                ISecurityObject securityObject = IItemSecurityUtils.INSTANCE.securityCapability(stack);
                if (securityObject != null) {
                    robit.setSecurityMode(securityObject.getSecurityMode());
                }
                robit.setSkin(stack.getData(MekanismAttachmentTypes.ROBIT_SKIN), player);
                world.addFreshEntity(robit);
                world.gameEvent(player, GameEvent.ENTITY_PLACE, robit.blockPosition());
                stack.shrink(1);
                CriteriaTriggers.SUMMONED_ENTITY.trigger((ServerPlayer) player, robit);
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(IItemSecurityUtils.INSTANCE.ownerCapability(), (stack, ctx) -> stack.getData(MekanismAttachmentTypes.SECURITY), this);
        event.registerItem(IItemSecurityUtils.INSTANCE.securityCapability(), (stack, ctx) -> stack.getData(MekanismAttachmentTypes.SECURITY), this);
    }
}
