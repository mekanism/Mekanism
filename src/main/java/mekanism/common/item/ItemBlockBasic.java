package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Range4D;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.BinTier;
import mekanism.common.Tier.InductionCellTier;
import mekanism.common.Tier.InductionProviderTier;
import mekanism.common.base.ITierItem;
import mekanism.api.TileNetworkList;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionProvider;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Item class for handling multiple metal block IDs. 0:0: Osmium Block 0:1: Bronze Block 0:2: Refined Obsidian 0:3:
 * Charcoal Block 0:4: Refined Glowstone 0:5: Steel Block 0:6: Bin 0:7: Teleporter Frame 0:8: Steel Casing 0:9: Dynamic
 * Tank 0:10: Structural Glass 0:11: Dynamic Valve 0:12: Copper Block 0:13: Tin Block 0:14: Thermal Evaporation
 * Controller 0:15: Thermal Evaporation Valve 1:0: Thermal Evaporation Block 1:1: Induction Casing 1:2: Induction Port
 * 1:3: Induction Cell 1:4: Induction Provider 1:5: Superheating Element 1:6: Pressure Disperser 1:7: Boiler Casing 1:8:
 * Boiler Valve 1:9: Security Desk
 *
 * @author AidanBrady
 */
public class ItemBlockBasic extends ItemBlock implements IEnergizedItem, ITierItem {

    public Block metaBlock;

    public ItemBlockBasic(Block block) {
        super(block);
        metaBlock = block;
        setHasSubtypes(true);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        if (BasicBlockType.get(stack) == BasicBlockType.BIN) {
            return 1; // Temporary no stacking due to #
        }

        return super.getItemStackLimit(stack);
    }

    public ItemStack getUnchargedCell(InductionCellTier tier) {
        ItemStack stack = new ItemStack(MekanismBlocks.BasicBlock2, 1, 3);
        setBaseTier(stack, tier.getBaseTier());

        return stack;
    }

    public ItemStack getUnchargedProvider(InductionProviderTier tier) {
        ItemStack stack = new ItemStack(MekanismBlocks.BasicBlock2, 1, 4);
        setBaseTier(stack, tier.getBaseTier());

        return stack;
    }

    @Override
    public BaseTier getBaseTier(ItemStack itemstack) {
        if (itemstack.getTagCompound() == null) {
            return BaseTier.BASIC;
        }

        return BaseTier.values()[itemstack.getTagCompound().getInteger("tier")];
    }

    @Override
    public void setBaseTier(ItemStack itemstack, BaseTier tier) {
        if (itemstack.getTagCompound() == null) {
            itemstack.setTagCompound(new NBTTagCompound());
        }

        itemstack.getTagCompound().setInteger("tier", tier.ordinal());
    }

    @Override
    public int getMetadata(int i) {
        return i;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list,
          @Nonnull ITooltipFlag flag) {
        BasicBlockType type = BasicBlockType.get(itemstack);

        if (type != null && type.hasDescription) {
            if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
                if (type == BasicBlockType.BIN) {
                    InventoryBin inv = new InventoryBin(itemstack);

                    if (inv.getItemCount() > 0) {
                        list.add(EnumColor.BRIGHT_GREEN + inv.getItemType().getDisplayName());
                        String amountStr = inv.getItemCount() == Integer.MAX_VALUE ? LangUtils.localize("gui.infinite")
                              : "" + inv.getItemCount();
                        list.add(EnumColor.PURPLE + LangUtils.localize("tooltip.itemAmount") + ": " + EnumColor.GREY
                              + amountStr);
                    } else {
                        list.add(EnumColor.DARK_RED + LangUtils.localize("gui.empty"));
                    }

                    int cap = BinTier.values()[getBaseTier(itemstack).ordinal()].storage;
                    list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.capacity") + ": " + EnumColor.GREY + (
                          cap == Integer.MAX_VALUE ? LangUtils.localize("gui.infinite") : cap) + " " + LangUtils
                          .localize("transmission.Items"));
                } else if (type == BasicBlockType.INDUCTION_CELL) {
                    InductionCellTier tier = InductionCellTier.values()[getBaseTier(itemstack).ordinal()];

                    list.add(
                          tier.getBaseTier().getColor() + LangUtils.localize("tooltip.capacity") + ": " + EnumColor.GREY
                                + MekanismUtils.getEnergyDisplay(tier.maxEnergy));
                } else if (type == BasicBlockType.INDUCTION_PROVIDER) {
                    InductionProviderTier tier = InductionProviderTier.values()[getBaseTier(itemstack).ordinal()];

                    list.add(tier.getBaseTier().getColor() + LangUtils.localize("tooltip.outputRate") + ": "
                          + EnumColor.GREY + MekanismUtils.getEnergyDisplay(tier.output));
                }

                if (getMaxEnergy(itemstack) > 0) {
                    list.add(EnumColor.BRIGHT_GREEN + LangUtils.localize("tooltip.storedEnergy") + ": " + EnumColor.GREY
                          + MekanismUtils.getEnergyDisplay(getEnergy(itemstack)));
                }

