package mekanism.common.item;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.robit.RobitSkin;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityMode;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.item.ComponentBackedItemHandler;
import mekanism.common.base.holiday.HolidayManager;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.capabilities.security.SecurityObject;
import mekanism.common.entity.EntityRobit;
import mekanism.common.network.to_client.security.PacketSyncSecurity;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.advancements.CriteriaTriggers;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemRobit extends ItemEnergized implements ICapabilityAware {

    public ItemRobit(Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(1)
              .component(MekanismDataComponents.ROBIT_SKIN, MekanismRobitSkins.BASE)
              .component(MekanismDataComponents.SECURITY, SecurityMode.PUBLIC)
              .component(MekanismDataComponents.DEFAULT_MANUALLY_SELECTED, false)
        );
    }

    @Override
    public void onDestroyed(@NotNull ItemEntity item, @NotNull DamageSource damageSource) {
        InventoryUtils.dropItemContents(item, damageSource);
    }

    @Override
    @Deprecated
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        Component name = stack.get(MekanismDataComponents.ROBIT_NAME);
        if (name == null) {
            name = MekanismLang.ROBIT.translate();
        }
        tooltipAdder.accept(MekanismLang.ROBIT_NAME.translateColored(EnumColor.INDIGO, EnumColor.GRAY, name));
        tooltipAdder.accept(MekanismLang.ROBIT_SKIN.translateColored(EnumColor.INDIGO, EnumColor.GRAY, RobitSkin.getTranslatedName(stack.getOrDefault(MekanismDataComponents.ROBIT_SKIN, MekanismRobitSkins.BASE))));
        IItemSecurityUtils.INSTANCE.addSecurityTooltip(stack, tooltipAdder);
        tooltipAdder.accept(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.hasInventory(stack)));
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
            if (world instanceof ServerLevel level) {
                ItemStack stack = context.getItemInHand();
                //TODO - 26.1: Determine how we want to set the y offset
                //EntityRobit robit = EntityRobit.create(world, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);
                EntityRobit spawnedRobit = MekanismEntityTypes.ROBIT.get().spawn(level, robit -> {
                    robit.setHome(chargepad.getTileGlobalPos());
                    IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                    if (energyContainer != null) {
                        robit.getEnergyContainer().setEnergy(energyContainer.getEnergy());
                    }
                    UUID ownerUUID = IItemSecurityUtils.INSTANCE.getOwnerUUID(stack);
                    if (ownerUUID == null) {
                        robit.setOwnerUUID(player.getUUID());
                        //If the robit doesn't already have an owner, make sure we portray this
                        PacketDistributor.sendToAllPlayers(new PacketSyncSecurity(player.getUUID()));
                    } else {
                        robit.setOwnerUUID(ownerUUID);
                    }
                    List<IInventorySlot> robitSlots = robit.getInventorySlots();
                    ComponentBackedItemHandler stackInventory = ContainerType.ITEM.createHandlerIfData(stack);
                    if (stackInventory != null) {
                        int slot = 0;
                        for (IInventorySlot container : stackInventory.getContainers()) {
                            robitSlots.get(slot++).copyContents(container);
                        }
                    }
                    Component name = stack.get(MekanismDataComponents.ROBIT_NAME);
                    if (name != null) {
                        robit.setCustomName(name);
                    }
                    ISecurityObject securityObject = IItemSecurityUtils.INSTANCE.securityCapability(stack);
                    if (securityObject != null) {
                        robit.setSecurityMode(securityObject.getSecurityMode());
                    }
                    robit.setSkin(stack.getOrDefault(MekanismDataComponents.ROBIT_SKIN, MekanismRobitSkins.BASE), player);
                    robit.setDefaultSkinManuallySelected(stack.getOrDefault(MekanismDataComponents.DEFAULT_MANUALLY_SELECTED, false));
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
        event.registerItem(IItemSecurityUtils.INSTANCE.ownerCapability(), (stack, ctx) -> new SecurityObject(stack), this);
        event.registerItem(IItemSecurityUtils.INSTANCE.securityCapability(), (stack, ctx) -> new SecurityObject(stack), this);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull ServerLevel level, @NotNull Entity entity, @Nullable EquipmentSlot slot) {
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
