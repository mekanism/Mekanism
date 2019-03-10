package mekanism.client.render.item;

import com.google.common.collect.ImmutableMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import mekanism.api.energy.IEnergizedItem;
import mekanism.client.model.ModelArmoredJetpack;
import mekanism.client.model.ModelAtomicDisassembler;
import mekanism.client.model.ModelChemicalCrystallizer;
import mekanism.client.model.ModelChemicalDissolutionChamber;
import mekanism.client.model.ModelDigitalMiner;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.model.ModelFlamethrower;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.model.ModelGasMask;
import mekanism.client.model.ModelJetpack;
import mekanism.client.model.ModelQuantumEntangloporter;
import mekanism.client.model.ModelResistiveHeater;
import mekanism.client.model.ModelScubaTank;
import mekanism.client.model.ModelSecurityDesk;
import mekanism.client.model.ModelSeismicVibrator;
import mekanism.client.model.ModelSolarNeutronActivator;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.common.MekanismItems;
import mekanism.common.SideData.IOState;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.Tier.FluidTankTier;
import mekanism.common.base.ITierItem;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.item.ItemAtomicDisassembler;
import mekanism.common.item.ItemBlockEnergyCube;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.item.ItemFreeRunners;
import mekanism.common.item.ItemGasMask;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fluids.Fluid;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

public class BakedCustomItemModel implements IBakedModel {

    public static ModelJetpack jetpack = new ModelJetpack();
    public static ModelArmoredJetpack armoredJetpack = new ModelArmoredJetpack();
    public static ModelGasMask gasMask = new ModelGasMask();
    public static ModelScubaTank scubaTank = new ModelScubaTank();
    public static ModelFreeRunners freeRunners = new ModelFreeRunners();
    public static ModelAtomicDisassembler atomicDisassembler = new ModelAtomicDisassembler();
    public static ModelFlamethrower flamethrower = new ModelFlamethrower();
    public static ModelChest personalChest = new ModelChest();
    public static ModelSolarNeutronActivator solarNeutronActivator = new ModelSolarNeutronActivator();
    public static ModelSeismicVibrator seismicVibrator = new ModelSeismicVibrator();
    public static ModelChemicalDissolutionChamber chemicalDissolutionChamber = new ModelChemicalDissolutionChamber();
    public static ModelChemicalCrystallizer chemicalCrystallizer = new ModelChemicalCrystallizer();
    public static ModelSecurityDesk securityDesk = new ModelSecurityDesk();
    public static ModelResistiveHeater resistiveHeater = new ModelResistiveHeater();
    public static ModelQuantumEntangloporter quantumEntangloporter = new ModelQuantumEntangloporter();
    public static ModelEnergyCube energyCube = new ModelEnergyCube();
    public static ModelDigitalMiner digitalMiner = new ModelDigitalMiner();
    // Copy from old CTM
    public static Map<TransformType, TRSRTransformation> transforms = ImmutableMap.<TransformType, TRSRTransformation>builder()
          .put(TransformType.GUI, get(0, 0, 0, 30, 225, 0, 0.625f))
          .put(TransformType.THIRD_PERSON_RIGHT_HAND, get(0, 2.5f, 0, 75, 45, 0, 0.375f))
          .put(TransformType.THIRD_PERSON_LEFT_HAND, get(0, 2.5f, 0, 75, 45, 0, 0.375f))
          .put(TransformType.FIRST_PERSON_RIGHT_HAND, get(0, 0, 0, 0, 45, 0, 0.4f))
          .put(TransformType.FIRST_PERSON_LEFT_HAND, get(0, 0, 0, 0, 225, 0, 0.4f))
          .put(TransformType.GROUND, get(0, 2, 0, 0, 0, 0, 0.25f))
          .put(TransformType.HEAD, get(0, 0, 0, 0, 0, 0, 1))
          .put(TransformType.FIXED, get(0, 0, 0, 0, 0, 0, 1))
          .put(TransformType.NONE, get(0, 0, 0, 0, 0, 0, 0))
          .build();
    private IBakedModel baseModel;
    private ItemStack stack;
    private TransformType prevTransform;
    private Minecraft mc = Minecraft.getMinecraft();

    public BakedCustomItemModel(IBakedModel model, ItemStack s) {
        baseModel = model;
        stack = s;
    }

