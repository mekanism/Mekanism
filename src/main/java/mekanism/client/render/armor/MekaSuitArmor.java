package mekanism.client.render.armor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.client.model.BaseModelCache.MekanismModelData;
import mekanism.client.model.BaseModelCache.OBJModelData;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import mekanism.client.render.lib.QuickHash;
import mekanism.client.render.lib.effect.BoltRenderer;
import mekanism.common.Mekanism;
import mekanism.common.content.gear.shared.ModuleColorModulationUnit;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.lib.Color;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.effect.BoltEffect.BoltRenderInfo;
import mekanism.common.lib.effect.BoltEffect.SpawnFunction;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ElytraAnimationState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ModelEvent.BakingCompleted;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MekaSuitArmor implements ICustomArmor, ISpecialGear {

    private static final String LED_TAG = "led";
    private static final String INACTIVE_TAG = "inactive_";
    private static final String OVERRIDDEN_TAG = "override_";
    private static final String EXCLUSIVE_TAG = "excl_";
    private static final String SHARED_TAG = "shared_";
    private static final String GLASS_TAG = "glass";

    public static final MekaSuitArmor HELMET = new MekaSuitArmor(EquipmentSlot.HEAD, EquipmentSlot.CHEST);
    public static final MekaSuitArmor BODYARMOR = new MekaSuitArmor(EquipmentSlot.CHEST, EquipmentSlot.HEAD);
    public static final MekaSuitArmor PANTS = new MekaSuitArmor(EquipmentSlot.LEGS, EquipmentSlot.FEET);
    public static final MekaSuitArmor BOOTS = new MekaSuitArmor(EquipmentSlot.FEET, EquipmentSlot.LEGS);

    private static final Table<EquipmentSlot, Holder<ModuleData<?>>, ModuleModelSpec> moduleModelSpec = HashBasedTable.create();

    private static final Map<UUID, BoltRenderer> boltRenderMap = new Object2ObjectOpenHashMap<>();

    private static final QuadTransformation BASE_TRANSFORM = QuadTransformation.list(QuadTransformation.rotate(0, 0, 180), QuadTransformation.translate(-1, 0.5F, 0));

    private final LoadingCache<QuickHash, ArmorQuads> cache = CacheBuilder.newBuilder().build(new CacheLoader<>() {
        @NotNull
        @Override
        @SuppressWarnings("unchecked")
        public ArmorQuads load(@NotNull QuickHash key) {
            return createQuads((Object2BooleanMap<ModuleModelSpec>) key.objs()[0], (Set<EquipmentSlot>) key.objs()[1], (boolean) key.objs()[2], (boolean) key.objs()[3]);
        }
    });

    private final EquipmentSlot type;
    private final EquipmentSlot adjacentType;

    private MekaSuitArmor(EquipmentSlot type, EquipmentSlot adjacentType) {
        this.type = type;
        this.adjacentType = adjacentType;
        MekanismModelCache.INSTANCE.reloadCallback(cache::invalidateAll);
    }

    private static Color getColor(ItemStack stack) {
        IModule<ModuleColorModulationUnit> colorUnit = IModuleHelper.INSTANCE.getModule(stack, MekanismModules.COLOR_MODULATION_UNIT);
        return colorUnit != null ? colorUnit.getCustomInstance().color() : Color.WHITE;
    }

    public <STATE extends HumanoidRenderState> void renderArm(HumanoidModel<STATE> baseModel, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords,
          STATE state, ItemStack stack, boolean rightHand) {
        ModelPos armPos = rightHand ? ModelPos.RIGHT_ARM : ModelPos.LEFT_ARM;
        ArmorQuads armorQuads = cache.getUnchecked(key(state));
        boolean hasOpaqueArm = armorQuads.opaqueQuads().containsKey(armPos);
        boolean hasTransparentArm = armorQuads.transparentQuads().containsKey(armPos);
        if (hasOpaqueArm || hasTransparentArm) {
            poseStack.pushPose();
            armPos.translate(baseModel, poseStack, state);
            PoseStack.Pose last = poseStack.last();
            if (hasOpaqueArm) {
                VertexConsumer builder = ItemRenderer.getFoilBuffer(nodeCollector, MekanismRenderType.MEKASUIT, false, stack.hasFoil());
                putQuads(armorQuads.opaqueQuads().get(armPos), builder, last, lightCoords, getColor(stack));
            }
            if (hasTransparentArm) {
                VertexConsumer builder = ItemRenderer.getFoilBuffer(nodeCollector, RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), false, stack.hasFoil());
                putQuads(armorQuads.transparentQuads().get(armPos), builder, last, lightCoords, Color.WHITE);
            }
            poseStack.popPose();
        }
    }

    @Override
    public <STATE extends HumanoidRenderState> void render(HumanoidModel<STATE> baseModel, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords,
          STATE state, ItemStack stack) {
        if (state.isBaby) {
            poseStack.pushPose();
            float f1 = 1.0F / baseModel.babyBodyScale;
            poseStack.scale(f1, f1, f1);
            poseStack.translate(0.0D, baseModel.bodyYOffset / 16.0F, 0.0D);
            renderMekaSuit(baseModel, poseStack, nodeCollector, lightCoords, getColor(stack), stack.hasFoil(), state);
            poseStack.popPose();
        } else {
            renderMekaSuit(baseModel, poseStack, nodeCollector, lightCoords, getColor(stack), stack.hasFoil(), state);
        }
    }

    private <STATE extends HumanoidRenderState> void renderMekaSuit(HumanoidModel<STATE> baseModel, PoseStack poseStack, SubmitNodeCollector nodeCollector,
          int lightCoords, Color color, boolean hasEffect, STATE state) {
        ArmorQuads armorQuads = cache.getUnchecked(key(state));
        render(baseModel, nodeCollector, poseStack, lightCoords, color, hasEffect, state, armorQuads.opaqueQuads(), false);

        if (type == EquipmentSlot.CHEST) {
            BoltRenderer boltRenderer = boltRenderMap.computeIfAbsent(entity.getUUID(), id -> new BoltRenderer());
            if (IModuleHelper.INSTANCE.isEnabled(state.chestEquipment, MekanismModules.GRAVITATIONAL_MODULATING_UNIT)) {
                BoltEffect leftBolt = new BoltEffect(BoltRenderInfo.ELECTRICITY, new Vec3(-0.01, 0.35, 0.37), new Vec3(-0.01, 0.15, 0.37), 10)
                      .size(0.012F).lifespan(6).spawn(SpawnFunction.noise(3, 1));
                BoltEffect rightBolt = new BoltEffect(BoltRenderInfo.ELECTRICITY, new Vec3(0.025, 0.35, 0.37), new Vec3(0.025, 0.15, 0.37), 10)
                      .size(0.012F).lifespan(6).spawn(SpawnFunction.noise(3, 1));
                boltRenderer.update(0, leftBolt, state.partialTick);
                boltRenderer.update(1, rightBolt, state.partialTick);
            }
            //Adjust the poseStack so that we render the lightning in the correct spot if the player is crouching
            poseStack.pushPose();
            ModelPos.BODY.translate(baseModel, poseStack, state);
            boltRenderer.render(gameTime, state.partialTick, poseStack, renderer);
            poseStack.popPose();
        }

        //Pass white as the color because we don't want to tint transparent quads
        render(baseModel, renderer, poseStack, lightCoords, Color.WHITE, hasEffect, state, armorQuads.transparentQuads(), true);
    }

    private <STATE extends HumanoidRenderState> void render(HumanoidModel<STATE> baseModel, SubmitNodeCollector nodeCollector, PoseStack poseStack, int lightCoords,
          Color color, boolean hasEffect, STATE state, Map<ModelPos, List<BakedQuad>> quadMap, boolean transparent) {
        if (!quadMap.isEmpty()) {
            RenderType renderType = transparent ? RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS) : MekanismRenderType.MEKASUIT;
            VertexConsumer builder = ItemRenderer.getFoilBuffer(nodeCollector, renderType, false, hasEffect);
            for (Map.Entry<ModelPos, List<BakedQuad>> entry : quadMap.entrySet()) {
                poseStack.pushPose();
                entry.getKey().translate(baseModel, poseStack, state);
                putQuads(entry.getValue(), builder, poseStack.last(), lightCoords, color);
                poseStack.popPose();
            }
        }
    }

    private void putQuads(List<BakedQuad> quads, VertexConsumer builder, PoseStack.Pose pose, int lightCoords, Color color) {
        for (BakedQuad quad : quads) {
            builder.putBulkData(pose, quad, color.rf(), color.gf(), color.bf(), color.af(), lightCoords, OverlayTexture.NO_OVERLAY);
        }
    }

    private static List<BakedQuad> getQuads(MekanismModelData data, Set<String> parts, Set<String> ledParts, @Nullable QuadTransformation transform) {
        RandomSource random = Minecraft.getInstance().level.getRandom();
        List<BakedQuad> quads = new ArrayList<>();
        //Note: We need to use a new list to not accidentally pollute the cached bake quads with the LED quads that we match them with
        // this also means that we can avoid even baking the data against empty part lists entirely
        if (!parts.isEmpty()) {
            quads.addAll(data.bake(new MekaSuitModelConfiguration(parts)).getQuads(null, null, random, ModelData.EMPTY, null));
        }
        if (!ledParts.isEmpty()) {
            List<BakedQuad> ledQuads = data.bake(new MekaSuitModelConfiguration(ledParts)).getQuads(null, null, random, ModelData.EMPTY, null);
            quads.addAll(QuadUtils.transformBakedQuads(ledQuads, QuadTransformation.fullbright));
        }
        if (transform != null) {
            quads = QuadUtils.transformBakedQuads(quads, transform);
        }
        return quads;
    }

    @NotNull
    @Override
    public ICustomArmor gearModel() {
        return this;
    }

    public enum ModelPos {
        HEAD(BASE_TRANSFORM, s -> s.contains("head")),
        BODY(BASE_TRANSFORM, s -> s.contains("body")),
        LEFT_ARM(BASE_TRANSFORM.and(QuadTransformation.translate(-0.3125F, -0.125F, 0)), s -> s.contains("left_arm")),
        RIGHT_ARM(BASE_TRANSFORM.and(QuadTransformation.translate(0.3125F, -0.125F, 0)), s -> s.contains("right_arm")),
        LEFT_LEG(BASE_TRANSFORM.and(QuadTransformation.translate(-0.125F, -0.75F, 0)), s -> s.contains("left_leg")),
        RIGHT_LEG(BASE_TRANSFORM.and(QuadTransformation.translate(0.125F, -0.75F, 0)), s -> s.contains("right_leg")),
        LEFT_WING(BASE_TRANSFORM, s -> s.contains("left_wing")),
        RIGHT_WING(BASE_TRANSFORM, s -> s.contains("right_wing"));

        private static final float EXPANDED_WING_X = 1;
        private static final float EXPANDED_WING_Y = -2.5F;
        private static final float EXPANDED_WING_Z = 5;
        private static final float EXPANDED_WING_Y_ROT = 45;
        private static final float EXPANDED_WING_Z_ROT = 25;
        public static final ModelPos[] VALUES = values();

        private final QuadTransformation transform;
        private final Predicate<String> modelSpec;

        ModelPos(QuadTransformation transform, Predicate<String> modelSpec) {
            this.transform = transform;
            this.modelSpec = modelSpec;
        }

        public QuadTransformation getTransform() {
            return transform;
        }

        public boolean contains(String s) {
            return modelSpec.test(s);
        }

        public static ModelPos get(String name) {
            name = name.toLowerCase(Locale.ROOT);
            for (ModelPos pos : VALUES) {
                if (pos.contains(name)) {
                    return pos;
                }
            }
            return null;
        }

        public <STATE extends HumanoidRenderState> void translate(HumanoidModel<STATE> baseModel, PoseStack poseStack, STATE state) {
            switch (this) {
                case HEAD -> baseModel.head.translateAndRotate(poseStack);
                case BODY -> baseModel.body.translateAndRotate(poseStack);
                case LEFT_ARM -> baseModel.leftArm.translateAndRotate(poseStack);
                case RIGHT_ARM -> baseModel.rightArm.translateAndRotate(poseStack);
                case LEFT_LEG -> baseModel.leftLeg.translateAndRotate(poseStack);
                case RIGHT_LEG -> baseModel.rightLeg.translateAndRotate(poseStack);
                case LEFT_WING, RIGHT_WING -> translateWings(baseModel, poseStack, state);
            }
        }

        private <STATE extends HumanoidRenderState> void translateWings(HumanoidModel<STATE> baseModel, PoseStack poseStack, STATE state) {
            baseModel.body.translateAndRotate(poseStack);
            float x = 0;
            float y = 0;
            float z = 0;
            float yRot = 0;
            float zRot = 0;
            //Note: In theory the entity is always "fall flying" for wing rendering given our conditions
            // for it rendering, but we validate it just in case.
            //If the entity is not dive-bombing the ground (at which point the wings will be folded)
            if (state.isFallFlying && state.xRot < 45) {
                float scale = 0;
                // then we check if the entity is not pointing steeply into the sky
                // if it isn't or if the entity has a lot of movement
                if (state.xRot > -45 || entity.getDeltaMovement().y > 1) {
                    // then we fully expand the wings
                    scale = 1;
                } else if (entity.getDeltaMovement().y > 0) {
                    // otherwise, if the entity is pointing steeply into the sky, and we have a small amount
                    // of movement (y movement between zero and one) then we partially expand the wings
                    scale = (float) entity.getDeltaMovement().y;
                }
                // if we don't have any upwards momentum, and we are pointing steeply into the sky then we just fold the wings
                x = EXPANDED_WING_X * scale;
                y = EXPANDED_WING_Y * scale;
                z = EXPANDED_WING_Z * scale;
                yRot = EXPANDED_WING_Y_ROT * scale;
                zRot = EXPANDED_WING_Z_ROT * scale;
            }
            //TODO - 1.21.11: I think we should actually be updating the rotations in entity.elytraAnimationState rather than in the state?
            if (state instanceof AvatarRenderState playerState) {
                //If the entity is a player, then transition the wings gradually to their target position
                ElytraAnimationState elytraAnimationState;
                //TODO - 1.21.11: What is the difference between playerState.flyingYRot and state.elytraRotY?
                state.elytraRotX = 0;
                yRot = state.elytraRotY = state.elytraRotY + (yRot - state.elytraRotY) * 0.01F;
                //Base off of target values
                float scale = state.elytraRotY / EXPANDED_WING_Y_ROT;
                x = EXPANDED_WING_X * scale;
                y = EXPANDED_WING_Y * scale;
                z = EXPANDED_WING_Z * scale;
                zRot = state.elytraRotZ = EXPANDED_WING_Z_ROT * scale;
            }
            if (this == RIGHT_WING) {
                //Invert things that need to be inverted for the right wing to mirror it properly
                x = -x;
                yRot = -yRot;
                zRot = -zRot;
            }
            poseStack.translate(x / 16, y / 16, z / 16);
            if (yRot != 0.0F) {
                poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
            }
            if (zRot != 0.0F) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(zRot));
            }

        }
    }

    private static void processMekaTool(OBJModelData mekaToolModel, Set<String> ignored) {
        for (String name : mekaToolModel.getModel().getRootComponentNames()) {
            if (name.contains(OVERRIDDEN_TAG)) {
                //Note: We just ignore the pieces here as the override will be rendered as part of the item's model
                ignored.add(processOverrideName(name, "mekatool"));
            }
        }
    }

    private record OverrideData(MekanismModelData modelData, String name) {
    }

    private ArmorQuads createQuads(Object2BooleanMap<ModuleModelSpec> modules, Set<EquipmentSlot> wornParts, boolean hasMekaToolLeft, boolean hasMekaToolRight) {
        Map<MekanismModelData, Map<ModelPos, Set<String>>> specialQuadsToRender = new Object2ObjectOpenHashMap<>();
        Map<MekanismModelData, Map<ModelPos, Set<String>>> specialLEDQuadsToRender = new Object2ObjectOpenHashMap<>();
        // map of normal model part name to overwritten model part name (i.e. helmet_head_center1 -> override_solar_helmet_helmet_head_center1)
        Map<String, OverrideData> overrides = new Object2ObjectOpenHashMap<>();
        Set<String> ignored = new HashSet<>();

        if (!modules.isEmpty()) {
            Map<MekanismModelData, Set<String>> allMatchedParts = new Object2ObjectOpenHashMap<>();
            for (ModuleOBJModelData modelData : MekanismModelCache.INSTANCE.MEKASUIT_MODULES) {
                Set<String> matchedParts = allMatchedParts.computeIfAbsent(modelData, d -> new HashSet<>());
                for (ObjectIterator<Object2BooleanMap.Entry<ModuleModelSpec>> iterator = Object2BooleanMaps.fastIterator(modules); iterator.hasNext(); ) {
                    Object2BooleanMap.Entry<ModuleModelSpec> entry = iterator.next();
                    ModuleModelSpec spec = entry.getKey();
                    for (String name : modelData.getPartsForSpec(spec, entry.getBooleanValue())) {
                        if (name.contains(OVERRIDDEN_TAG)) {
                            overrides.put(spec.processOverrideName(name), new OverrideData(modelData, name));
                        }
                        // if this armor unit controls rendering of this module
                        if (type == spec.slotType) {
                            // then add the part as one we will need to add to render, this way we can ensure
                            // we respect any overrides that might be in a later model part
                            matchedParts.add(name);
                        }
                    }
                }
            }
            for (Map.Entry<MekanismModelData, Set<String>> entry : allMatchedParts.entrySet()) {
                Set<String> matchedParts = entry.getValue();
                if (!matchedParts.isEmpty()) {
                    MekanismModelData modelData = entry.getKey();
                    Map<ModelPos, Set<String>> quadsToRender = specialQuadsToRender.computeIfAbsent(modelData, d -> new EnumMap<>(ModelPos.class));
                    Map<ModelPos, Set<String>> ledQuadsToRender = specialLEDQuadsToRender.computeIfAbsent(modelData, d -> new EnumMap<>(ModelPos.class));
                    //For all the parts we matched, go through and try adding them, while respecting any overrides we might have
                    for (String name : matchedParts) {
                        ModelPos pos = ModelPos.get(name);
                        if (pos == null) {
                            Mekanism.logger.warn("MekaSuit part '{}' is invalid from modules model. Ignoring.", name);
                        } else {
                            //Note: Currently the special quads here for overrides will likely point to our module and module led quads to render
                            // but for consistency and future proofing it is better to make sure we look it up in case overrides gets other stuff
                            // added to it at some point
                            addQuadsToRender(pos, name, overrides, quadsToRender, ledQuadsToRender, specialQuadsToRender, specialLEDQuadsToRender);
                        }
                    }
                }
            }
        }

        // handle mekatool overrides
        if (type == EquipmentSlot.CHEST) {
            if (hasMekaToolLeft) {
                processMekaTool(MekanismModelCache.INSTANCE.MEKATOOL_LEFT_HAND, ignored);
            }
            if (hasMekaToolRight) {
                processMekaTool(MekanismModelCache.INSTANCE.MEKATOOL_RIGHT_HAND, ignored);
            }
        }

        Map<ModelPos, Set<String>> armorQuadsToRender = new EnumMap<>(ModelPos.class);
        Map<ModelPos, Set<String>> armorLEDQuadsToRender = new EnumMap<>(ModelPos.class);
        for (String name : MekanismModelCache.INSTANCE.MEKASUIT.getModel().getRootComponentNames()) {
            if (!checkEquipment(type, name)) {
                // skip if it's the wrong equipment type
                continue;
            } else if (name.startsWith(EXCLUSIVE_TAG)) {
                if (wornParts.contains(adjacentType)) {
                    // skip if the part is exclusive and the adjacent part is present
                    continue;
                }
            } else if (name.startsWith(SHARED_TAG) && wornParts.contains(adjacentType) && adjacentType.ordinal() > type.ordinal()) {
                // skip if the part is shared and the shared part already rendered
                continue;
            }
            ModelPos pos = ModelPos.get(name);
            if (pos == null) {
                Mekanism.logger.warn("MekaSuit part '{}' is invalid. Ignoring.", name);
            } else if (!ignored.contains(name)) {
                addQuadsToRender(pos, name, overrides, armorQuadsToRender, armorLEDQuadsToRender, specialQuadsToRender, specialLEDQuadsToRender);
            }
        }

        Map<ModelPos, List<BakedQuad>> opaqueMap = new EnumMap<>(ModelPos.class);
        Map<ModelPos, List<BakedQuad>> transparentMap = new EnumMap<>(ModelPos.class);
        for (ModelPos pos : ModelPos.VALUES) {
            for (MekanismModelData modelData : MekanismModelCache.INSTANCE.MEKASUIT_MODULES) {
                parseTransparency(modelData, pos, opaqueMap, transparentMap, specialQuadsToRender.getOrDefault(modelData, Collections.emptyMap()),
                      specialLEDQuadsToRender.getOrDefault(modelData, Collections.emptyMap()));
            }
            parseTransparency(MekanismModelCache.INSTANCE.MEKASUIT, pos, opaqueMap, transparentMap, armorQuadsToRender, armorLEDQuadsToRender);
        }
        return new ArmorQuads(opaqueMap, transparentMap);
    }

    private static void addQuadsToRender(ModelPos pos, String name, Map<String, OverrideData> overrides, Map<ModelPos, Set<String>> quadsToRender,
          Map<ModelPos, Set<String>> ledQuadsToRender, Map<MekanismModelData, Map<ModelPos, Set<String>>> specialQuadsToRender,
          Map<MekanismModelData, Map<ModelPos, Set<String>>> specialLEDQuadsToRender) {
        OverrideData override = overrides.get(name);
        if (override != null) {
            //Update the name and the target quads if there is an override
            name = override.name();
            // Note: In theory the special quads should have our model data corresponding
            // to a map already, but on the off chance they don't compute and add it
            MekanismModelData overrideData = override.modelData();
            quadsToRender = specialQuadsToRender.computeIfAbsent(overrideData, d -> new EnumMap<>(ModelPos.class));
            ledQuadsToRender = specialLEDQuadsToRender.computeIfAbsent(overrideData, d -> new EnumMap<>(ModelPos.class));
        }
        if (name.contains(LED_TAG)) {
            ledQuadsToRender.computeIfAbsent(pos, p -> new HashSet<>()).add(name);
        } else {
            quadsToRender.computeIfAbsent(pos, p -> new HashSet<>()).add(name);
        }
    }

    private static void parseTransparency(MekanismModelData modelData, ModelPos pos, Map<ModelPos, List<BakedQuad>> opaqueMap, Map<ModelPos, List<BakedQuad>> transparentMap,
          Map<ModelPos, Set<String>> regularQuads, Map<ModelPos, Set<String>> ledQuads) {
        Set<String> opaqueRegularQuads = new HashSet<>(), opaqueLEDQuads = new HashSet<>();
        Set<String> transparentRegularQuads = new HashSet<>(), transparentLEDQuads = new HashSet<>();
        parseTransparency(pos, opaqueRegularQuads, transparentRegularQuads, regularQuads);
        parseTransparency(pos, opaqueLEDQuads, transparentLEDQuads, ledQuads);
        addParsedQuads(modelData, pos, opaqueMap, opaqueRegularQuads, opaqueLEDQuads);
        addParsedQuads(modelData, pos, transparentMap, transparentRegularQuads, transparentLEDQuads);
    }

    private static void addParsedQuads(MekanismModelData modelData, ModelPos pos, Map<ModelPos, List<BakedQuad>> map, Set<String> quads, Set<String> ledQuads) {
        //Only add a new entry to our map if we will have any quads. Our getQuads method will return empty if there are no quads
        List<BakedQuad> bakedQuads = getQuads(modelData, quads, ledQuads, pos.getTransform());
        if (!bakedQuads.isEmpty()) {
            map.computeIfAbsent(pos, p -> new ArrayList<>()).addAll(bakedQuads);
        }
    }

    private static void parseTransparency(ModelPos pos, Set<String> opaqueQuads, Set<String> transparentQuads, Map<ModelPos, Set<String>> quads) {
        for (String quad : quads.getOrDefault(pos, Collections.emptySet())) {
            if (quad.contains(GLASS_TAG)) {
                transparentQuads.add(quad);
            } else {
                opaqueQuads.add(quad);
            }
        }
    }

    private static boolean checkEquipment(EquipmentSlot type, String text) {
        return switch (type) {
            case HEAD -> text.contains("helmet");
            case CHEST -> text.contains("chest");
            case LEGS -> text.contains("leggings");
            case FEET -> text.contains("boots");
            default -> false;
        };
    }

    private record ArmorQuads(Map<ModelPos, List<BakedQuad>> opaqueQuads, Map<ModelPos, List<BakedQuad>> transparentQuads) {

        private ArmorQuads {
            if (opaqueQuads.isEmpty()) {
                opaqueQuads = Collections.emptyMap();
            }
            if (transparentQuads.isEmpty()) {
                transparentQuads = Collections.emptyMap();
            }
        }
    }

    private record ModuleModelSpec(ModuleData<?> module, EquipmentSlot slotType, String name, Predicate<LivingEntity> isActive) {

        /**
         * Score closest to zero is considered best, negative one for no match at all.
         */
        public int score(String name) {
            return name.indexOf(this.name + "_");
        }

        public boolean isActive(LivingEntity entity) {
            return isActive.test(entity);
        }

        public String processOverrideName(String part) {
            return MekaSuitArmor.processOverrideName(part, name);
        }
    }

    private static String processOverrideName(String part, String name) {
        return part.replaceFirst(OVERRIDDEN_TAG, "").replaceFirst(name + "_", "");
    }

    /**
     * Call via {@link IModuleHelper#addMekaSuitModuleModelSpec(String, Holder, EquipmentSlot, Predicate)}.
     */
    @Internal
    public static void registerModule(String name, Holder<ModuleData<?>> moduleData, EquipmentSlot slotType, Predicate<LivingEntity> isActive) {
        moduleModelSpec.put(slotType, moduleData, new ModuleModelSpec(moduleData.value(), slotType, name, isActive));
    }

    private ItemStack getItemBySlot(HumanoidRenderState state, EquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND -> state.mainArm == HumanoidArm.RIGHT ? state.rightHandItemStack : state.leftHandItemStack;
            case OFFHAND -> state.mainArm == HumanoidArm.RIGHT ? state.leftHandItemStack : state.rightHandItemStack;
            case FEET -> state.feetEquipment;
            case LEGS -> state.legsEquipment;
            case CHEST -> state.chestEquipment;
            case HEAD -> state.headEquipment;
            default -> ItemStack.EMPTY;
        };
    }

    public QuickHash key(HumanoidRenderState state) {
        Object2BooleanMap<ModuleModelSpec> modules = new Object2BooleanOpenHashMap<>();
        Set<EquipmentSlot> wornParts = EnumSet.noneOf(EquipmentSlot.class);
        for (EquipmentSlot slotType : EnumUtils.ARMOR_SLOTS) {
            ItemStack stack = getItemBySlot(state, slotType);
            if (stack.getItem() instanceof ItemMekaSuitArmor) {
                IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainer(stack);
                if (container != null) {
                    wornParts.add(slotType);
                    for (Entry<Holder<ModuleData<?>>, ModuleModelSpec> entry : moduleModelSpec.row(slotType).entrySet()) {
                        if (container.hasEnabled(entry.getKey())) {
                            ModuleModelSpec spec = entry.getValue();
                            modules.put(spec, spec.isActive(state));
                        }
                    }
                }
            }
        }
        return new QuickHash(modules.isEmpty() ? Object2BooleanMaps.emptyMap() : modules, wornParts.isEmpty() ? Collections.emptySet() : wornParts,
              state.leftHandItemStack.getItem() instanceof ItemMekaTool, state.rightHandItemStack.getItem() instanceof ItemMekaTool);
    }

    public static class ModuleOBJModelData extends OBJModelData {

        private record SpecData(Set<String> active, Set<String> inactive) {
        }

        private final Map<ModuleModelSpec, SpecData> specParts = new Object2ObjectOpenHashMap<>();

        public ModuleOBJModelData(Identifier rl) {
            super(rl);
        }

        private Set<String> getPartsForSpec(ModuleModelSpec spec, boolean active) {
            SpecData specData = specParts.get(spec);
            if (specData == null) {
                return Collections.emptySet();
            }
            return active ? specData.active() : specData.inactive();
        }

        @Override
        protected void reload(BakingCompleted evt) {
            super.reload(evt);
            Collection<ModuleModelSpec> modules = moduleModelSpec.values();
            for (String name : getModel().getRootComponentNames()) {
                //Find the "best" spec by checking all the specs and finding out which one is listed first
                // this way if we are overriding another module, then we just put the module that is overriding
                // the other one first in the name so that it gets the spec matched to it
                ModuleModelSpec matchingSpec = null;
                int bestScore = -1;
                for (ModuleModelSpec spec : modules) {
                    int score = spec.score(name);
                    if (score != -1 && (bestScore == -1 || score < bestScore)) {
                        bestScore = score;
                        matchingSpec = spec;
                    }
                }
                if (matchingSpec != null) {
                    SpecData specData = specParts.computeIfAbsent(matchingSpec, spec -> new SpecData(new HashSet<>(), new HashSet<>()));
                    if (name.contains(INACTIVE_TAG + matchingSpec.name + "_")) {
                        specData.inactive().add(name);
                    } else {
                        specData.active().add(name);
                    }
                }
            }
            //Update entries to reclaim some memory for empty sets
            for (Map.Entry<ModuleModelSpec, SpecData> entry : specParts.entrySet()) {
                SpecData specData = entry.getValue();
                if (specData.active().isEmpty()) {
                    entry.setValue(new SpecData(Collections.emptySet(), specData.inactive()));
                } else if (specData.inactive().isEmpty()) {
                    entry.setValue(new SpecData(specData.active(), Collections.emptySet()));
                }
            }
        }
    }

    private record MekaSuitModelConfiguration(Set<String> parts) implements IGeometryBakingContext {

        private static final Material NO_MATERIAL = new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation());

        private MekaSuitModelConfiguration {
            parts = parts.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(parts);
        }

        @NotNull
        @Override
        public String getModelName() {
            return "mekanism:mekasuit";
        }

        @Override
        public boolean hasMaterial(@NotNull String name) {
            return false;
        }

        @NotNull
        @Override
        public Material getMaterial(@NotNull String name) {
            return NO_MATERIAL;
        }

        @Override
        public boolean isGui3d() {
            return false;
        }

        @Override
        public boolean useBlockLight() {
            return false;
        }

        @Override
        public boolean useAmbientOcclusion() {
            return true;
        }

        @NotNull
        @Override
        @Deprecated
        public ItemTransforms getTransforms() {
            return ItemTransforms.NO_TRANSFORMS;
        }

        @NotNull
        @Override
        public Transformation getRootTransform() {
            return Transformation.identity();
        }

        @Nullable
        @Override
        public Identifier getRenderTypeHint() {
            return null;
        }

        @Override
        public boolean isComponentVisible(@NotNull String component, boolean fallback) {
            //Ignore fallback as we always have a true or false answer
            return parts.contains(component);
        }
    }
}