package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.ChemicalResource;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.MultiblockContentsRenderState;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.tileentity.MultiblockTileEntityRenderer;
import mekanism.common.util.WorldUtils;
import mekanism.generators.client.render.RenderIndustrialTurbine.TurbineRenderState;
import mekanism.generators.client.render.RenderTurbineRotor.TurbineRotorRenderState;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderIndustrialTurbine extends MultiblockTileEntityRenderer<TurbineMultiblockData, TileEntityTurbineCasing, TurbineRenderState> {

    public RenderIndustrialTurbine(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public TurbineRenderState createRenderState() {
        return new TurbineRenderState();
    }

    @Override
    public void extractRenderState(TileEntityTurbineCasing turbine, TurbineMultiblockData multiblock, TurbineRenderState state, float partialTick, Vec3 cameraPosition,
          ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        state.height = multiblock.lowerVolume / ((state.length + 2) * (state.width + 2));
        Level level = turbine.getLevel();
        //Update the light coords based on the multiblock so that we can pass the proper value to the rotor
        state.calculateLightCoords(level, multiblock);
        if (RenderTurbineRotor.INSTANCE != null && multiblock.complex != null) {
            boolean tickingNormally = isTickingNormally(turbine);
            BlockPos.MutableBlockPos complexPos = multiblock.complex.mutable();
            while (true) {
                complexPos.move(Direction.DOWN);
                TileEntityTurbineRotor rotor = WorldUtils.getTileEntity(TileEntityTurbineRotor.class, level, complexPos);
                if (rotor == null) {
                    break;
                }
                if (rotor.getHousedBlades() > 0) {
                    if (tickingNormally) {
                        int baseIndex = rotor.getPosition() * 2;
                        float rotateSpeed = multiblock.clientRotation * RenderTurbineRotor.BASE_SPEED;
                        rotor.rotationLower += rotateSpeed / (baseIndex + 1);
                        rotor.rotationUpper += rotateSpeed / (baseIndex + 2);
                        rotor.rotationLower %= 360;
                        rotor.rotationUpper %= 360;
                    }

                    TurbineRotorRenderState rotorState = RenderTurbineRotor.INSTANCE.createRenderState();
                    RenderTurbineRotor.INSTANCE.extractRenderState(rotor, rotorState, partialTick, cameraPosition, null);
                    //Use the multiblock's light level
                    rotorState.lightCoords = state.lightCoords;
                    state.rotors.add(rotorState);
                }
            }
        }
        if (state.height > 0 && !multiblock.chemicalTank.isEmpty()) {
            ChemicalResource steam = multiblock.chemicalTank.resource();
            state.steamTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getChemicalTexture(steam));
            state.steamMaxY = ModelRenderer.getMaxY(state.height, multiblock.prevSteamScale, steam.is(MekanismAPITags.Chemicals.GASEOUS));
            state.steamColor = MekanismRenderer.getColorARGB(steam, multiblock.prevSteamScale);
            //We already calculated the multiblock's light coords, so update it the steam light coords here
            state.steamLightCoords = LightCoordsUtil.lightCoordsWithEmission(state.lightCoords, steam.value().lightLevel());
        }
    }

    @Override
    public void submit(TurbineRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (RenderTurbineRotor.INSTANCE != null && !state.rotors.isEmpty()) {
            BlockPos pos = state.blockPos;
            for (TurbineRotorRenderState rotor : state.rotors) {
                poseStack.pushPose();
                poseStack.translate(rotor.blockPos.getX() - pos.getX(), rotor.blockPos.getY() - pos.getY(), rotor.blockPos.getZ() - pos.getZ());
                RenderTurbineRotor.INSTANCE.submit(rotor, poseStack, nodeCollector, camera);
                poseStack.popPose();
            }
        }
        if (state.steamTexture != null) {
            RenderResizableCuboid.renderObject(camera.pos, poseStack, Sheets.translucentBlockItemSheet(), nodeCollector, RenderResizableCuboid.SideRender.ALL_FACES,
                  0.01F, 0.01F, 0.01F, state.length - 0.02F, state.steamMaxY, state.width - 0.02F, state.steamTexture,
                  OverlayTexture.NO_OVERLAY, state.steamLightCoords, state.steamColor, state.blockPos, state.renderLocation, state.length, state.width);
        }
    }

    public static class TurbineRenderState extends MultiblockContentsRenderState {
        public final List<TurbineRotorRenderState> rotors = new ArrayList<>();
        public RenderResizableCuboid.@Nullable TexturePicker steamTexture;
        public float steamMaxY;
        public int steamColor;
        public int steamLightCoords;
    }
}