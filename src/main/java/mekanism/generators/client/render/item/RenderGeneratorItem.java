package mekanism.generators.client.render.item;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.SubTypeItemRenderer;
import mekanism.generators.common.block.states.BlockStateGenerator.GeneratorType;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGeneratorItem extends SubTypeItemRenderer<GeneratorType> {

    public static Map<GeneratorType, ItemLayerWrapper> modelMap = new HashMap<>();

    @Override
    protected boolean earlyExit() {
        return true;
    }

    @Override
    protected void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        GeneratorType generatorType = GeneratorType.get(stack);

        if (generatorType != null) {
            if (generatorType == GeneratorType.BIO_GENERATOR) {
                RenderBioGeneratorItem.renderStack(stack, transformType);
            } else if (generatorType == GeneratorType.ADVANCED_SOLAR_GENERATOR) {
                RenderAdvancedSolarGeneratorItem.renderStack(stack, transformType);
            } else if (generatorType == GeneratorType.SOLAR_GENERATOR) {
                RenderSolarGeneratorItem.renderStack(stack, transformType);
            } else if (generatorType == GeneratorType.HEAT_GENERATOR) {
                RenderHeatGeneratorItem.renderStack(stack, transformType);
            } else if (generatorType == GeneratorType.GAS_GENERATOR) {
                RenderGasGeneratorItem.renderStack(stack, transformType);
            } else if (generatorType == GeneratorType.WIND_GENERATOR) {
                RenderWindGeneratorItem.renderStack(stack, transformType);
            }
        }
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType) {

    }

    @Nullable
    @Override
    protected ItemLayerWrapper getModel(GeneratorType generatorType) {
        return modelMap.get(generatorType);
    }

    @Nullable
    @Override
    protected GeneratorType getType(@Nonnull ItemStack stack) {
        return GeneratorType.get(stack);
    }
}