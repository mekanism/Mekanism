package mekanism.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.client.render.armor.MekaSuitArmor;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel.ArmPose;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderFirstPersonMekaSuitArms {

    @SubscribeEvent
    public void renderHand(RenderHandEvent event) {
        //TODO - 10.1: If https://github.com/MinecraftForge/MinecraftForge/pull/8254 gets merged, make an LTS PR of it and
        // switch this over to using that and remove the two ATs (calculateMapTilt, and renderMap) that are used by this
        // and at that point we can move this event listener into RenderTickHandler
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player != null && !player.isInvisible() && !player.isSpectator()) {
            ItemStack chestStack = player.getItemBySlot(EquipmentSlotType.CHEST);
            if (chestStack.getItem() instanceof ItemMekaSuitArmor) {
                ItemStack stack = event.getItemStack();
                Hand hand = event.getHand();
                if (stack.isEmpty()) {
                    if (hand == Hand.MAIN_HAND) {
                        renderFirstPersonHand(player, chestStack, event.getMatrixStack(), event.getBuffers(), player.getMainArm() == HandSide.RIGHT,
                              event.getSwingProgress(), event.getEquipProgress(), event.getLight());
                        event.setCanceled(true);
                    }
                } else if (stack.getItem() == Items.FILLED_MAP) {
                    if (hand == Hand.MAIN_HAND && player.getOffhandItem().isEmpty()) {
                        renderTwoHandedMap(player, chestStack, event.getMatrixStack(), event.getBuffers(), event.getSwingProgress(), event.getEquipProgress(),
                              event.getLight(), event.getInterpolatedPitch(), stack);
                    } else {
                        renderOneHandedMap(player, chestStack, event.getMatrixStack(), event.getBuffers(), event.getSwingProgress(), event.getEquipProgress(),
                              event.getLight(), hand, stack);
                    }
                    event.setCanceled(true);
                }
            }
        }
    }

    private void renderArm(MatrixStack matrix, IRenderTypeBuffer renderer, int light, boolean rightHand, ClientPlayerEntity player, ItemStack chestStack) {
        MekaSuitArmor armor = (MekaSuitArmor) ((ItemMekaSuitArmor) chestStack.getItem()).getGearModel();
        armor.setAllVisible(true);
        if (rightHand) {
            armor.rightArmPose = ArmPose.EMPTY;
        } else {
            armor.leftArmPose = ArmPose.EMPTY;
        }
        armor.attackTime = 0.0F;
        armor.crouching = false;
        armor.swimAmount = 0.0F;
        armor.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        armor.renderArm(matrix, renderer, light, OverlayTexture.NO_OVERLAY, chestStack.hasFoil(), player, rightHand);
    }

    /**
     * Copy of FirstPersonRenderer#renderPlayerArm but tweaked to render the MekaSuit's arm
     */
    private void renderFirstPersonHand(ClientPlayerEntity player, ItemStack chestStack, MatrixStack matrix, IRenderTypeBuffer renderer, boolean rightHand,
          float swingProgress, float equipProgress, int light) {
        matrix.pushPose();
        float f = rightHand ? 1.0F : -1.0F;
        float f1 = MathHelper.sqrt(swingProgress);
        float f2 = -0.3F * MathHelper.sin(f1 * (float) Math.PI);
        float f3 = 0.4F * MathHelper.sin(f1 * ((float) Math.PI * 2F));
        float f4 = -0.4F * MathHelper.sin(swingProgress * (float) Math.PI);
        matrix.translate(f * (f2 + 0.64000005F), f3 - 0.6F + equipProgress * -0.6F, f4 - 0.71999997F);
        matrix.mulPose(Vector3f.YP.rotationDegrees(f * 45.0F));
        float f5 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float f6 = MathHelper.sin(f1 * (float) Math.PI);
        matrix.mulPose(Vector3f.YP.rotationDegrees(f * f6 * 70.0F));
        matrix.mulPose(Vector3f.ZP.rotationDegrees(f * f5 * -20.0F));

        matrix.translate(f * -1.0F, 3.6F, 3.5D);
        matrix.mulPose(Vector3f.ZP.rotationDegrees(f * 120.0F));
        matrix.mulPose(Vector3f.XP.rotationDegrees(200.0F));
        matrix.mulPose(Vector3f.YP.rotationDegrees(f * -135.0F));
        matrix.translate(f * 5.6F, 0.0D, 0.0D);

        renderArm(matrix, renderer, light, rightHand, player, chestStack);

        matrix.popPose();
    }

    /**
     * Copy of FirstPersonRenderer#renderTwoHandedMap but tweaked to render the MekaSuit's arms with some extra rotations of renderMapHand factored up to this level.
     */
    private void renderTwoHandedMap(ClientPlayerEntity player, ItemStack chestStack, MatrixStack matrix, IRenderTypeBuffer renderer, float swingProgress,
          float equipProgress, int light, float interpolatedPitch, ItemStack map) {
        FirstPersonRenderer firstPersonRenderer = Minecraft.getInstance().getItemInHandRenderer();
        matrix.pushPose();
        float f = MathHelper.sqrt(swingProgress);
        float f1 = -0.2F * MathHelper.sin(swingProgress * (float) Math.PI);
        float f2 = -0.4F * MathHelper.sin(f * (float) Math.PI);
        matrix.translate(0.0D, -f1 / 2.0F, f2);
        float f3 = firstPersonRenderer.calculateMapTilt(interpolatedPitch);
        matrix.translate(0.0D, 0.04F + equipProgress * -1.2F + f3 * -0.5F, -0.72F);
        matrix.mulPose(Vector3f.XP.rotationDegrees(f3 * -85.0F));
        matrix.pushPose();
        matrix.mulPose(Vector3f.YP.rotationDegrees(90.0F));

        matrix.mulPose(Vector3f.YP.rotationDegrees(92.0F));
        matrix.mulPose(Vector3f.XP.rotationDegrees(45.0F));

        renderMapHand(matrix, renderer, light, true, player, chestStack);
        renderMapHand(matrix, renderer, light, false, player, chestStack);
        matrix.popPose();

        float f4 = MathHelper.sin(f * (float) Math.PI);
        matrix.mulPose(Vector3f.XP.rotationDegrees(f4 * 20.0F));
        matrix.scale(2.0F, 2.0F, 2.0F);
        firstPersonRenderer.renderMap(matrix, renderer, light, map);
        matrix.popPose();
    }

    /**
     * Copy of FirstPersonRenderer#renderMapHand but tweaked to render the MekaSuit's arm and with the extra rotations factored one level up.
     */
    private void renderMapHand(MatrixStack matrix, IRenderTypeBuffer renderer, int light, boolean rightHand, ClientPlayerEntity player, ItemStack chestStack) {
        matrix.pushPose();
        float f = rightHand ? 1.0F : -1.0F;
        //matrix.mulPose(Vector3f.YP.rotationDegrees(92.0F));
        //matrix.mulPose(Vector3f.XP.rotationDegrees(45.0F));
        matrix.mulPose(Vector3f.ZP.rotationDegrees(f * -41.0F));
        matrix.translate(f * 0.3F, -1.1F, 0.45F);
        renderArm(matrix, renderer, light, rightHand, player, chestStack);
        matrix.popPose();
    }

    /**
     * Copy of FirstPersonRenderer#renderOneHandedMap but tweaked to render the MekaSuit's arm
     */
    private void renderOneHandedMap(ClientPlayerEntity player, ItemStack chestStack, MatrixStack matrix, IRenderTypeBuffer renderer, float swingProgress,
          float equipProgress, int light, Hand hand, ItemStack map) {
        boolean rightHand = (player.getMainArm() == HandSide.RIGHT) == (hand == Hand.MAIN_HAND);
        float f = rightHand ? 1.0F : -1.0F;
        matrix.pushPose();
        matrix.translate(f * 0.125F, -0.125D, 0.0D);
        matrix.pushPose();
        matrix.mulPose(Vector3f.ZP.rotationDegrees(f * 10.0F));
        renderFirstPersonHand(player, chestStack, matrix, renderer, rightHand, swingProgress, equipProgress, light);
        matrix.popPose();

        matrix.pushPose();
        matrix.translate(f * 0.51F, -0.08F + equipProgress * -1.2F, -0.75D);
        float f1 = MathHelper.sqrt(swingProgress);
        float f2 = MathHelper.sin(f1 * (float) Math.PI);
        float f3 = -0.5F * f2;
        float f4 = 0.4F * MathHelper.sin(f1 * ((float) Math.PI * 2F));
        float f5 = -0.3F * MathHelper.sin(swingProgress * (float) Math.PI);
        matrix.translate(f * f3, f4 - 0.3F * f2, f5);
        matrix.mulPose(Vector3f.XP.rotationDegrees(f2 * -45.0F));
        matrix.mulPose(Vector3f.YP.rotationDegrees(f * f2 * -30.0F));
        Minecraft.getInstance().getItemInHandRenderer().renderMap(matrix, renderer, light, map);
        matrix.popPose();
        matrix.popPose();
    }
}