    private static TRSRTransformation get(float tx, float ty, float tz, float ax, float ay, float az, float s) {
        return new TRSRTransformation(
              new Vector3f(tx / 16, ty / 16, tz / 16),
              TRSRTransformation.quatFromXYZDegrees(new Vector3f(ax, ay, az)),
              new Vector3f(s, s, s),
              null);
    }

    private void doRender(TransformType type) {
        BasicBlockType basicType = BasicBlockType.get(stack);

        if (type == TransformType.GUI) {
            GlStateManager.rotate(180F, 0.0F, 1.0F, 0.0F);
        }

        if (basicType != null) {
            if (basicType == BasicBlockType.BIN) {
				/*GlStateManager.pushMatrix();
				ItemBlockBasic itemBasic = (ItemBlockBasic)stack.getItem();
				InventoryBin inv = new InventoryBin(stack);
				binRenderer.render(EnumFacing.NORTH, inv.getItemType(), inv.getItemCount(), false, -0.5, -0.5, -0.5);
				Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				GlStateManager.enableRescaleNormal();
		        GlStateManager.enableAlpha();
		        GlStateManager.alphaFunc(516, 0.1F);
		        GlStateManager.enableBlend();
		        GlStateManager.blendFunc(770, 771);
		        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				GlStateManager.popMatrix(); TODO get this working*/
            } else if (basicType == BasicBlockType.SECURITY_DESK) {
                GlStateManager.rotate(180, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(180, 0.0F, 1.0F, 0.0F);
                GlStateManager.scale(0.8F, 0.8F, 0.8F);
                GlStateManager.translate(0.0F, -0.8F, 0.0F);
                mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "SecurityDesk.png"));
                securityDesk.render(0.0625F, mc.renderEngine);
            }

            return;
        }

        MachineType machineType = MachineType.get(stack);

