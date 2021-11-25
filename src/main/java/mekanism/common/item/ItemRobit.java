package mekanism.common.item;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.providers.IRobitSkinProvider;
import mekanism.api.robit.RobitSkin;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.network.to_client.PacketSecurityUpdate;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemRobit extends ItemEnergized implements IItemSustainedInventory, ISecurityItem {

    public ItemRobit(Properties properties) {
        super(() -> EntityRobit.MAX_ENERGY.multiply(0.005), () -> EntityRobit.MAX_ENERGY, properties.rarity(Rarity.RARE));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.add(MekanismLang.ROBIT_NAME.translateColored(EnumColor.INDIGO, EnumColor.GRAY, getRobitName(stack)));
        tooltip.add(MekanismLang.ROBIT_SKIN.translateColored(EnumColor.INDIGO, EnumColor.GRAY, getRobitSkin(stack)));
        SecurityUtils.addSecurityTooltip(stack, tooltip);
        tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(hasInventory(stack))));
    }

    @Nonnull
    @Override
    public ActionResultType useOn(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResultType.PASS;
        }
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        TileEntityMekanism chargepad = WorldUtils.getTileEntity(TileEntityChargepad.class, world, pos);
        if (chargepad != null) {
            if (!chargepad.getActive()) {
                if (!world.isClientSide) {
                    ItemStack stack = context.getItemInHand();
                    EntityRobit robit = EntityRobit.create(world, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);
                    if (robit == null) {
                        return ActionResultType.FAIL;
                    }
                    robit.setHome(chargepad.getTileCoord());
                    IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                    if (energyContainer != null) {
                        robit.getEnergyContainer().setEnergy(energyContainer.getEnergy());
                    }
                    UUID ownerUUID = getOwnerUUID(stack);
                    if (ownerUUID == null) {
                        robit.setOwnerUUID(player.getUUID());
                        //If the robit doesn't already have an owner, make sure we portray this
                        Mekanism.packetHandler.sendToAll(new PacketSecurityUpdate(player.getUUID(), null));
                    } else {
                        robit.setOwnerUUID(ownerUUID);
                    }
                    robit.setInventory(getInventory(stack));
                    robit.setCustomName(getRobitName(stack));
                    robit.setSecurityMode(getSecurity(stack));
                    robit.setSkin(getRobitSkin(stack), player);
                    world.addFreshEntity(robit);
                    stack.shrink(1);
                }
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    public void setName(ItemStack stack, ITextComponent name) {
        ItemDataUtils.setString(stack, NBTConstants.NAME, ITextComponent.Serializer.toJson(name));
    }

    private ITextComponent getRobitName(ItemStack stack) {
        String name = ItemDataUtils.getString(stack, NBTConstants.NAME);
        return name.isEmpty() ? MekanismLang.ROBIT.translate() : ITextComponent.Serializer.fromJson(name);
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
}