package mekanism.common.integration.lookingat.jade;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Optional;
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
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;

public class JadeTooltipRenderer implements IBlockComponentProvider, IEntityComponentProvider {

    static final JadeTooltipRenderer INSTANCE = new JadeTooltipRenderer();

    @Override
    public ResourceLocation getUid() {
        return JadeConstants.TOOLTIP_RENDERER;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        append(tooltip, accessor, config);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        append(tooltip, accessor, config);
    }

    private void append(ITooltip tooltip, Accessor<?> accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.contains(NBTConstants.MEK_DATA, Tag.TAG_LIST)) {
            Component lastText = null;
            //Copy the data we need and have from the server and pass it on to the tooltip rendering
            ListTag list = data.getList(NBTConstants.MEK_DATA, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag elementData = list.getCompound(i);
                LookingAtElement element;
                ResourceLocation name;
                if (elementData.contains(JadeLookingAtHelper.TEXT)) {
                    Optional<Component> text = ComponentSerialization.CODEC.parse(NbtOps.INSTANCE, elementData.get(JadeLookingAtHelper.TEXT)).result();
                    if (text.isPresent()) {
                        if (lastText != null) {
                            //Fallback to printing the last text
                            tooltip.add(lastText);
                        }
                        lastText = text.get();
                    }
                    continue;
                } else if (elementData.contains(NBTConstants.ENERGY_STORED, Tag.TAG_STRING)) {
                    element = new EnergyElement(FloatingLong.parseFloatingLong(elementData.getString(NBTConstants.ENERGY_STORED), true),
                          FloatingLong.parseFloatingLong(elementData.getString(NBTConstants.MAX), true));
                    name = LookingAtUtils.ENERGY;
                } else if (elementData.contains(NBTConstants.FLUID_STORED, Tag.TAG_COMPOUND)) {
                    //TODO - 1.20.5: Providers
                    //element = new FluidElement(FluidStack.loadFluidStackFromNBT(elementData.getCompound(NBTConstants.FLUID_STORED)), elementData.getInt(NBTConstants.MAX));
                    element = new FluidElement(FluidStack.EMPTY, elementData.getInt(NBTConstants.MAX));
                    name = LookingAtUtils.FLUID;
                } else if (elementData.contains(JadeLookingAtHelper.CHEMICAL_STACK, Tag.TAG_COMPOUND)) {
                    ChemicalStack<?> chemicalStack;
                    CompoundTag chemicalData = elementData.getCompound(JadeLookingAtHelper.CHEMICAL_STACK);
                    if (chemicalData.contains(NBTConstants.GAS_NAME, Tag.TAG_STRING)) {
                        chemicalStack = GasStack.readFromNBT(chemicalData);
                        name = LookingAtUtils.GAS;
                    } else if (chemicalData.contains(NBTConstants.INFUSE_TYPE_NAME, Tag.TAG_STRING)) {
                        chemicalStack = InfusionStack.readFromNBT(chemicalData);
                        name = LookingAtUtils.INFUSE_TYPE;
                    } else if (chemicalData.contains(NBTConstants.PIGMENT_NAME, Tag.TAG_STRING)) {
                        chemicalStack = PigmentStack.readFromNBT(chemicalData);
                        name = LookingAtUtils.PIGMENT;
                    } else if (chemicalData.contains(NBTConstants.SLURRY_NAME, Tag.TAG_STRING)) {
                        chemicalStack = SlurryStack.readFromNBT(chemicalData);
                        name = LookingAtUtils.SLURRY;
                    } else {//Unknown chemical
                        continue;
                    }
                    element = new ChemicalElement(chemicalStack, elementData.getLong(NBTConstants.MAX));
                } else {//Skip, unknown
                    continue;
                }
                if (config.get(name)) {
                    tooltip.add(new MekElement(lastText, element).tag(name));
                }
                lastText = null;
            }
            if (lastText != null) {
                tooltip.add(lastText);
            }
        }
    }

    private static class MekElement extends Element {

        @Nullable
        private final Component text;
        private final LookingAtElement element;

        public MekElement(@Nullable Component text, LookingAtElement element) {
            this.element = element;
            this.text = text;
        }

        @Override
        public Vec2 getSize() {
            int width = element.getWidth();
            int height = element.getHeight() + 2;
            if (text != null) {
                width = Math.max(width, 96);
                height += 14;
            }
            return new Vec2(width, height);
        }

        @Override
        public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
            if (text != null) {
                LookingAtElement.renderScaledText(Minecraft.getInstance(), guiGraphics, x + 4, y + 3, 0xFFFFFF, 92, text);
                y += 13;
            }
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(x, y, 0);
            element.render(guiGraphics, 0, 1);
            pose.popPose();
        }
    }
}