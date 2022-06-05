package mekanism.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.BaseTier;
import mekanism.client.SpecialColors;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.model.baked.DigitalMinerBakedModel;
import mekanism.client.render.MekanismRenderer.Model3D.SpriteInfo;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.ValveRenderData;
import mekanism.client.render.item.block.RenderFluidTankItem;
import mekanism.client.render.lib.ColorAtlas;
import mekanism.client.render.lib.ColorAtlas.ColorRegistryObject;
import mekanism.client.render.tileentity.RenderDigitalMiner;
import mekanism.client.render.tileentity.RenderDimensionalStabilizer;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.client.render.tileentity.RenderNutritionalLiquifier;
import mekanism.client.render.tileentity.RenderPigmentMixer;
import mekanism.client.render.tileentity.RenderTeleporter;
import mekanism.client.render.transmitter.RenderLogisticalTransporter;
import mekanism.client.render.transmitter.RenderMechanicalPipe;
import mekanism.client.render.transmitter.RenderTransmitterBase;
import mekanism.common.Mekanism;
import mekanism.common.lib.Color;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MekanismRenderer {

    //TODO: Replace various usages of this with the getter for calculating glow light, at least if we end up making it only
    // effect block light for the glow rather than having it actually become full light
    public static final int FULL_LIGHT = 0xF000F0;
    public static final int FULL_SKY_LIGHT = LightTexture.pack(0, 15);

    public static TextureAtlasSprite energyIcon;
    public static TextureAtlasSprite heatIcon;
    public static TextureAtlasSprite whiteIcon;
    public static TextureAtlasSprite teleporterPortal;
    public static TextureAtlasSprite redstoneTorch;
    public static TextureAtlasSprite redstonePulse;
    public static final Map<TransmissionType, TextureAtlasSprite> overlays = new EnumMap<>(TransmissionType.class);

    /**
     * Get a fluid texture when a stack does not exist.
     *
     * @param fluid the fluid to get
     * @param type  Still or Flowing
     *
     * @return the sprite, or missing sprite if not found
     */
    public static TextureAtlasSprite getBaseFluidTexture(@Nonnull Fluid fluid, @Nonnull FluidType type) {
        ResourceLocation spriteLocation;
        if (type == FluidType.STILL) {
            spriteLocation = fluid.getAttributes().getStillTexture();
        } else {
            spriteLocation = fluid.getAttributes().getFlowingTexture();
        }

        return getSprite(spriteLocation);
    }

    public static TextureAtlasSprite getFluidTexture(@Nonnull FluidStack fluidStack, @Nonnull FluidType type) {
        Fluid fluid = fluidStack.getFluid();
        ResourceLocation spriteLocation;
        if (type == FluidType.STILL) {
            spriteLocation = fluid.getAttributes().getStillTexture(fluidStack);
        } else {
            spriteLocation = fluid.getAttributes().getFlowingTexture(fluidStack);
        }
        return getSprite(spriteLocation);
    }

    public static TextureAtlasSprite getChemicalTexture(@Nonnull Chemical<?> chemical) {
        return getSprite(chemical.getIcon());
    }

    public static TextureAtlasSprite getSprite(ResourceLocation spriteLocation) {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(spriteLocation);
    }

    public static void prepFlowing(Model3D model, @Nonnull FluidStack fluid) {
        SpriteInfo still = new SpriteInfo(getFluidTexture(fluid, FluidType.STILL), 16);
        SpriteInfo flowing = new SpriteInfo(getFluidTexture(fluid, FluidType.FLOWING), 8);
        model.setTextures(still, still, flowing, flowing, flowing, flowing);
    }

    public static void prepSingleFaceModelSize(Model3D model, Direction face) {
        switch (face) {
            case DOWN -> {
                model.minX = 0;
                model.maxX = 1;
                model.minY = -0.01F;
                model.maxY = -0.001F;
                model.minZ = 0;
                model.maxZ = 1;
            }
            case UP -> {
                model.minX = 0;
                model.maxX = 1;
                model.minY = 1.001F;
                model.maxY = 1.01F;
                model.minZ = 0;
                model.maxZ = 1;
            }
            case NORTH -> {
                model.minX = 0;
                model.maxX = 1;
                model.minY = 0;
                model.maxY = 1;
                model.minZ = -0.01F;
                model.maxZ = -0.001F;
            }
            case SOUTH -> {
                model.minX = 0;
                model.maxX = 1;
                model.minY = 0;
                model.maxY = 1;
                model.minZ = 1.001F;
                model.maxZ = 1.01F;
            }
            case WEST -> {
                model.minX = -0.01F;
                model.maxX = -0.001F;
                model.minY = 0;
                model.maxY = 1;
                model.minZ = 0;
                model.maxZ = 1;
            }
            case EAST -> {
                model.minX = 1.001F;
                model.maxX = 1.01F;
                model.minY = 0;
                model.maxY = 1;
                model.minZ = 0;
                model.maxZ = 1;
            }
        }
    }

    public static void renderObject(@Nullable Model3D object, @Nonnull PoseStack matrix, VertexConsumer buffer, int argb, int light, int overlay,
          FaceDisplay faceDisplay) {
        renderObject(object, matrix, buffer, argb, light, overlay, faceDisplay, true);
    }

    public static void renderObject(@Nullable Model3D object, @Nonnull PoseStack matrix, VertexConsumer buffer, int argb, int light, int overlay,
          FaceDisplay faceDisplay, boolean fakeDisableDiffuse) {
        if (object != null) {
            RenderResizableCuboid.renderCube(object, matrix, buffer, argb, light, overlay, faceDisplay, fakeDisableDiffuse);
        }
    }

    public static void renderObject(@Nullable Model3D object, @Nonnull PoseStack matrix, VertexConsumer buffer, int[] colors, int light, int overlay,
          FaceDisplay faceDisplay) {
        if (object != null) {
            RenderResizableCuboid.renderCube(object, matrix, buffer, colors, light, overlay, faceDisplay, true);
        }
    }

    public static void renderValves(PoseStack matrix, VertexConsumer buffer, Set<ValveData> valves, FluidRenderData data, BlockPos pos, int glow, int overlay,
          BooleanSupplier inMultiblock) {
        FaceDisplay faceDisplay;
        if (!valves.isEmpty()) {
            //If we are in the multiblock, render both faces of the valves as we may be "inside" of them or inside and outside them
            // if we aren't in the multiblock though we can just get away with only rendering the front faces
            faceDisplay = inMultiblock.getAsBoolean() ? FaceDisplay.BOTH : FaceDisplay.FRONT;
            for (ValveData valveData : valves) {
                matrix.pushPose();
                matrix.translate(valveData.location.getX() - pos.getX(), valveData.location.getY() - pos.getY(), valveData.location.getZ() - pos.getZ());
                renderObject(ModelRenderer.getValveModel(ValveRenderData.get(data, valveData)), matrix, buffer, data.getColorARGB(), glow, overlay, faceDisplay);
                matrix.popPose();
            }
        }
    }

    //Color
    public static void resetColor() {
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    public static float getRed(int color) {
        return (color >> 16 & 0xFF) / 255.0F;
    }

    public static float getGreen(int color) {
        return (color >> 8 & 0xFF) / 255.0F;
    }

    public static float getBlue(int color) {
        return (color & 0xFF) / 255.0F;
    }

    public static float getAlpha(int color) {
        return (color >> 24 & 0xFF) / 255.0F;
    }

    public static void color(int color) {
        RenderSystem.setShaderColor(getRed(color), getGreen(color), getBlue(color), getAlpha(color));
    }

    public static void color(ColorRegistryObject colorRO) {
        color(colorRO.get());
    }

    public static void color(Color color) {
        RenderSystem.setShaderColor(color.rf(), color.gf(), color.bf(), color.af());
    }

    public static void color(@Nonnull FluidStack fluid) {
        if (!fluid.isEmpty()) {
            color(fluid.getFluid().getAttributes().getColor(fluid));
        }
    }

    public static void color(@Nonnull ChemicalStack<?> chemicalStack) {
        if (!chemicalStack.isEmpty()) {
            color(chemicalStack.getType());
        }
    }

    public static void color(@Nonnull Chemical<?> chemical) {
        if (!chemical.isEmptyType()) {
            int color = chemical.getTint();
            RenderSystem.setShaderColor(getRed(color), getGreen(color), getBlue(color), 1);
        }
    }

    public static void color(@Nonnull BaseTier tier) {
        color(tier.getColor());
    }

    public static void color(@Nullable EnumColor color) {
        color(color, 1.0F);
    }

    public static void color(@Nullable EnumColor color, float alpha) {
        if (color != null) {
            RenderSystem.setShaderColor(color.getColor(0), color.getColor(1), color.getColor(2), alpha);
        }
    }

    public static int getColorARGB(EnumColor color, float alpha) {
        return getColorARGB(color.getRgbCode()[0], color.getRgbCode()[1], color.getRgbCode()[2], alpha);
    }

    public static int getColorARGB(@Nonnull FluidStack fluidStack) {
        return fluidStack.getFluid().getAttributes().getColor(fluidStack);
    }

    public static int getColorARGB(@Nonnull FluidStack fluidStack, float fluidScale) {
        if (fluidStack.isEmpty()) {
            return -1;
        }
        int color = getColorARGB(fluidStack);
        if (fluidStack.getFluid().getAttributes().isGaseous(fluidStack)) {
            //TODO: We probably want to factor in the fluid's alpha value somehow
            return getColorARGB(getRed(color), getGreen(color), getBlue(color), Math.min(1, fluidScale + 0.2F));
        }
        return color;
    }

    public static int getColorARGB(@Nonnull ChemicalStack<?> stack, float scale, boolean gaseous) {
        if (stack.isEmpty()) {
            return -1;
        }
        int color = stack.getChemicalTint();
        return getColorARGB(getRed(color), getGreen(color), getBlue(color), gaseous ? Math.min(1, scale + 0.2F) : 1);
    }

    public static int getColorARGB(float red, float green, float blue, float alpha) {
        return getColorARGB((int) (255 * red), (int) (255 * green), (int) (255 * blue), alpha);
    }

    public static int getColorARGB(int red, int green, int blue, float alpha) {
        if (alpha < 0) {
            alpha = 0;
        } else if (alpha > 1) {
            alpha = 1;
        }
        int argb = (int) (255 * alpha) << 24;
        argb |= red << 16;
        argb |= green << 8;
        argb |= blue;
        return argb;
    }

    public static int calculateGlowLight(int combinedLight, @Nonnull FluidStack fluid) {
        return fluid.isEmpty() ? combinedLight : calculateGlowLight(combinedLight, fluid.getFluid().getAttributes().getLuminosity(fluid));
    }

    public static int calculateGlowLight(int combinedLight, int glow) {
        //Only factor the glow into the block light portion
        return (combinedLight & 0xFFFF0000) | Math.max(Math.min(glow, 15) << 4, combinedLight & 0xFFFF);
    }

    public static void renderColorOverlay(PoseStack matrix, int x, int y, int width, int height, int color) {
        float r = getRed(color);
        float g = getGreen(color);
        float b = getBlue(color);
        float a = getAlpha(color);
        RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix4f = matrix.last().pose();
        bufferbuilder.vertex(matrix4f, width, y, 0).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(matrix4f, x, y, 0).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(matrix4f, x, height, 0).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(matrix4f, width, height, 0).color(r, g, b, a).endVertex();
        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }

    public static float getPartialTick() {
        return Minecraft.getInstance().getFrameTime();
    }

    public static void rotate(PoseStack matrix, Direction facing, float north, float south, float west, float east) {
        switch (facing) {
            case NORTH -> matrix.mulPose(Vector3f.YP.rotationDegrees(north));
            case SOUTH -> matrix.mulPose(Vector3f.YP.rotationDegrees(south));
            case WEST -> matrix.mulPose(Vector3f.YP.rotationDegrees(west));
            case EAST -> matrix.mulPose(Vector3f.YP.rotationDegrees(east));
        }
    }

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Pre event) {
        if (!event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            return;
        }
        for (TransmissionType type : EnumUtils.TRANSMISSION_TYPES) {
            event.addSprite(Mekanism.rl("block/overlay/" + type.getTransmission() + "_overlay"));
        }

        event.addSprite(Mekanism.rl("block/overlay/overlay_white"));
        event.addSprite(Mekanism.rl("liquid/energy"));
        event.addSprite(Mekanism.rl("liquid/heat"));
        event.addSprite(Mekanism.rl("icon/redstone_control_pulse"));
        event.addSprite(Mekanism.rl("block/teleporter_portal"));

        //MekaSuit
        event.addSprite(Mekanism.rl("entity/armor/blank"));
        event.addSprite(Mekanism.rl("entity/armor/mekasuit_player"));
        event.addSprite(Mekanism.rl("entity/armor/mekasuit_armor_body"));
        event.addSprite(Mekanism.rl("entity/armor/mekasuit_armor_helmet"));
        event.addSprite(Mekanism.rl("entity/armor/mekasuit_armor_exoskeleton"));
        event.addSprite(Mekanism.rl("entity/armor/mekasuit_gravitational_modulator"));
        event.addSprite(Mekanism.rl("entity/armor/mekasuit_elytra"));
        event.addSprite(Mekanism.rl("entity/armor/mekasuit_armor_modules"));
        event.addSprite(Mekanism.rl("entity/armor/mekatool"));

        DigitalMinerBakedModel.preStitch(event);

        addChemicalSprites(event, MekanismAPI.gasRegistry());
        addChemicalSprites(event, MekanismAPI.infuseTypeRegistry());
        addChemicalSprites(event, MekanismAPI.pigmentRegistry());
        addChemicalSprites(event, MekanismAPI.slurryRegistry());

        ModelRenderer.resetCachedModels();
        RenderDigitalMiner.resetCachedVisuals();
        RenderDimensionalStabilizer.resetCachedVisuals();
        RenderFluidTank.resetCachedModels();
        RenderFluidTankItem.resetCachedModels();
        RenderNutritionalLiquifier.resetCachedModels();
        RenderPigmentMixer.resetCached();
        RenderMechanicalPipe.onStitch();
        RenderTickHandler.resetCached();
        RenderTeleporter.resetCachedModels();

        parseColorAtlas(Mekanism.rl("textures/colormap/primary.png"));
        SpecialColors.GUI_OBJECTS.parse(Mekanism.rl("textures/colormap/gui_objects.png"));
        SpecialColors.GUI_TEXT.parse(Mekanism.rl("textures/colormap/gui_text.png"));
        GuiElementHolder.updateBackgroundColor();
    }

    private static void parseColorAtlas(ResourceLocation rl) {
        List<Color> parsed = ColorAtlas.load(rl, EnumUtils.COLORS.length);
        if (parsed.size() < EnumUtils.COLORS.length) {
            Mekanism.logger.error("Failed to parse primary color atlas.");
            return;
        }
        for (int i = 0; i < EnumUtils.COLORS.length; i++) {
            EnumUtils.COLORS[i].setColorFromAtlas(parsed.get(i).rgbArray());
        }
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>> void addChemicalSprites(TextureStitchEvent.Pre event, IForgeRegistry<CHEMICAL> chemicalRegistry) {
        for (Chemical<?> chemical : chemicalRegistry.getValues()) {
            event.addSprite(chemical.getIcon());
        }
    }

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Post event) {
        TextureAtlas map = event.getAtlas();
        if (!map.location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            return;
        }
        for (TransmissionType type : EnumUtils.TRANSMISSION_TYPES) {
            overlays.put(type, map.getSprite(Mekanism.rl("block/overlay/" + type.getTransmission() + "_overlay")));
        }

        whiteIcon = map.getSprite(Mekanism.rl("block/overlay/overlay_white"));
        energyIcon = map.getSprite(Mekanism.rl("liquid/energy"));
        heatIcon = map.getSprite(Mekanism.rl("liquid/heat"));
        redstoneTorch = map.getSprite(new ResourceLocation("minecraft:block/redstone_torch"));
        redstonePulse = map.getSprite(Mekanism.rl("icon/redstone_control_pulse"));
        teleporterPortal = map.getSprite(Mekanism.rl("block/teleporter_portal"));

        DigitalMinerBakedModel.onStitch(event);

        //Note: These are called in post rather than pre to make sure the icons have properly been stitched/attached
        RenderLogisticalTransporter.onStitch(map);
        RenderTransmitterBase.onStitch();
    }

    public enum FluidType {
        STILL,
        FLOWING
    }

    public static class Model3D {

        public float minX, minY, minZ;
        public float maxX, maxY, maxZ;

        private final SpriteInfo[] textures = new SpriteInfo[6];
        private final boolean[] renderSides = {true, true, true, true, true, true};

        public void setSideRender(Direction side, boolean value) {
            renderSides[side.ordinal()] = value;
        }

        public Model3D copy() {
            Model3D copy = new Model3D();
            System.arraycopy(textures, 0, copy.textures, 0, textures.length);
            System.arraycopy(renderSides, 0, copy.renderSides, 0, renderSides.length);
            copy.minX = minX;
            copy.minY = minY;
            copy.minZ = minZ;
            copy.maxX = maxX;
            copy.maxY = maxY;
            copy.maxZ = maxZ;
            return copy;
        }

        @Nullable
        public SpriteInfo getSpriteToRender(Direction side) {
            int ordinal = side.ordinal();
            if (renderSides[ordinal]) {
                return textures[ordinal];
            }
            return null;
        }

        public void setTexture(Direction side, SpriteInfo spriteInfo) {
            textures[side.ordinal()] = spriteInfo;
        }

        public void setTexture(TextureAtlasSprite tex) {
            setTexture(tex, 16);
        }

        public void setTexture(TextureAtlasSprite tex, int size) {
            Arrays.fill(textures, new SpriteInfo(tex, size));
        }

        public void setTextures(SpriteInfo down, SpriteInfo up, SpriteInfo north, SpriteInfo south, SpriteInfo west, SpriteInfo east) {
            textures[0] = down;
            textures[1] = up;
            textures[2] = north;
            textures[3] = south;
            textures[4] = west;
            textures[5] = east;
        }

        public record SpriteInfo(TextureAtlasSprite sprite, int size) {
        }
    }
}