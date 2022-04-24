package mekanism.common.item;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.providers.IRobitSkinProvider;
import mekanism.api.robit.RobitSkin;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.security.item.ItemStackSecurityObject;
import mekanism.common.entity.EntityRobit;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.network.to_client.PacketSecurityUpdate;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

public class ItemRobit extends ItemEnergized implements IItemSustainedInventory {

    public ItemRobit(Properties properties) {
        super(() -> EntityRobit.MAX_ENERGY.multiply(0.005), () -> EntityRobit.MAX_ENERGY, properties.rarity(Rarity.RARE));
    }

    @Override
    public void onDestroyed(@Nonnull ItemEntity item, @Nonnull DamageSource damageSource) {
        InventoryUtils.dropItemContents(item, damageSource);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.add(MekanismLang.ROBIT_NAME.translateColored(EnumColor.INDIGO, EnumColor.GRAY, getRobitName(stack)));
        tooltip.add(MekanismLang.ROBIT_SKIN.translateColored(EnumColor.INDIGO, EnumColor.GRAY, getRobitSkin(stack)));
        MekanismAPI.getSecurityUtils().addSecurityTooltip(stack, tooltip);
        tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(hasInventory(stack))));
    }

    @Nonnull
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
                robit.setHome(chargepad.getTileCoord());
                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                if (energyContainer != null) {
                    robit.getEnergyContainer().setEnergy(energyContainer.getEnergy());
                }
                ISecurityUtils securityUtils = MekanismAPI.getSecurityUtils();
                UUID ownerUUID = securityUtils.getOwnerUUID(stack);
                if (ownerUUID == null) {
                    robit.setOwnerUUID(player.getUUID());
                    //If the robit doesn't already have an owner, make sure we portray this
                    Mekanism.packetHandler().sendToAll(new PacketSecurityUpdate(player.getUUID()));
                } else {
                    robit.setOwnerUUID(ownerUUID);
                }
                robit.setInventory(getInventory(stack));
                robit.setCustomName(getRobitName(stack));
                robit.setSecurityMode(stack.getCapability(Capabilities.SECURITY_OBJECT).map(ISecurityObject::getSecurityMode).orElse(SecurityMode.PUBLIC));
                robit.setSkin(getRobitSkin(stack), player);
                world.addFreshEntity(robit);
                world.gameEvent(player, GameEvent.ENTITY_PLACE, robit);
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public void setName(ItemStack stack, Component name) {
        ItemDataUtils.setString(stack, NBTConstants.NAME, Component.Serializer.toJson(name));
    }

    private Component getRobitName(ItemStack stack) {
        String name = ItemDataUtils.getString(stack, NBTConstants.NAME);
        return name.isEmpty() ? MekanismLang.ROBIT.translate() : Component.Serializer.fromJson(name);
    }

    public void setSkin(ItemStack stack, RobitSkin skin) {
        ItemDataUtils.setString(stack, NBTConstants.SKIN, skin.getRegistryName().toString());
    }

    public IRobitSkinProvider getRobitSkin(ItemStack stack) {
        String skin = ItemDataUtils.getString(stack, NBTConstants.SKIN);
        if (!skin.isEmpty()) {
            ResourceLocation rl = ResourceLocation.tryParse(skin);
            if (rl != null) {
                RobitSkin robitSkin = MekanismAPI.robitSkinRegistry().getValue(rl);
                if (robitSkin != null) {
                    return robitSkin;
                }
            }
        }
        return MekanismRobitSkins.BASE;
    }

    @Override
    protected void gatherCapabilities(List<ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
        capabilities.add(new ItemStackSecurityObject());
        super.gatherCapabilities(capabilities, stack, nbt);
    }
}