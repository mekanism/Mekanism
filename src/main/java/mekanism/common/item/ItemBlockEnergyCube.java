package mekanism.common.item;

import cofh.redstoneflux.api.IEnergyContainerItem;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Range4D;
import mekanism.api.TileNetworkList;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismClient;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.base.ITierItem;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.forgeenergy.ForgeEnergyItemWrapper;
import mekanism.common.integration.ic2.IC2ItemManager;
import mekanism.common.integration.tesla.TeslaItemWrapper;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@InterfaceList({
      @Interface(iface = "cofh.redstoneflux.api.IEnergyContainerItem", modid = MekanismHooks.REDSTONEFLUX_MOD_ID),
      @Interface(iface = "ic2.api.item.ISpecialElectricItem", modid = MekanismHooks.IC2_MOD_ID)
})
public class ItemBlockEnergyCube extends ItemBlock implements IEnergizedItem, ISpecialElectricItem, ISustainedInventory, IEnergyContainerItem, ISecurityItem, ITierItem {

    public Block metaBlock;

    public ItemBlockEnergyCube(Block block) {
        super(block);
        metaBlock = block;
        setMaxStackSize(1);
        setHasSubtypes(true);
        setNoRepair();
        setCreativeTab(Mekanism.tabMekanism);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list, @Nonnull ITooltipFlag flag) {
        list.add(EnumColor.BRIGHT_GREEN + LangUtils.localize("tooltip.storedEnergy") + ": " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(getEnergy(itemstack)));
        list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.capacity") + ": " + EnumColor.GREY +
                 MekanismUtils.getEnergyDisplay(EnergyCubeTier.get(getBaseTier(itemstack)).getMaxEnergy()));

        if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.AQUA + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) +
                     EnumColor.GREY + " " + LangUtils.localize("tooltip.forDetails") + ".");
        } else {
            if (hasSecurity(itemstack)) {
                list.add(SecurityUtils.getOwnerDisplay(Minecraft.getMinecraft().player, MekanismClient.clientUUIDMap.get(getOwnerUUID(itemstack))));
                list.add(EnumColor.GREY + LangUtils.localize("gui.security") + ": " + SecurityUtils.getSecurityDisplay(itemstack, Side.CLIENT));
                if (SecurityUtils.isOverridden(itemstack, Side.CLIENT)) {
                    list.add(EnumColor.RED + "(" + LangUtils.localize("gui.overridden") + ")");
                }
            }
            list.add(EnumColor.AQUA + LangUtils.localize("tooltip.inventory") + ": " + EnumColor.GREY +
                     LangUtils.transYesNo(getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
        }
    }

    public ItemStack getUnchargedItem(EnergyCubeTier tier) {
        ItemStack stack = new ItemStack(this);
        setBaseTier(stack, tier.getBaseTier());
        return stack;
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack itemstack) {
        return LangUtils.localize("tile.EnergyCube" + getBaseTier(itemstack).getSimpleName() + ".name");
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY,
          float hitZ, @Nonnull IBlockState state) {
        boolean place = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state);

        if (place) {
            TileEntityEnergyCube tileEntity = (TileEntityEnergyCube) world.getTileEntity(pos);
            tileEntity.tier = EnergyCubeTier.get(getBaseTier(stack));
            tileEntity.electricityStored = getEnergy(stack);
            if (tileEntity.tier == EnergyCubeTier.CREATIVE) {
                tileEntity.configComponent.fillConfig(TransmissionType.ENERGY, tileEntity.getEnergy() > 0 ? 2 : 1);
            }
            ((ISecurityTile) tileEntity).getSecurity().setOwnerUUID(getOwnerUUID(stack));
            if (hasSecurity(stack)) {
                ((ISecurityTile) tileEntity).getSecurity().setMode(getSecurity(stack));
            }
            if (getOwnerUUID(stack) == null) {
                ((ISecurityTile) tileEntity).getSecurity().setOwnerUUID(player.getUniqueID());
            }
            if (ItemDataUtils.hasData(stack, "sideDataStored")) {
                ((ISideConfiguration) tileEntity).getConfig().read(ItemDataUtils.getDataMap(stack));
                ((ISideConfiguration) tileEntity).getEjector().read(ItemDataUtils.getDataMap(stack));
            }
            ((ISustainedInventory) tileEntity).setInventory(getInventory(stack));
            if (!world.isRemote) {
                Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity), tileEntity.getNetworkedData(new TileNetworkList())),
                      new Range4D(Coord4D.get(tileEntity)));
            }
        }
        return place;
    }

    @Override
    public BaseTier getBaseTier(ItemStack itemstack) {
        if (!itemstack.hasTagCompound()) {
            return BaseTier.getDefault();
        }
        return BaseTier.get(itemstack.getTagCompound().getInteger("tier"));
    }

    @Override
    public void setBaseTier(ItemStack itemstack, BaseTier tier) {
        if (!itemstack.hasTagCompound()) {
            itemstack.setTagCompound(new NBTTagCompound());
        }
        itemstack.getTagCompound().setInteger("tier", tier.ordinal());
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
        if (getBaseTier(itemStack) == BaseTier.CREATIVE && amount != Double.MAX_VALUE) {
            return;
        }
        ItemDataUtils.setDouble(itemStack, "energyStored", Math.max(Math.min(amount, getMaxEnergy(itemStack)), 0));
    }

    @Override
    public double getMaxEnergy(ItemStack itemStack) {
        return EnergyCubeTier.get(getBaseTier(itemStack)).getMaxEnergy();
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
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int receiveEnergy(ItemStack theItem, int energy, boolean simulate) {
        if (canReceive(theItem)) {
            double energyNeeded = getMaxEnergy(theItem) - getEnergy(theItem);
            double toReceive = Math.min(energy * MekanismConfig.current().general.FROM_RF.val(), energyNeeded);
            if (!simulate) {
                setEnergy(theItem, getEnergy(theItem) + toReceive);
            }
            return MekanismUtils.clampToInt(toReceive * MekanismConfig.current().general.TO_RF.val());
        }
        return 0;
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int extractEnergy(ItemStack theItem, int energy, boolean simulate) {
        if (canSend(theItem)) {
            double energyRemaining = getEnergy(theItem);
            double toSend = Math.min(energy * MekanismConfig.current().general.FROM_RF.val(), energyRemaining);
            if (!simulate) {
                setEnergy(theItem, getEnergy(theItem) - toSend);
            }
            return MekanismUtils.clampToInt(toSend * MekanismConfig.current().general.TO_RF.val());
        }
        return 0;
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int getEnergyStored(ItemStack theItem) {
        return (int) (getEnergy(theItem) * MekanismConfig.current().general.TO_RF.val());
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int getMaxEnergyStored(ItemStack theItem) {
        return (int) (getMaxEnergy(theItem) * MekanismConfig.current().general.TO_RF.val());
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
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public IElectricItemManager getManager(ItemStack itemStack) {
        return IC2ItemManager.getManager(this);
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
            return SecurityMode.getDefault();
        }
        return SecurityMode.get(ItemDataUtils.getInt(stack, "security"));
    }

    @Override
    public void setSecurity(ItemStack stack, SecurityMode mode) {
        ItemDataUtils.setInt(stack, "security", mode.ordinal());
    }

    @Override
    public boolean hasSecurity(ItemStack stack) {
        return true;
    }

    @Override
    public boolean hasOwner(ItemStack stack) {
        return hasSecurity(stack);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new ItemCapabilityWrapper(stack, new TeslaItemWrapper(), new ForgeEnergyItemWrapper());
    }
}