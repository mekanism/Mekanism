package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mcmultipart.api.multipart.IMultipart;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Range4D;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.Tier;
import mekanism.common.Tier.BaseTier;
import mekanism.common.base.ITierItem;
import mekanism.api.TileNetworkList;
import mekanism.common.block.states.BlockStateTransmitter.TransmitterType;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.multipart.MultipartMekanism;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.transmitter.TileEntitySidedPipe;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockTransmitter extends ItemBlockMultipartAble implements ITierItem {

    public Block metaBlock;

    public ItemBlockTransmitter(Block block) {
        super(block);
        metaBlock = block;
        setHasSubtypes(true);
        setCreativeTab(Mekanism.tabMekanism);
    }

    @Override
    public int getMetadata(int i) {
        return i;
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world,
          @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull IBlockState state) {
        boolean place = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state);

        if (place) {
            TileEntitySidedPipe tileEntity = (TileEntitySidedPipe) world.getTileEntity(pos);
            tileEntity.setBaseTier(getBaseTier(stack));

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
        if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            TransmissionType transmission = TransmitterType.values()[itemstack.getItemDamage()].getTransmission();
            BaseTier tier = getBaseTier(itemstack);

            if (transmission == TransmissionType.ENERGY) {
                list.add(
                      EnumColor.INDIGO + LangUtils.localize("tooltip.capacity") + ": " + EnumColor.GREY + MekanismUtils
                            .getEnergyDisplay(Tier.CableTier.get(tier).cableCapacity) + "/t");
            } else if (transmission == TransmissionType.FLUID) {
                list.add(
                      EnumColor.INDIGO + LangUtils.localize("tooltip.capacity") + ": " + EnumColor.GREY + Tier.PipeTier
                            .get(tier).pipeCapacity + "mB/t");
                list.add(
                      EnumColor.INDIGO + LangUtils.localize("tooltip.pumpRate") + ": " + EnumColor.GREY + Tier.PipeTier
                            .get(tier).pipePullAmount + "mB/t");
            } else if (transmission == TransmissionType.GAS) {
                list.add(
                      EnumColor.INDIGO + LangUtils.localize("tooltip.capacity") + ": " + EnumColor.GREY + Tier.TubeTier
                            .get(tier).tubeCapacity + "mB/t");
                list.add(
                      EnumColor.INDIGO + LangUtils.localize("tooltip.pumpRate") + ": " + EnumColor.GREY + Tier.TubeTier
                            .get(tier).tubePullAmount + "mB/t");
            } else if (transmission == TransmissionType.ITEM) {
                list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.speed") + ": " + EnumColor.GREY + (
                      Tier.TransporterTier.get(tier).speed / (100 / 20)) + " m/s");
                list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.pumpRate") + ": " + EnumColor.GREY
                      + Tier.TransporterTier.get(tier).pullAmount * 2 + "/s");
            } else if (transmission == TransmissionType.HEAT) {
                list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.conduction") + ": " + EnumColor.GREY
                      + Tier.ConductorTier.get(tier).inverseConduction);
                list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.insulation") + ": " + EnumColor.GREY
                      + Tier.ConductorTier.get(tier).inverseConductionInsulation);
                list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.heatCapacity") + ": " + EnumColor.GREY
                      + Tier.ConductorTier.get(tier).inverseHeatCapacity);
            }

            list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.AQUA + GameSettings
                  .getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) + EnumColor.GREY + " " + LangUtils
                  .localize("tooltip.forDetails"));
        } else {
            TransmitterType type = TransmitterType.values()[itemstack.getItemDamage()];

            switch (type) {
                case UNIVERSAL_CABLE: {
                    list.add(EnumColor.DARK_GREY + LangUtils.localize("tooltip.capableTrans") + ":");
                    list.add("- " + EnumColor.PURPLE + "RF " + EnumColor.GREY + "(ThermalExpansion)");
                    list.add("- " + EnumColor.PURPLE + "EU " + EnumColor.GREY + "(IndustrialCraft)");
                    list.add("- " + EnumColor.PURPLE + "Joules " + EnumColor.GREY + "(Mekanism)");
                    break;
                }
                case MECHANICAL_PIPE: {
                    list.add(EnumColor.DARK_GREY + LangUtils.localize("tooltip.capableTrans") + ":");
                    list.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.fluids") + " " + EnumColor.GREY
                          + "(MinecraftForge)");
                    break;
                }
                case PRESSURIZED_TUBE: {
                    list.add(EnumColor.DARK_GREY + LangUtils.localize("tooltip.capableTrans") + ":");
                    list.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.gasses") + " (Mekanism)");
                    break;
                }
                case LOGISTICAL_TRANSPORTER: {
                    list.add(EnumColor.DARK_GREY + LangUtils.localize("tooltip.capableTrans") + ":");
                    list.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.items") + " (" + LangUtils
                          .localize("tooltip.universal") + ")");
                    list.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.blocks") + " (" + LangUtils
                          .localize("tooltip.universal") + ")");
                    break;
                }
                case RESTRICTIVE_TRANSPORTER: {
                    list.add(EnumColor.DARK_GREY + LangUtils.localize("tooltip.capableTrans") + ":");
                    list.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.items") + " (" + LangUtils
                          .localize("tooltip.universal") + ")");
                    list.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.blocks") + " (" + LangUtils
                          .localize("tooltip.universal") + ")");
                    list.add("- " + EnumColor.DARK_RED + LangUtils.localize("tooltip.restrictiveDesc"));
                    break;
                }
                case DIVERSION_TRANSPORTER: {
                    list.add(EnumColor.DARK_GREY + LangUtils.localize("tooltip.capableTrans") + ":");
                    list.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.items") + " (" + LangUtils
                          .localize("tooltip.universal") + ")");
                    list.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.blocks") + " (" + LangUtils
                          .localize("tooltip.universal") + ")");
                    list.add("- " + EnumColor.DARK_RED + LangUtils.localize("tooltip.diversionDesc"));
                    break;
                }
                case THERMODYNAMIC_CONDUCTOR: {
                    list.add(EnumColor.DARK_GREY + LangUtils.localize("tooltip.capableTrans") + ":");
                    list.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.heat") + " (Mekanism)");
                    break;
                }
            }
        }
    }

    @Nonnull
    @Override
    public String getTranslationKey(ItemStack stack) {
        TransmitterType type = TransmitterType.get(stack.getItemDamage());
        String name = type.getTranslationKey();

        if (type.hasTiers()) {
            BaseTier tier = getBaseTier(stack);
            name = tier.getSimpleName() + name;
        }

        return getTranslationKey() + "." + name;
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
    @Optional.Method(modid = MekanismHooks.MCMULTIPART_MOD_ID)
    protected IMultipart getMultiPart() {
        return MultipartMekanism.TRANSMITTER_MP;
    }
}