        if (machineType != null) {
            if (machineType == MachineType.FLUID_TANK) {
                GlStateManager.pushMatrix();
                ItemBlockMachine itemMachine = (ItemBlockMachine) stack.getItem();
                float targetScale =
                      (float) (itemMachine.getFluidStack(stack) != null ? itemMachine.getFluidStack(stack).amount : 0)
                            / itemMachine.getCapacity(stack);
                FluidTankTier tier = FluidTankTier.values()[itemMachine.getBaseTier(stack).ordinal()];
                Fluid fluid =
                      itemMachine.getFluidStack(stack) != null ? itemMachine.getFluidStack(stack).getFluid() : null;
                RenderFluidTank.INSTANCE.render(tier, fluid, targetScale, false, null, -0.5, -0.5, -0.5);
                GlStateManager.popMatrix();
            } else if (machineType == MachineType.PERSONAL_CHEST) {
                GlStateManager.pushMatrix();

                GlStateManager.rotate(180F, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(-0.5F, -0.5F, -0.5F);
                GlStateManager.translate(0, 1.0F, 1.0F);
                GlStateManager.scale(1.0F, -1F, -1F);

                mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "PersonalChest.png"));

                personalChest.renderAll();
                GlStateManager.popMatrix();
            } else if (machineType == MachineType.SOLAR_NEUTRON_ACTIVATOR) {
                GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
                GlStateManager.scale(0.6F, 0.6F, 0.6F);
                GlStateManager.translate(0.0F, -0.55F, 0.0F);
                mc.renderEngine
                      .bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "SolarNeutronActivator.png"));
                solarNeutronActivator.render(0.0625F);
            } else if (machineType == MachineType.SEISMIC_VIBRATOR) {
                GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
                GlStateManager.scale(0.6F, 0.6F, 0.6F);
                GlStateManager.translate(0.0F, -0.55F, 0.0F);
                mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "SeismicVibrator.png"));
                seismicVibrator.render(0.0625F);
            } else if (machineType == MachineType.CHEMICAL_CRYSTALLIZER) {
                GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
                GlStateManager.translate(0.05F, -1.001F, 0.05F);
                mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ChemicalCrystallizer.png"));
                chemicalCrystallizer.render(0.0625F);
            } else if (machineType == MachineType.CHEMICAL_DISSOLUTION_CHAMBER) {
                GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
                GlStateManager.translate(0.05F, -1.001F, 0.05F);
                mc.renderEngine
                      .bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ChemicalDissolutionChamber.png"));
                chemicalDissolutionChamber.render(0.0625F);
            } else if (machineType == MachineType.QUANTUM_ENTANGLOPORTER) {
                GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
                GlStateManager.translate(0.0F, -1.0F, 0.0F);
                mc.renderEngine
                      .bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "QuantumEntangloporter.png"));
                quantumEntangloporter.render(0.0625F, mc.renderEngine, true);
            } else if (machineType == MachineType.RESISTIVE_HEATER) {
                GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
                GlStateManager.translate(0.05F, -0.96F, 0.05F);
                mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ResistiveHeater.png"));
                resistiveHeater.render(0.0625F, false, mc.renderEngine, true);
            } else if (machineType == MachineType.DIGITAL_MINER) {
                GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
                GL11.glTranslatef(0.35F, 0.1F, 0.0F);
                mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "DigitalMiner.png"));
                digitalMiner.render(0.022F, false, mc.renderEngine, true);
            }

            return;
        }

        if (stack.getItem() instanceof ItemBlockEnergyCube) {
            GlStateManager.pushMatrix();
            EnergyCubeTier tier = EnergyCubeTier.values()[((ITierItem) stack.getItem()).getBaseTier(stack).ordinal()];
            IEnergizedItem energized = (IEnergizedItem) stack.getItem();
            mc.renderEngine.bindTexture(RenderEnergyCube.baseTexture);

            GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(180F, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, -1.0F, 0.0F);

            MekanismRenderer.blendOn();

            energyCube.render(0.0625F, tier, mc.renderEngine, true);

            for (EnumFacing side : EnumFacing.VALUES) {
                mc.renderEngine.bindTexture(RenderEnergyCube.baseTexture);
                energyCube.renderSide(0.0625F, side, side == EnumFacing.NORTH ? IOState.OUTPUT : IOState.INPUT, tier,
                      mc.renderEngine);
            }

            MekanismRenderer.blendOff();
            GlStateManager.popMatrix();
        }

        if (type == TransformType.GUI) {
            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        } else {
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        }

        if (stack.getItem() == MekanismItems.Jetpack) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(90, 0.0F, -1.0F, 0.0F);
            GlStateManager.translate(0.2F, -0.35F, 0.0F);
            mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Jetpack.png"));
            jetpack.render(0.0625F);
            GlStateManager.popMatrix();
        } else if (stack.getItem() == MekanismItems.ArmoredJetpack) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(90, 0.0F, -1.0F, 0.0F);
            GlStateManager.translate(0.2F, -0.35F, 0.0F);
            mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Jetpack.png"));
            armoredJetpack.render(0.0625F);
            GlStateManager.popMatrix();
        } else if (stack.getItem() instanceof ItemGasMask) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(90, 0.0F, -1.0F, 0.0F);
            GlStateManager.translate(0.1F, 0.2F, 0.0F);
            mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ScubaSet.png"));
            gasMask.render(0.0625F);
            GlStateManager.popMatrix();
        } else if (stack.getItem() instanceof ItemScubaTank) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(90, 0.0F, -1.0F, 0.0F);
            GlStateManager.scale(1.6F, 1.6F, 1.6F);
            GlStateManager.translate(0.2F, -0.5F, 0.0F);
            mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ScubaSet.png"));
            scubaTank.render(0.0625F);
            GlStateManager.popMatrix();
        } else if (stack.getItem() instanceof ItemFreeRunners) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(90, 0.0F, -1.0F, 0.0F);
            GlStateManager.scale(2.0F, 2.0F, 2.0F);
            GlStateManager.translate(0.2F, -1.43F, 0.12F);
            mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "FreeRunners.png"));
            freeRunners.render(0.0625F);
            GlStateManager.popMatrix();
        } else if (stack.getItem() instanceof ItemAtomicDisassembler) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.4F, 1.4F, 1.4F);
            GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);

            if (type == TransformType.THIRD_PERSON_RIGHT_HAND || type == TransformType.THIRD_PERSON_LEFT_HAND) {
                if (type == TransformType.THIRD_PERSON_LEFT_HAND) {
                    GlStateManager.rotate(-90, 0.0F, 1.0F, 0.0F);
                }

                GlStateManager.rotate(45, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(50, 1.0F, 0.0F, 0.0F);
                GlStateManager.scale(2.0F, 2.0F, 2.0F);
                GlStateManager.translate(0.0F, -0.4F, 0.4F);
            } else if (type == TransformType.GUI) {
                GlStateManager.rotate(225, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(45, -1.0F, 0.0F, -1.0F);
                GlStateManager.scale(0.6F, 0.6F, 0.6F);
                GlStateManager.translate(0.0F, -0.2F, 0.0F);
            } else {
                if (type == TransformType.FIRST_PERSON_LEFT_HAND) {
                    GlStateManager.rotate(90, 0.0F, 1.0F, 0.0F);
                }

                GlStateManager.rotate(45, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(0.0F, -0.7F, 0.0F);
            }

            mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "AtomicDisassembler.png"));
            atomicDisassembler.render(0.0625F);
            GlStateManager.popMatrix();
        } else if (stack.getItem() instanceof ItemFlamethrower) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(160, 0.0F, 0.0F, 1.0F);
            mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Flamethrower.png"));

            GlStateManager.translate(0.0F, -1.0F, 0.0F);
            GlStateManager.rotate(135, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-20, 0.0F, 0.0F, 1.0F);

            if (type == TransformType.FIRST_PERSON_RIGHT_HAND || type == TransformType.THIRD_PERSON_RIGHT_HAND
                  || type == TransformType.FIRST_PERSON_LEFT_HAND || type == TransformType.THIRD_PERSON_LEFT_HAND) {
                if (type == TransformType.FIRST_PERSON_RIGHT_HAND) {
                    GlStateManager.rotate(55, 0.0F, 1.0F, 0.0F);
                } else if (type == TransformType.FIRST_PERSON_LEFT_HAND) {
                    GlStateManager.rotate(-160, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(30F, 1.0F, 0.0F, 0.0F);
                } else if (type == TransformType.THIRD_PERSON_RIGHT_HAND) {
                    GlStateManager.translate(0.0F, 0.7F, 0.0F);
                    GlStateManager.rotate(75, 0.0F, 1.0F, 0.0F);
                } else //if(type == TransformType.THIRD_PERSON_LEFT_HAND)
                {
                    GlStateManager.translate(0.0F, 0.7F, 0.0F);
                    GlStateManager.rotate(-75, 0.0F, 1.0F, 0.0F);
                }

                GlStateManager.scale(2.5F, 2.5F, 2.5F);
                GlStateManager.translate(0.0F, -1.0F, -0.5F);
            } else if (type == TransformType.GUI) {
                GlStateManager.translate(-0.6F, 0.0F, 0.0F);
                GlStateManager.rotate(45, 0.0F, 1.0F, 0.0F);
            }

            flamethrower.render(0.0625F);
            GlStateManager.popMatrix();
        }
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) {
            List<BakedQuad> faceQuads = new LinkedList<>();

            if (Block.getBlockFromItem(stack.getItem()) != Blocks.AIR) {
                MachineType machineType = MachineType.get(stack);

                if (machineType != MachineType.QUANTUM_ENTANGLOPORTER && machineType != MachineType.RESISTIVE_HEATER) {
                    if (!(stack.getItem() instanceof ItemBlockEnergyCube)) {
                        faceQuads.addAll(baseModel.getQuads(state, side, rand));
                    }
                }
            }

            return faceQuads;
        }

        Tessellator tessellator = Tessellator.getInstance();
        List<BakedQuad> generalQuads = new LinkedList<>();

        MekanismRenderer.pauseRenderer(tessellator);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        GlStateManager.rotate(180, 0.0F, 1.0F, 0.0F);
        doRender(prevTransform);
        GlStateManager.enableLighting();
        GlStateManager.enableLight(0);
        GlStateManager.enableLight(1);
        GlStateManager.enableColorMaterial();
        GlStateManager.colorMaterial(1032, 5634);
        GlStateManager.enableCull();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.popMatrix();

        MekanismRenderer.resumeRenderer(tessellator);

        if (Block.getBlockFromItem(stack.getItem()) != Blocks.AIR) {
            MachineType machineType = MachineType.get(stack);

            if (machineType != MachineType.DIGITAL_MINER) {
                if (!(stack.getItem() instanceof ItemBlockEnergyCube)) {
                    generalQuads.addAll(baseModel.getQuads(state, side, rand));
                }
            }
        }

        return generalQuads;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return baseModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return baseModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return baseModel.isBuiltInRenderer();
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return baseModel.getParticleTexture();
    }

    @Nonnull
    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return baseModel.getItemCameraTransforms();
    }

    @Nonnull
    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(
          @Nonnull ItemCameraTransforms.TransformType cameraTransformType) {
        prevTransform = cameraTransformType;

        return Pair.of(this, transforms.get(cameraTransformType).getMatrix());
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
