package mekanism.client.model;

import mekanism.client.render.MekanismRenderer;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelChemicalOxidizer extends ModelBase
{
	ModelRenderer Base;
	ModelRenderer TSSW;
	ModelRenderer TSNW;
	ModelRenderer TSSE;
	ModelRenderer TSNE;
	ModelRenderer TTN;
	ModelRenderer TTS;
	ModelRenderer TTE;
	ModelRenderer TTW;
	ModelRenderer Connection;
	ModelRenderer ItemEntry;
	ModelRenderer GasExit;
	ModelRenderer GasConnection;
	ModelRenderer Machine;
	ModelRenderer Post;
	ModelRenderer GlassN;
	ModelRenderer GlassS;
	ModelRenderer GlassW;
	ModelRenderer GlassE;
	ModelRenderer GlassU;

	public ModelChemicalOxidizer()
	{
		textureWidth = 128;
		textureHeight = 128;

		Base = new ModelRenderer(this, 0, 0);
		Base.addBox(0F, 0F, 0F, 16, 1, 16);
		Base.setRotationPoint(-8F, 23F, -8F);
		Base.setTextureSize(128, 128);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		TSSW = new ModelRenderer(this, 0, 17);
		TSSW.addBox(0F, 0F, 0F, 1, 15, 1);
		TSSW.setRotationPoint(-1F, 8F, 3F);
		TSSW.setTextureSize(128, 128);
		TSSW.mirror = true;
		setRotation(TSSW, 0F, 0F, 0F);
		TSNW = new ModelRenderer(this, 0, 17);
		TSNW.addBox(0F, 0F, 0F, 1, 15, 1);
		TSNW.setRotationPoint(-1F, 8F, -4F);
		TSNW.setTextureSize(128, 128);
		TSNW.mirror = true;
		setRotation(TSNW, 0F, 0F, 0F);
		TSSE = new ModelRenderer(this, 0, 17);
		TSSE.addBox(0F, 0F, 0F, 1, 15, 1);
		TSSE.setRotationPoint(6F, 8F, 3F);
		TSSE.setTextureSize(128, 128);
		TSSE.mirror = true;
		setRotation(TSSE, 0F, 0F, 0F);
		TSNE = new ModelRenderer(this, 0, 17);
		TSNE.addBox(0F, 0F, 0F, 1, 15, 1);
		TSNE.setRotationPoint(6F, 8F, -4F);
		TSNE.setTextureSize(128, 128);
		TSNE.mirror = true;
		setRotation(TSNE, 0F, 0F, 0F);
		TTN = new ModelRenderer(this, 4, 17);
		TTN.addBox(0F, 0F, 0F, 6, 1, 1);
		TTN.setRotationPoint(0F, 8F, -4F);
		TTN.setTextureSize(128, 128);
		TTN.mirror = true;
		setRotation(TTN, 0F, 0F, 0F);
		TTS = new ModelRenderer(this, 4, 17);
		TTS.addBox(0F, 0F, 0F, 6, 1, 1);
		TTS.setRotationPoint(0F, 8F, 3F);
		TTS.setTextureSize(128, 128);
		TTS.mirror = true;
		setRotation(TTS, 0F, 0F, 0F);
		TTE = new ModelRenderer(this, 18, 17);
		TTE.addBox(0F, 0F, 0F, 1, 1, 6);
		TTE.setRotationPoint(6F, 8F, -3F);
		TTE.setTextureSize(128, 128);
		TTE.mirror = true;
		setRotation(TTE, 0F, 0F, 0F);
		TTW = new ModelRenderer(this, 18, 17);
		TTW.addBox(0F, 0F, 0F, 1, 1, 6);
		TTW.setRotationPoint(-1F, 8F, -3F);
		TTW.setTextureSize(128, 128);
		TTW.mirror = true;
		setRotation(TTW, 0F, 0F, 0F);
		Connection = new ModelRenderer(this, 32, 17);
		Connection.addBox(0F, 0F, 0F, 7, 4, 4);
		Connection.setRotationPoint(-7F, 14F, -2F);
		Connection.setTextureSize(128, 128);
		Connection.mirror = true;
		setRotation(Connection, 0F, 0F, 0F);
		ItemEntry = new ModelRenderer(this, 64, 0);
		ItemEntry.addBox(0F, 0F, 0F, 1, 8, 8);
		ItemEntry.setRotationPoint(-8F, 12F, -4F);
		ItemEntry.setTextureSize(128, 128);
		ItemEntry.mirror = true;
		setRotation(ItemEntry, 0F, 0F, 0F);
		GasExit = new ModelRenderer(this, 0, 33);
		GasExit.addBox(0F, 1F, 0F, 1, 6, 6);
		GasExit.setRotationPoint(7F, 12F, -3F);
		GasExit.setTextureSize(128, 128);
		GasExit.mirror = true;
		setRotation(GasExit, 0F, 0F, 0F);
		GasConnection = new ModelRenderer(this, 82, 0);
		GasConnection.addBox(0F, 1F, 0F, 1, 4, 4);
		GasConnection.setRotationPoint(6F, 13F, -2F);
		GasConnection.setTextureSize(128, 128);
		GasConnection.mirror = true;
		setRotation(GasConnection, 0F, 0F, 0F);
		Machine = new ModelRenderer(this, 0, 45);
		Machine.addBox(0F, 0F, 0F, 4, 6, 6);
		Machine.setRotationPoint(-6F, 13F, -3F);
		Machine.setTextureSize(128, 128);
		Machine.mirror = true;
		setRotation(Machine, 0F, 0F, 0F);
		Post = new ModelRenderer(this, 0, 57);
		Post.addBox(0F, 0F, 0F, 2, 4, 2);
		Post.setRotationPoint(-5F, 19F, -1F);
		Post.setTextureSize(128, 128);
		Post.mirror = true;
		setRotation(Post, 0F, 0F, 0F);
		GlassN = new ModelRenderer(this, 92, 0);
		GlassN.addBox(0F, 0F, 0F, 6, 14, 1);
		GlassN.setRotationPoint(0F, 9F, -4F);
		GlassN.setTextureSize(128, 128);
		GlassN.mirror = true;
		setRotation(GlassN, 0F, 0F, 0F);
		GlassS = new ModelRenderer(this, 92, 0);
		GlassS.addBox(0F, 0F, 0F, 6, 14, 1);
		GlassS.setRotationPoint(0F, 9F, 3F);
		GlassS.setTextureSize(128, 128);
		GlassS.mirror = true;
		setRotation(GlassS, 0F, 0F, 0F);
		GlassW = new ModelRenderer(this, 0, 70);
		GlassW.addBox(0F, 0F, 0F, 1, 14, 6);
		GlassW.setRotationPoint(-1F, 9F, -3F);
		GlassW.setTextureSize(128, 128);
		GlassW.mirror = true;
		setRotation(GlassW, 0F, 0F, 0F);
		GlassE = new ModelRenderer(this, 0, 70);
		GlassE.addBox(0F, 0F, 0F, 1, 14, 6);
		GlassE.setRotationPoint(6F, 9F, -3F);
		GlassE.setTextureSize(128, 128);
		GlassE.mirror = true;
		setRotation(GlassE, 0F, 0F, 0F);
		GlassU = new ModelRenderer(this, 0, 63);
		GlassU.addBox(0F, 0F, 0F, 6, 1, 6);
		GlassU.setRotationPoint(0F, 8F, -3F);
		GlassU.setTextureSize(128, 128);
		GlassU.mirror = true;
		setRotation(GlassU, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		Base.render(size);
		TSSW.render(size);
		TSNW.render(size);
		TSSE.render(size);
		TSNE.render(size);
		TTN.render(size);
		TTS.render(size);
		TTE.render(size);
		TTW.render(size);
		Connection.render(size);
		ItemEntry.render(size);
		GasExit.render(size);
		GasConnection.render(size);
		Machine.render(size);
		Post.render(size);
	}

	public void renderGlass(float size)
	{
		GL11.glPushMatrix();
		MekanismRenderer.blendOn();
		GL11.glColor4f(1, 1, 1, 0.2F);

		GlassN.render(size);
		GlassS.render(size);
		GlassW.render(size);
		GlassE.render(size);
		GlassU.render(size);

		MekanismRenderer.blendOff();
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glPopMatrix();
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}