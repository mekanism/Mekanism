package mekanism.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.BaseTier;
import mekanism.client.model.baked.DriveArrayBakedModel;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.ValveRenderData;
import mekanism.client.render.item.block.RenderFluidTankItem;
import mekanism.client.render.obj.TransmitterLoader;
import mekanism.client.render.tileentity.RenderFluidTank;
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
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.ModelSettings;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MekanismRenderer {

    //TODO: Replace various usages of this with the getter for calculating glow light, at least if we end up making it only
    // effect block light for the glow rather than having it actually become full light
    public static final int FULL_LIGHT = 0xF000F0;

    public static OBJModel contentsModel;
    public static TextureAtlasSprite energyIcon;
    public static TextureAtlasSprite heatIcon;
    public static TextureAtlasSprite whiteIcon;
    public static final Map<TransmissionType, TextureAtlasSprite> overlays = new EnumMap<>(TransmissionType.class);

    //We ignore the warning, due to this actually being able to be null during runData
    @SuppressWarnings("ConstantConditions")
    public static void registerModelLoader() {
        if (Minecraft.getInstance() != null) {
            ModelLoaderRegistry.registerLoader(Mekanism.rl("transmitter"), TransmitterLoader.INSTANCE);
        }
    }

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
        return Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(spriteLocation);
    }

    public static void prepFlowing(Model3D model, @Nonnull FluidStack fluid) {
        TextureAtlasSprite still = getFluidTexture(fluid, FluidType.STILL);
        TextureAtlasSprite flowing = getFluidTexture(fluid, FluidType.FLOWING);
        model.setTextures(still, still, flowing, flowing, flowing, flowing);
    }

    public static void renderObject(@Nullable Model3D object, @Nonnull MatrixStack matrix, IVertexBuilder buffer, int argb, int light) {
        if (object != null) {
            RenderResizableCuboid.INSTANCE.renderCube(object, matrix, buffer, argb, light);
        }
    }

    public static void renderValves(MatrixStack matrix, IVertexBuilder buffer, Set<ValveData> valves, FluidRenderData data, BlockPos pos, int glow) {
        for (ValveData valveData : valves) {
            matrix.push();
            matrix.translate(valveData.location.getX() - pos.getX(), valveData.location.getY() - pos.getY(), valveData.location.getZ() - pos.getZ());
            MekanismRenderer.renderObject(ModelRenderer.getValveModel(ValveRenderData.get(data, valveData)), matrix, buffer, data.getColorARGB(), glow);
            matrix.pop();
        }
    }

    public static void bindTexture(ResourceLocation texture) {
        Minecraft.getInstance().textureManager.bindTexture(texture);
    }

    //Color
    public static void resetColor() {
        RenderSystem.color4f(1, 1, 1, 1);
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
        RenderSystem.color4f(getRed(color), getGreen(color), getBlue(color), getAlpha(color));
    }

    public static void color(Color color) {
        RenderSystem.color4f(color.rf(), color.gf(), color.bf(), color.af());
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
            RenderSystem.color3f(getRed(color), getGreen(color), getBlue(color));
        }
    }

    public static void color(@Nonnull BaseTier tier) {
        color(tier.getColor());
    }

    public static void color(@Nullable EnumColor color) {
        color(color, 1.0F);
    }

    public static void color(@Nullable EnumColor color, float alpha) {
        color(color, alpha, 1.0F);
    }

    public static void color(@Nullable EnumColor color, float alpha, float multiplier) {
        if (color != null) {
            RenderSystem.color4f(color.getColor(0) * multiplier, color.getColor(1) * multiplier, color.getColor(2) * multiplier, alpha);
        }
    }

    public static int getColorARGB(EnumColor color, float alpha) {
        return getColorARGB(color.rgbCode[0], color.rgbCode[1], color.rgbCode[2], alpha);
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

    public static int calculateGlowLight(int light, @Nonnull FluidStack fluid) {
        return fluid.isEmpty() ? light : calculateGlowLight(light, fluid.getFluid().getAttributes().getLuminosity(fluid));
    }

    public static int calculateGlowLight(int light, int glow) {
        if (glow >= 15) {
            return MekanismRenderer.FULL_LIGHT;
        }
        int blockLight = LightTexture.getLightBlock(light);
        int skyLight = LightTexture.getLightSky(light);
        return LightTexture.packLight(Math.max(blockLight, glow), Math.max(skyLight, glow));
    }

    public static void renderColorOverlay(int x, int y, int width, int height, int color) {
        float r = (color >> 24 & 255) / 255.0F;
        float g = (color >> 16 & 255) / 255.0F;
        float b = (color >> 8 & 255) / 255.0F;
        float a = (color & 255) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(width, y, 0).color(r, g, b, a).endVertex();
        bufferbuilder.pos(x, y, 0).color(r, g, b, a).endVertex();
        bufferbuilder.pos(x, height, 0).color(r, g, b, a).endVertex();
        bufferbuilder.pos(width, height, 0).color(r, g, b, a).endVertex();
        tessellator.draw();
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    public static float getPartialTick() {
        return Minecraft.getInstance().getRenderPartialTicks();
    }

    @Deprecated
    public static void rotate(Direction facing, float north, float south, float west, float east) {
        switch (facing) {
            case NORTH:
                RenderSystem.rotatef(north, 0, 1, 0);
                break;
            case SOUTH:
                RenderSystem.rotatef(south, 0, 1, 0);
                break;
            case WEST:
                RenderSystem.rotatef(west, 0, 1, 0);
                break;
            case EAST:
                RenderSystem.rotatef(east, 0, 1, 0);
                break;
        }
    }

    public static void rotate(MatrixStack matrix, Direction facing, float north, float south, float west, float east) {
        switch (facing) {
            case NORTH:
                matrix.rotate(Vector3f.YP.rotationDegrees(north));
                break;
            case SOUTH:
                matrix.rotate(Vector3f.YP.rotationDegrees(south));
                break;
            case WEST:
                matrix.rotate(Vector3f.YP.rotationDegrees(west));
                break;
            case EAST:
                matrix.rotate(Vector3f.YP.rotationDegrees(east));
                break;
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        try {
            contentsModel = OBJLoader.INSTANCE.loadModel(new ModelSettings(RenderTransmitterBase.MODEL_LOCATION, true, false, true, true, null));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Pre event) {
        if (!event.getMap().getTextureLocation().equals(AtlasTexture.LOCATION_BLOCKS_TEXTURE)) {
            return;
        }
        for (TransmissionType type : EnumUtils.TRANSMISSION_TYPES) {
            event.addSprite(Mekanism.rl("block/overlay/" + type.getTransmission() + "_overlay"));
        }

        event.addSprite(Mekanism.rl("block/overlay/overlay_white"));
        event.addSprite(Mekanism.rl("liquid/energy"));
        event.addSprite(Mekanism.rl("liquid/heat"));

        //MekaSuit
        event.addSprite(Mekanism.rl("entity/armor/blank"));
        event.addSprite(Mekanism.rl("entity/armor/mekasuit_player"));
        event.addSprite(Mekanism.rl("entity/armor/mekasuit_armor_body"));
        event.addSprite(Mekanism.rl("entity/armor/mekasuit_armor_helmet"));
        event.addSprite(Mekanism.rl("entity/armor/mekasuit_armor_exoskeleton"));
        event.addSprite(Mekanism.rl("entity/armor/mekasuit_gravitational_modulator"));
        event.addSprite(Mekanism.rl("entity/armor/mekasuit_armor_modules"));
        event.addSprite(Mekanism.rl("entity/armor/mekatool"));

        addChemicalSprites(event, MekanismAPI.gasRegistry());
        addChemicalSprites(event, MekanismAPI.infuseTypeRegistry());
        addChemicalSprites(event, MekanismAPI.pigmentRegistry());
        addChemicalSprites(event, MekanismAPI.slurryRegistry());

        ModelRenderer.resetCachedModels();
        RenderFluidTank.resetCachedModels();
        RenderFluidTankItem.resetCachedModels();
        RenderMechanicalPipe.onStitch();
        RenderTickHandler.resetCachedOverlays();
        RenderTeleporter.resetCachedModels();

        DriveArrayBakedModel.preStitch(event);
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>> void addChemicalSprites(TextureStitchEvent.Pre event, IForgeRegistry<CHEMICAL> chemicalRegistry) {
        for (Chemical<?> chemical : chemicalRegistry.getValues()) {
            event.addSprite(chemical.getIcon());
        }
    }

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Post event) {
        AtlasTexture map = event.getMap();
        if (!map.getTextureLocation().equals(AtlasTexture.LOCATION_BLOCKS_TEXTURE)) {
            return;
        }
        for (TransmissionType type : EnumUtils.TRANSMISSION_TYPES) {
            overlays.put(type, map.getSprite(Mekanism.rl("block/overlay/" + type.getTransmission() + "_overlay")));
        }

        whiteIcon = map.getSprite(Mekanism.rl("block/overlay/overlay_white"));
        energyIcon = map.getSprite(Mekanism.rl("liquid/energy"));
        heatIcon = map.getSprite(Mekanism.rl("liquid/heat"));

        //Note: These are called in post rather than pre to make sure the icons have properly been stitched/attached
        RenderLogisticalTransporter.onStitch(map);
        RenderTransmitterBase.onStitch();
        DriveArrayBakedModel.onStitch(map);
    }

    public enum FluidType {
        STILL,
        FLOWING
    }

    public static class Model3D {

        public double minX, minY, minZ;
        public double maxX, maxY, maxZ;

        public final TextureAtlasSprite[] textures = new TextureAtlasSprite[6];

        public final boolean[] renderSides = new boolean[]{true, true, true, true, true, true, false};

        public double sizeX() {
            return maxX - minX;
        }

        public double sizeY() {
            return maxY - minY;
        }

        public double sizeZ() {
            return maxZ - minZ;
        }

        public void setSideRender(Direction side, boolean value) {
            renderSides[side.ordinal()] = value;
        }

        public boolean shouldSideRender(Direction side) {
            return renderSides[side.ordinal()];
        }

        public void setTexture(TextureAtlasSprite tex) {
            Arrays.fill(textures, tex);
        }

        public void setTextures(TextureAtlasSprite down, TextureAtlasSprite up, TextureAtlasSprite north, TextureAtlasSprite south, TextureAtlasSprite west, TextureAtlasSprite east) {
            textures[0] = down;
            textures[1] = up;
            textures[2] = north;
            textures[3] = south;
            textures[4] = west;
            textures[5] = east;
        }
    }
}