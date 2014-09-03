package mekanism.client.render;

import mekanism.client.model.ModelArmoredJetpack;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.model.ModelGasMask;
import mekanism.client.model.ModelJetpack;
import mekanism.client.model.ModelScubaTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelCustomArmor extends ModelBiped
{
	public static ModelCustomArmor INSTANCE = new ModelCustomArmor();

	public static GlowArmor GLOW_BIG = new GlowArmor(1.0F);
	public static GlowArmor GLOW_SMALL = new GlowArmor(0.5F);

	public static Minecraft mc = Minecraft.getMinecraft();

	public ArmorModel modelType;

	public ModelCustomArmor()
	{
		resetPart(bipedHead, 0, 0, 0);
		resetPart(bipedBody, 0, 0, 0);
		resetPart(bipedRightArm, 5, 2, 0);
		resetPart(bipedLeftArm, -5, 2, 0);
		resetPart(bipedRightLeg, 0, 0, 0);
		resetPart(bipedLeftLeg, 0, 0, 0);

		bipedHeadwear.cubeList.clear();
		bipedEars.cubeList.clear();
		bipedCloak.cubeList.clear();
	}

	public void init(Entity entity, float f, float f1, float f2, float f3, float f4, float size)
	{
		reset();

		if(entity instanceof EntityLivingBase)
		{
			isSneak = ((EntityLivingBase)entity).isSneaking();
			isRiding = ((EntityLivingBase)entity).isRiding();
			isChild = ((EntityLivingBase)entity).isChild();
		}

		if(modelType.armorSlot == 0)
		{
			bipedHead.isHidden = false;
			bipedHead.showModel = true;
		}
		else if(modelType.armorSlot == 1)
		{
			bipedBody.isHidden = false;
			bipedBody.showModel = true;
		}
		else if(modelType.armorSlot == 3)
		{
			bipedLeftLeg.isHidden = false;
			bipedLeftLeg.showModel = true;
			bipedRightLeg.isHidden = false;
			bipedRightLeg.showModel = true;
		}

		setRotationAngles(f, f1, f2, f3, f4, size, entity);
	}

	public void reset()
	{
		bipedHead.isHidden = true;
		bipedBody.isHidden = true;
		bipedRightArm.isHidden = true;
		bipedLeftArm.isHidden = true;
		bipedRightLeg.isHidden = true;
		bipedLeftLeg.isHidden = true;

		bipedHead.showModel = false;
		bipedBody.showModel = false;
		bipedRightArm.showModel = false;
		bipedLeftArm.showModel = false;
		bipedRightLeg.showModel = false;
		bipedLeftLeg.showModel = false;
	}

	public void resetPart(ModelRenderer renderer, float x, float y, float z)
	{
		renderer.cubeList.clear();
		ModelCustom model = new ModelCustom(this, renderer);
		renderer.addChild(model);
		setOffset(renderer, x, y, z);
	}

	public void setOffset(ModelRenderer renderer, float x, float y, float z)
	{
		renderer.offsetX = x;
		renderer.offsetY = y;
		renderer.offsetZ = z;
	}

	public class ModelCustom extends ModelRenderer
	{
		public ModelCustomArmor biped;
		public ModelRenderer partRender;

		public ModelCustom(ModelCustomArmor base, ModelRenderer renderer)
		{
			super(base);
			biped = base;
			partRender = renderer;
		}

		@Override
		public void render(float size)
		{
			if(ModelCustomArmor.this.modelType != null)
			{
				GL11.glPushMatrix();
				GL11.glTranslatef(0, 0, 0.06F);

				mc.renderEngine.bindTexture(modelType.resource);

				if(useModel(biped.modelType, partRender, biped))
				{
					if(biped.modelType == ArmorModel.JETPACK)
					{
						ArmorModel.jetpackModel.render(0.0625F);
					}
					else if(biped.modelType == ArmorModel.ARMOREDJETPACK)
					{
						ArmorModel.armoredJetpackModel.render(0.0625F);
					}
					else if(biped.modelType == ArmorModel.SCUBATANK)
					{
						ArmorModel.scubaTankModel.render(0.0625F);
					}
					else if(biped.modelType == ArmorModel.GASMASK)
					{
						GL11.glTranslatef(0, 0, -0.05F);
						ArmorModel.gasMaskModel.render(0.0625F);
					}
					else if(biped.modelType == ArmorModel.FREERUNNERS)
					{
						GL11.glScalef(1.02F, 1.02F, 1.02F);

						if(partRender == biped.bipedLeftLeg)
						{
							GL11.glTranslatef(-0.1375F, -0.75F, -0.0625F);
							ArmorModel.freeRunnersModel.renderLeft(0.0625F);
						}
						else if(partRender == biped.bipedRightLeg)
						{
							GL11.glTranslatef(0.1375F, -0.75F, -0.0625F);
							ArmorModel.freeRunnersModel.renderRight(0.0625F);
						}
					}
				}

				GL11.glPopMatrix();
			}
		}
	}

	@Override
	public void render(Entity entity, float par2, float par3, float par4, float par5, float par6, float par7)
	{
		init(entity, par2, par3, par4, par5, par6, par7);
		super.render(entity, par2, par3, par4, par5, par6, par7);
	}

	public static boolean useModel(ArmorModel type, ModelRenderer partRender, ModelCustomArmor biped)
	{
		if(type.armorSlot == 0)
		{
			return partRender == biped.bipedHead;
		}
		else if(type.armorSlot == 1)
		{
			return partRender == biped.bipedBody;
		}
		else if(type.armorSlot == 3)
		{
			return partRender == biped.bipedLeftLeg || partRender == biped.bipedRightLeg;
		}

		return false;
	}

	public static enum ArmorModel
	{
		JETPACK(1, MekanismUtils.getResource(ResourceType.RENDER, "Jetpack.png")),
		ARMOREDJETPACK(1, MekanismUtils.getResource(ResourceType.RENDER, "Jetpack.png")),
		SCUBATANK(1, MekanismUtils.getResource(ResourceType.RENDER, "ScubaSet.png")),
		GASMASK(0, MekanismUtils.getResource(ResourceType.RENDER, "ScubaSet.png")),
		FREERUNNERS(3, MekanismUtils.getResource(ResourceType.RENDER, "FreeRunners.png"));

		public int armorSlot;
		public ResourceLocation resource;

		public static ModelJetpack jetpackModel = new ModelJetpack();
		public static ModelArmoredJetpack armoredJetpackModel = new ModelArmoredJetpack();
		public static ModelGasMask gasMaskModel = new ModelGasMask();
		public static ModelScubaTank scubaTankModel = new ModelScubaTank();
		public static ModelFreeRunners freeRunnersModel = new ModelFreeRunners();

		private ArmorModel(int i, ResourceLocation r)
		{
			armorSlot = i;
			resource = r;
		}
	}

	public static ModelBiped getGlow(int index)
	{
		ModelBiped biped = index != 2 ? GLOW_BIG : GLOW_SMALL;

		biped.bipedHead.showModel = index == 0;
		biped.bipedHeadwear.showModel = index == 0;
		biped.bipedBody.showModel = index == 1 || index == 2;
		biped.bipedRightArm.showModel = index == 1;
		biped.bipedLeftArm.showModel = index == 1;
		biped.bipedRightLeg.showModel = index == 2 || index == 3;
		biped.bipedLeftLeg.showModel = index == 2 || index == 3;

		return biped;
	}

	public static class GlowArmor extends ModelBiped
	{
		public GlowArmor(float size)
		{
			super(size);
		}

		@Override
		public void render(Entity entity, float par2, float par3, float par4, float par5, float par6, float par7)
		{
			if(entity instanceof EntityLivingBase)
			{
				isSneak = ((EntityLivingBase)entity).isSneaking();
				isRiding = ((EntityLivingBase)entity).isRiding();
				isChild = ((EntityLivingBase)entity).isChild();
			}

			setRotationAngles(par2, par3, par4, par5, par6, par7, entity);

			MekanismRenderer.glowOn();
			super.render(entity, par2, par3, par4, par5, par6, par7);
			MekanismRenderer.glowOff();
		}
	}
}
