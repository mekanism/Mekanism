package mekanism.common.item;

import java.util.UUID;
import java.util.function.Consumer;
import mekanism.api.robit.RobitSkin;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityMode;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.base.holiday.HolidayManager;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.capabilities.security.SecurityObject;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.entity.EntityRobit;
import mekanism.common.network.to_client.security.PacketSyncSecurity;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public class ItemRobit extends ItemEnergized implements ICapabilityAware {

    public ItemRobit(Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(1)
              .component(MekanismDataComponents.ROBIT_SKIN, MekanismRobitSkins.BASE)
              .component(MekanismDataComponents.SECURITY, SecurityMode.PUBLIC)
              .component(MekanismDataComponents.DEFAULT_MANUALLY_SELECTED, false)
        );
    }

    @Override
    public void onDestroyed(ItemEntity item, DamageSource damageSource) {
        InventoryUtils.dropItemContents(item, damageSource);
    }

    @Override
    @Deprecated
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        Component name = stack.get(MekanismDataComponents.ROBIT_NAME);
        if (name == null) {
            name = MekanismLang.ROBIT.translate();
        }
        tooltipAdder.accept(MekanismLang.ROBIT_NAME.translateColored(EnumColor.INDIGO, EnumColor.GRAY, name));
        tooltipAdder.accept(MekanismLang.ROBIT_SKIN.translateColored(EnumColor.INDIGO, EnumColor.GRAY, RobitSkin.getTranslatedName(stack.getOrDefault(MekanismDataComponents.ROBIT_SKIN, MekanismRobitSkins.BASE))));
        ItemAccess itemAccess = ItemAccessUtils.sideEffectFreeAccess(stack);
        IItemSecurityUtils.INSTANCE.addSecurityTooltip(itemAccess, tooltipAdder);
        tooltipAdder.accept(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.hasInventory(itemAccess)));
    }

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
            if (world instanceof ServerLevel level) {
                ItemStack stack = context.getItemInHand();
                //TODO - 26.1: Determine how we want to set the y offset
                //EntityRobit robit = EntityRobit.create(world, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);
                EntityRobit spawnedRobit = MekanismEntityTypes.ROBIT.get().spawn(level, robit -> {
                    robit.setHome(chargepad.getTileGlobalPos());
                    ItemAccess itemAccess = ItemAccessUtils.sideEffectFreeAccess(stack);
                    ItemResource itemType = itemAccess.getResource();
                    UUID ownerUUID = IItemSecurityUtils.INSTANCE.getOwnerUUID(itemAccess);
                    if (ownerUUID == null) {
                        robit.setOwnerUUID(player.getUUID(), null);
                        //If the robit doesn't already have an owner, make sure we portray this
                        PacketDistributor.sendToAllPlayers(new PacketSyncSecurity(player.getUUID()));
                    } else {
                        robit.setOwnerUUID(ownerUUID, null);
                    }
                    ContainerType.ENERGY.copyToContainer(robit.getEnergyContainer(), itemType);
                    ContainerType.ITEM.copyToContainers(robit.getInventorySlots(), itemType);
                    Component name = itemType.get(MekanismDataComponents.ROBIT_NAME);
                    if (name != null) {
                        robit.setCustomName(name);
                    }
                    ISecurityObject securityObject = IItemSecurityUtils.INSTANCE.securityCapability(itemAccess);
                    if (securityObject != null) {
                        robit.setSecurityMode(securityObject.getSecurityMode(), null);
                    }
                    robit.setSkin(itemType.getOrDefault(MekanismDataComponents.ROBIT_SKIN, MekanismRobitSkins.BASE), player);
                    robit.setDefaultSkinManuallySelected(itemType.getOrDefault(MekanismDataComponents.DEFAULT_MANUALLY_SELECTED, false));
                }, pos, EntitySpawnReason.SPAWN_ITEM_USE, false, false);
                if (spawnedRobit == null) {
                    return InteractionResult.FAIL;
                }
                world.gameEvent(player, GameEvent.ENTITY_PLACE, spawnedRobit.blockPosition());
                //TODO - 26.1: Do we want this to be consume?
                stack.shrink(1);
                CriteriaTriggers.SUMMONED_ENTITY.trigger((ServerPlayer) player, spawnedRobit);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(IItemSecurityUtils.INSTANCE.ownerCapability(), (_, itemAccess) -> new SecurityObject(itemAccess), this);
        event.registerItem(IItemSecurityUtils.INSTANCE.securityCapability(), (_, itemAccess) -> new SecurityObject(itemAccess), this);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!level.isClientSide() && HolidayManager.hasRobitSkinsToday() && !stack.getOrDefault(MekanismDataComponents.DEFAULT_MANUALLY_SELECTED, false)) {
            ResourceKey<RobitSkin> skin = stack.get(MekanismDataComponents.ROBIT_SKIN);
            if (skin == null || skin == MekanismRobitSkins.BASE) {
                //Randomize the robit's skin
                stack.set(MekanismDataComponents.ROBIT_SKIN, HolidayManager.getRandomBaseSkin(level.getRandom()));
            }
        }
    }
}