                list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.INDIGO + GameSettings
                      .getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) + EnumColor.GREY + " " + LangUtils
                      .localize("tooltip.forDetails") + ".");
            } else {
                list.addAll(MekanismUtils.splitTooltip(type.getDescription(), itemstack));
            }
        }
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return BasicBlockType.get(stack) == BasicBlockType.BIN && ItemDataUtils.hasData(stack, "newCount");
    }

    @Nonnull
    @Override
    public ItemStack getContainerItem(@Nonnull ItemStack stack) {
        if (BasicBlockType.get(stack) == BasicBlockType.BIN) {
            if (!ItemDataUtils.hasData(stack, "newCount")) {
                return ItemStack.EMPTY;
            }

            int newCount = ItemDataUtils.getInt(stack, "newCount");
            ItemDataUtils.removeData(stack, "newCount");

            ItemStack ret = stack.copy();
            ItemDataUtils.setInt(ret, "itemCount", newCount);

            return ret;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world,
          @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull IBlockState state) {
        boolean place = true;

        BasicBlockType type = BasicBlockType.get(stack);

        if (type == BasicBlockType.SECURITY_DESK) {
            if (world.isOutsideBuildHeight(pos.up()) || !world.getBlockState(pos.up()).getBlock()
                  .isReplaceable(world, pos.up())) {
                place = false;
            }
        }

        if (place && super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state)) {
            if (type == BasicBlockType.BIN && stack.getTagCompound() != null) {
                TileEntityBin tileEntity = (TileEntityBin) world.getTileEntity(pos);
                InventoryBin inv = new InventoryBin(stack);

                tileEntity.tier = BinTier.values()[getBaseTier(stack).ordinal()];

                if (!inv.getItemType().isEmpty()) {
                    tileEntity.setItemType(inv.getItemType());
                }

                tileEntity.setItemCount(inv.getItemCount());
            } else if (type == BasicBlockType.INDUCTION_CELL) {
                TileEntityInductionCell tileEntity = (TileEntityInductionCell) world.getTileEntity(pos);
                tileEntity.tier = InductionCellTier.values()[getBaseTier(stack).ordinal()];

                if (!world.isRemote) {
                    Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity),
                          tileEntity.getNetworkedData(new TileNetworkList())), new Range4D(Coord4D.get(tileEntity)));
                }
            } else if (type == BasicBlockType.INDUCTION_PROVIDER) {
                TileEntityInductionProvider tileEntity = (TileEntityInductionProvider) world.getTileEntity(pos);
                tileEntity.tier = InductionProviderTier.values()[getBaseTier(stack).ordinal()];

                if (!world.isRemote) {
                    Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity),
                          tileEntity.getNetworkedData(new TileNetworkList())), new Range4D(Coord4D.get(tileEntity)));
                }
            }

            TileEntity tileEntity = world.getTileEntity(pos);

            if (tileEntity instanceof IStrictEnergyStorage && !(tileEntity instanceof TileEntityMultiblock<?>)) {
                ((IStrictEnergyStorage) tileEntity).setEnergy(getEnergy(stack));
            }
        }

        return place;
    }

    @Nonnull
    @Override
    public String getTranslationKey(ItemStack itemstack) {
        BasicBlockType type = BasicBlockType.get(itemstack);

        if (type != null) {
            String name = getTranslationKey() + "." + type.name;

            if (type == BasicBlockType.BIN || type == BasicBlockType.INDUCTION_CELL
                  || type == BasicBlockType.INDUCTION_PROVIDER) {
                name += getBaseTier(itemstack).getSimpleName();
            }

            return name;
        }

        return "Invalid Basic Block";
    }

    @Override
    public double getEnergy(ItemStack itemStack) {
        if (BasicBlockType.get(itemStack) == BasicBlockType.INDUCTION_CELL) {
            return ItemDataUtils.getDouble(itemStack, "energyStored");
        }

        return 0;
    }

    @Override
    public void setEnergy(ItemStack itemStack, double amount) {
        if (BasicBlockType.get(itemStack) == BasicBlockType.INDUCTION_CELL) {
            ItemDataUtils.setDouble(itemStack, "energyStored", Math.max(Math.min(amount, getMaxEnergy(itemStack)), 0));
        }
    }

    @Override
    public double getMaxEnergy(ItemStack itemStack) {
        if (BasicBlockType.get(itemStack) == BasicBlockType.INDUCTION_CELL) {
            return InductionCellTier.values()[getBaseTier(itemStack).ordinal()].maxEnergy;
        }

        return 0;
    }

    @Override
    public double getMaxTransfer(ItemStack itemStack) {
        return 0;
    }

    @Override
    public boolean canReceive(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return false;
    }

    @Override
    public int getItemBurnTime(ItemStack itemStack) {
        // If this is a block of charcoal, set burn time to 16000 ticks (per Minecraft standard)
        if (this.metaBlock == MekanismBlocks.BasicBlock && itemStack.getMetadata() == 3) {
            return 16000; // ticks
        } else {
            return super.getItemBurnTime(itemStack);
        }
    }
}
