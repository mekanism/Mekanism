package mekanism.common.item;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.network.PacketSecurityUpdate;
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
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add(MekanismLang.ROBIT_NAME.translateColored(EnumColor.INDIGO, EnumColor.GRAY, getName(stack)));
        SecurityUtils.addSecurityTooltip(stack, tooltip);
        tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(hasInventory(stack))));
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResultType.PASS;
        }
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        TileEntityMekanism chargepad = WorldUtils.getTileEntity(TileEntityChargepad.class, world, pos);
        if (chargepad != null) {
            if (!chargepad.getActive()) {
                if (!world.isRemote) {
                    ItemStack stack = context.getItem();
                    EntityRobit robit = new EntityRobit(world, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);
                    robit.setHome(Coord4D.get(chargepad));
                    IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                    if (energyContainer != null) {
                        robit.getEnergyContainer().setEnergy(energyContainer.getEnergy());
                    }
                    UUID ownerUUID = getOwnerUUID(stack);
                    if (ownerUUID == null) {
                        robit.setOwnerUUID(player.getUniqueID());
                        //If the robit doesn't already have an owner, make sure we portray this
                        Mekanism.packetHandler.sendToAll(new PacketSecurityUpdate(player.getUniqueID(), null));
                    } else {
                        robit.setOwnerUUID(ownerUUID);
                    }
                    robit.setInventory(getInventory(stack));
                    robit.setCustomName(getName(stack));
                    robit.setSecurityMode(getSecurity(stack));
                    world.addEntity(robit);
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

    public ITextComponent getName(ItemStack stack) {
        String name = ItemDataUtils.getString(stack, NBTConstants.NAME);
        return name.isEmpty() ? MekanismLang.ROBIT.translate() : ITextComponent.Serializer.getComponentFromJson(name);
    }
}