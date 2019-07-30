package mekanism.common.item.block;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismClient;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.forgeenergy.ForgeEnergyItemWrapper;
import mekanism.common.integration.tesla.TeslaItemWrapper;
import mekanism.common.item.IItemEnergized;
import mekanism.common.item.IItemRedirectedModel;
import mekanism.common.item.ITieredItem;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockEnergyCube extends ItemBlockMekanism implements IItemEnergized, ISustainedInventory, ISecurityItem, ITieredItem<EnergyCubeTier>, IItemRedirectedModel {

    public ItemBlockEnergyCube(BlockEnergyCube block) {
        super(block);
        setMaxStackSize(1);
        setNoRepair();
    }

    @Nullable
    @Override
    public EnergyCubeTier getTier(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockEnergyCube) {
            return ((BlockEnergyCube) ((ItemBlockEnergyCube) item).block).getTier();
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list, @Nonnull ITooltipFlag flag) {
        list.add(EnumColor.BRIGHT_GREEN + LangUtils.localize("tooltip.storedEnergy") + ": " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(getEnergy(itemstack)));
        EnergyCubeTier tier = getTier(itemstack);
        if (tier != null) {
            list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.capacity") + ": " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(tier.getMaxEnergy()));
        }

        if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.AQUA + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) +
                     EnumColor.GREY + " " + LangUtils.localize("tooltip.forDetails") + ".");
        } else {
            list.add(SecurityUtils.getOwnerDisplay(Minecraft.getMinecraft().player, MekanismClient.clientUUIDMap.get(getOwnerUUID(itemstack))));
            list.add(EnumColor.GREY + LangUtils.localize("gui.security") + ": " + SecurityUtils.getSecurityDisplay(itemstack, Side.CLIENT));
            if (SecurityUtils.isOverridden(itemstack, Side.CLIENT)) {
                list.add(EnumColor.RED + "(" + LangUtils.localize("gui.overridden") + ")");
            }
            list.add(EnumColor.AQUA + LangUtils.localize("tooltip.inventory") + ": " + EnumColor.GREY +
                     LangUtils.transYesNo(getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
        }
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY,
          float hitZ, @Nonnull IBlockState state) {
        boolean place = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state);

        if (place) {
            EnergyCubeTier tier = getTier(stack);
            if (tier != null) {
                TileEntityEnergyCube tileEntity = (TileEntityEnergyCube) world.getTileEntity(pos);
                tileEntity.tier = tier;
                tileEntity.electricityStored = getEnergy(stack);
                if (tileEntity.tier == EnergyCubeTier.CREATIVE) {
                    tileEntity.configComponent.fillConfig(TransmissionType.ENERGY, tileEntity.getEnergy() > 0 ? 2 : 1);
                }
                ((ISecurityTile) tileEntity).getSecurity().setOwnerUUID(getOwnerUUID(stack));
                ((ISecurityTile) tileEntity).getSecurity().setMode(getSecurity(stack));
                if (getOwnerUUID(stack) == null) {
                    ((ISecurityTile) tileEntity).getSecurity().setOwnerUUID(player.getUniqueID());
                }
                if (ItemDataUtils.hasData(stack, "sideDataStored")) {
                    ((ISideConfiguration) tileEntity).getConfig().read(ItemDataUtils.getDataMap(stack));
                    ((ISideConfiguration) tileEntity).getEjector().read(ItemDataUtils.getDataMap(stack));
                }
                ((ISustainedInventory) tileEntity).setInventory(getInventory(stack));
                if (!world.isRemote) {
                    Mekanism.packetHandler.sendUpdatePacket(tileEntity);
                }
            }
        }
        return place;
    }

    @Override
    public void setInventory(NBTTagList nbtTags, Object... data) {
        if (data[0] instanceof ItemStack) {
            ItemDataUtils.setList((ItemStack) data[0], "Items", nbtTags);
        }
    }

    @Override
    public NBTTagList getInventory(Object... data) {
        if (data[0] instanceof ItemStack) {
            return ItemDataUtils.getList((ItemStack) data[0], "Items");
        }
        return null;
    }

    @Override
    public double getEnergy(ItemStack itemStack) {
        if (!itemStack.hasTagCompound()) {
            return 0;
        }
        return ItemDataUtils.getDouble(itemStack, "energyStored");
    }

    @Override
    public void setEnergy(ItemStack itemStack, double amount) {
        if (getTier(itemStack) == EnergyCubeTier.CREATIVE && amount != Double.MAX_VALUE) {
            return;
        }
        ItemDataUtils.setDouble(itemStack, "energyStored", Math.max(Math.min(amount, getMaxEnergy(itemStack)), 0));
    }

    @Override
    public double getMaxEnergy(ItemStack itemStack) {
        EnergyCubeTier tier = getTier(itemStack);
        return tier == null ? 0 : tier.getMaxEnergy();
    }

    @Override
    public double getMaxTransfer(ItemStack itemStack) {
        return getMaxEnergy(itemStack) * 0.005;
    }

    @Override
    public boolean canReceive(ItemStack itemStack) {
        return true;
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return true;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1D - (getEnergy(stack) / getMaxEnergy(stack));
    }

    @Override
    public int getRGBDurabilityForDisplay(@Nonnull ItemStack stack) {
        return MathHelper.hsvToRGB(Math.max(0.0F, (float) (1 - getDurabilityForDisplay(stack))) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public UUID getOwnerUUID(ItemStack stack) {
        if (ItemDataUtils.hasData(stack, "ownerUUID")) {
            return UUID.fromString(ItemDataUtils.getString(stack, "ownerUUID"));
        }
        return null;
    }

    @Override
    public void setOwnerUUID(ItemStack stack, UUID owner) {
        if (owner == null) {
            ItemDataUtils.removeData(stack, "ownerUUID");
        } else {
            ItemDataUtils.setString(stack, "ownerUUID", owner.toString());
        }
    }

    @Override
    public SecurityMode getSecurity(ItemStack stack) {
        if (!MekanismConfig.current().general.allowProtection.val()) {
            return SecurityMode.PUBLIC;
        }
        return SecurityMode.values()[ItemDataUtils.getInt(stack, "security")];
    }

    @Override
    public void setSecurity(ItemStack stack, SecurityMode mode) {
        ItemDataUtils.setInt(stack, "security", mode.ordinal());
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new ItemCapabilityWrapper(stack, new TeslaItemWrapper(), new ForgeEnergyItemWrapper());
    }

    @Nonnull
    @Override
    public String getRedirectLocation() {
        return "energy_cube";
    }
}