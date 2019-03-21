package mekanism.common.item;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Range4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismClient;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.GasTankTier;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.base.ITierItem;
import mekanism.api.TileNetworkList;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockGasTank extends ItemBlock implements IGasItem, ISustainedInventory, ITierItem, ISecurityItem {

    /**
     * How fast this tank can transfer gas.
     */
    public static final int TRANSFER_RATE = 256;
    public Block metaBlock;
    /**
     * The maximum amount of gas this tank can hold.
     */
    public int MAX_GAS = 96000;

    public ItemBlockGasTank(Block block) {
        super(block);
        metaBlock = block;
        setHasSubtypes(true);
        setMaxStackSize(1);
        setCreativeTab(Mekanism.tabMekanism);
    }

    @Override
    public int getMetadata(int i) {
        return i;
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack itemstack) {
        return LangUtils.localize("tile.GasTank" + getBaseTier(itemstack).getSimpleName() + ".name");
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world,
          @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull IBlockState state) {
        boolean place = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state);

        if (place) {
            TileEntityGasTank tileEntity = (TileEntityGasTank) world.getTileEntity(pos);
            tileEntity.tier = GasTankTier.values()[getBaseTier(stack).ordinal()];
            tileEntity.gasTank.setMaxGas(tileEntity.tier.storage);
            tileEntity.gasTank.setGas(getGas(stack));

            ISecurityTile security = tileEntity;
            security.getSecurity().setOwnerUUID(getOwnerUUID(stack));

            if (hasSecurity(stack)) {
                security.getSecurity().setMode(getSecurity(stack));
            }

            if (getOwnerUUID(stack) == null) {
                security.getSecurity().setOwnerUUID(player.getUniqueID());
            }

            ISideConfiguration config = tileEntity;

            if (ItemDataUtils.hasData(stack, "sideDataStored")) {
                config.getConfig().read(ItemDataUtils.getDataMap(stack));
                config.getEjector().read(ItemDataUtils.getDataMap(stack));
            }

            ((ISustainedInventory) tileEntity).setInventory(getInventory(stack));

            if (!world.isRemote) {
                Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity),
                      tileEntity.getNetworkedData(new TileNetworkList())), new Range4D(Coord4D.get(tileEntity)));
            }
        }

        return place;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list,
          @Nonnull ITooltipFlag flag) {
        GasStack gasStack = getGas(itemstack);

        if (gasStack == null) {
            list.add(EnumColor.DARK_RED + LangUtils.localize("gui.empty") + ".");
        } else {
            String amount =
                  "" + (gasStack.amount == Integer.MAX_VALUE ? LangUtils.localize("gui.infinite") : gasStack.amount);
            list.add(EnumColor.ORANGE + gasStack.getGas().getLocalizedName() + ": " + EnumColor.GREY + amount);
        }

        int cap = GasTankTier.values()[getBaseTier(itemstack).ordinal()].storage;
        list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.capacity") + ": " + EnumColor.GREY + (
              cap == Integer.MAX_VALUE ? LangUtils.localize("gui.infinite") : cap));

        if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.AQUA + GameSettings
                  .getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) + EnumColor.GREY + " " + LangUtils
                  .localize("tooltip.forDetails") + ".");
        } else {
            if (hasSecurity(itemstack)) {
                list.add(SecurityUtils.getOwnerDisplay(Minecraft.getMinecraft().player,
                      MekanismClient.clientUUIDMap.get(getOwnerUUID(itemstack))));
                list.add(EnumColor.GREY + LangUtils.localize("gui.security") + ": " + SecurityUtils
                      .getSecurityDisplay(itemstack, Side.CLIENT));

                if (SecurityUtils.isOverridden(itemstack, Side.CLIENT)) {
                    list.add(EnumColor.RED + "(" + LangUtils.localize("gui.overridden") + ")");
                }
            }

            list.add(EnumColor.AQUA + LangUtils.localize("tooltip.inventory") + ": " + EnumColor.GREY + LangUtils
                  .transYesNo(getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
        }
    }

    @Override
    public GasStack getGas(ItemStack itemstack) {
        return GasStack.readFromNBT(ItemDataUtils.getCompound(itemstack, "stored"));
    }

    @Override
    public void setGas(ItemStack itemstack, GasStack stack) {
        if (stack == null || stack.amount == 0) {
            ItemDataUtils.removeData(itemstack, "stored");
        } else {
            int amount = Math.max(0, Math.min(stack.amount, getMaxGas(itemstack)));
            GasStack gasStack = new GasStack(stack.getGas(), amount);

            ItemDataUtils.setCompound(itemstack, "stored", gasStack.write(new NBTTagCompound()));
        }
    }

    public ItemStack getEmptyItem(GasTankTier tier) {
        ItemStack empty = new ItemStack(this);
        setBaseTier(empty, tier.getBaseTier());
        setGas(empty, null);

        return empty;
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tabs, @Nonnull NonNullList<ItemStack> list) {
        if (!isInCreativeTab(tabs)) {
            return;
        }
        for (GasTankTier tier : GasTankTier.values()) {
            ItemStack empty = new ItemStack(this);
            setBaseTier(empty, tier.getBaseTier());
            list.add(empty);
        }

        if (general.prefilledGasTanks) {
            for (Gas type : GasRegistry.getRegisteredGasses()) {
                if (type.isVisible()) {
                    ItemStack filled = new ItemStack(this);
                    setBaseTier(filled, BaseTier.CREATIVE);
                    setGas(filled, new GasStack(type, ((IGasItem) filled.getItem()).getMaxGas(filled)));
                    list.add(filled);
                }
            }
        }
    }

    @Override
    public BaseTier getBaseTier(ItemStack itemstack) {
        if (!itemstack.hasTagCompound()) {
            return BaseTier.BASIC;
        }

        return BaseTier.values()[itemstack.getTagCompound().getInteger("tier")];
    }

    @Override
    public void setBaseTier(ItemStack itemstack, BaseTier tier) {
        if (!itemstack.hasTagCompound()) {
            itemstack.setTagCompound(new NBTTagCompound());
        }

        itemstack.getTagCompound().setInteger("tier", tier.ordinal());
    }

    @Override
    public int getMaxGas(ItemStack itemstack) {
        return GasTankTier.values()[getBaseTier(itemstack).ordinal()].storage;
    }

    @Override
    public int getRate(ItemStack itemstack) {
        return GasTankTier.values()[getBaseTier(itemstack).ordinal()].output;
    }

    @Override
    public int addGas(ItemStack itemstack, GasStack stack) {
        if (getGas(itemstack) != null && getGas(itemstack).getGas() != stack.getGas()) {
            return 0;
        }

        if (getBaseTier(itemstack) == BaseTier.CREATIVE) {
            setGas(itemstack, new GasStack(stack.getGas(), Integer.MAX_VALUE));
            return stack.amount;
        }

        int toUse = Math.min(getMaxGas(itemstack) - getStored(itemstack), Math.min(getRate(itemstack), stack.amount));
        setGas(itemstack, new GasStack(stack.getGas(), getStored(itemstack) + toUse));

        return toUse;
    }

    @Override
    public GasStack removeGas(ItemStack itemstack, int amount) {
        if (getGas(itemstack) == null) {
            return null;
        }

        Gas type = getGas(itemstack).getGas();

        int gasToUse = Math.min(getStored(itemstack), Math.min(getRate(itemstack), amount));

        if (getBaseTier(itemstack) != BaseTier.CREATIVE) {
            setGas(itemstack, new GasStack(type, getStored(itemstack) - gasToUse));
        }

        return new GasStack(type, gasToUse);
    }

    private int getStored(ItemStack itemstack) {
        return getGas(itemstack) != null ? getGas(itemstack).amount : 0;
    }

    @Override
    public boolean canReceiveGas(ItemStack itemstack, Gas type) {
        return getGas(itemstack) == null || getGas(itemstack).getGas() == type;
    }

    @Override
    public boolean canProvideGas(ItemStack itemstack, Gas type) {
        return getGas(itemstack) != null && (type == null || getGas(itemstack).getGas() == type);
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
    public boolean showDurabilityBar(ItemStack stack) {
        return getGas(stack) != null; // No bar for empty containers as bars are drawn on top of stack count number
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1D - ((getGas(stack) != null ? (double) getGas(stack).amount : 0D) / (double) getMaxGas(stack));
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
            return;
        }

        ItemDataUtils.setString(stack, "ownerUUID", owner.toString());
    }

    @Override
    public SecurityMode getSecurity(ItemStack stack) {
        if (!general.allowProtection) {
            return SecurityMode.PUBLIC;
        }

        return SecurityMode.values()[ItemDataUtils.getInt(stack, "security")];
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
}
