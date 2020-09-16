package mekanism.common.integration.lookingat.hwyla;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.awt.Dimension;
import java.util.List;
import mcp.mobius.waila.api.ICommonAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltipRenderer;
import mcp.mobius.waila.api.RenderableTextComponent;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.lookingat.ChemicalElement;
import mekanism.common.integration.lookingat.EnergyElement;
import mekanism.common.integration.lookingat.FluidElement;
import mekanism.common.integration.lookingat.LookingAtElement;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;

public class HwylaTooltipRenderer implements IComponentProvider, ITooltipRenderer {

    static final HwylaTooltipRenderer INSTANCE = new HwylaTooltipRenderer();

    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        CompoundNBT data = accessor.getServerData();
        if (data.contains(NBTConstants.MEK_DATA, NBT.TAG_LIST)) {
            //Copy the data we need and have from the server and pass it on to the tooltip rendering
            ListNBT list = data.getList(NBTConstants.MEK_DATA, NBT.TAG_COMPOUND);
            if (!list.isEmpty()) {
                CompoundNBT mekData = new CompoundNBT();
                mekData.put(NBTConstants.MEK_DATA, list);
                tooltip.add(new RenderableTextComponent(MekanismHwylaPlugin.HWLYA_TOOLTIP, mekData));
            }
        }
    }

    @Override
    public Dimension getSize(CompoundNBT data, ICommonAccessor accessor) {
        ListNBT list = data.getList(NBTConstants.MEK_DATA, NBT.TAG_COMPOUND);
        return new Dimension(102, 15 * list.size());
    }

    @Override
    public void draw(CompoundNBT data, ICommonAccessor accessor, int x, int y) {
        ListNBT list = data.getList(NBTConstants.MEK_DATA, NBT.TAG_COMPOUND);
        MatrixStack matrix = new MatrixStack();
        int currentX = x + 1;
        int currentY = y + 1;
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT elementData = list.getCompound(i);
            LookingAtElement element;
            if (elementData.contains(MekanismHwylaPlugin.TEXT, NBT.TAG_STRING)) {
                ITextComponent text = ITextComponent.Serializer.getComponentFromJson(elementData.getString(MekanismHwylaPlugin.TEXT));
                if (text != null) {
                    LookingAtElement.renderScaledText(Minecraft.getInstance(), matrix, currentX + 4, currentY + 3, 0xFFFFFF, 92, text);
                    currentY += 15;
                }
                continue;
            } else if (elementData.contains(NBTConstants.ENERGY_STORED, NBT.TAG_STRING)) {
                element = new EnergyElement(FloatingLong.parseFloatingLong(elementData.getString(NBTConstants.ENERGY_STORED), true),
                      FloatingLong.parseFloatingLong(elementData.getString(NBTConstants.MAX), true));
            } else if (elementData.contains(NBTConstants.FLUID_STORED, NBT.TAG_COMPOUND)) {
                element = new FluidElement(FluidStack.loadFluidStackFromNBT(elementData.getCompound(NBTConstants.FLUID_STORED)), elementData.getInt(NBTConstants.MAX));
            } else if (elementData.contains(MekanismHwylaPlugin.CHEMICAL_STACK, NBT.TAG_COMPOUND)) {
                ChemicalStack<?> chemicalStack;
                CompoundNBT chemicalData = elementData.getCompound(MekanismHwylaPlugin.CHEMICAL_STACK);
                if (chemicalData.contains(NBTConstants.GAS_NAME, NBT.TAG_STRING)) {
                    chemicalStack = GasStack.readFromNBT(chemicalData);
                } else if (chemicalData.contains(NBTConstants.INFUSE_TYPE_NAME, NBT.TAG_STRING)) {
                    chemicalStack = InfusionStack.readFromNBT(chemicalData);
                } else if (chemicalData.contains(NBTConstants.PIGMENT_NAME, NBT.TAG_STRING)) {
                    chemicalStack = PigmentStack.readFromNBT(chemicalData);
                } else if (chemicalData.contains(NBTConstants.SLURRY_NAME, NBT.TAG_STRING)) {
                    chemicalStack = SlurryStack.readFromNBT(chemicalData);
                } else {
                    //Unknown chemical
                    continue;
                }
                element = new ChemicalElement(chemicalStack, elementData.getLong(NBTConstants.MAX));
            } else {
                //Skip
                continue;
            }
            element.render(matrix, currentX, currentY);
            currentY += 15;
        }
    }
}