package mekanism.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelTheoreticalElementizer extends ModelBase
{
    ModelRenderer A;
    ModelRenderer B;
    ModelRenderer C;
    ModelRenderer DROT;
    ModelRenderer E;
    ModelRenderer F;
    ModelRenderer G;
    ModelRenderer H;
    ModelRenderer I;
    ModelRenderer J;
    ModelRenderer K;
    ModelRenderer L;
    ModelRenderer M;
    ModelRenderer N;
    ModelRenderer O;
    ModelRenderer P;
    ModelRenderer Q;
    ModelRenderer R;

    public ModelTheoreticalElementizer()
    {
    	textureWidth = 128;
    	textureHeight = 128;

    	A = new ModelRenderer(this, 0, 0);
    	A.addBox(-8F, 0F, -8F, 16, 1, 16);
    	A.setRotationPoint(0F, 23F, 0F);
    	A.setTextureSize(64, 32);
    	A.mirror = true;
    	setRotation(A, 0F, 0F, 0F);
    	B = new ModelRenderer(this, 0, 40);
    	B.addBox(-5F, 0F, -4F, 10, 10, 8);
    	B.setRotationPoint(0F, 13F, 0F);
    	B.setTextureSize(64, 32);
    	B.mirror = true;
    	setRotation(B, 0F, 0F, 0F);
    	C = new ModelRenderer(this, 0, 19);
    	C.addBox(-3F, 0F, -3F, 6, 14, 6);
    	C.setRotationPoint(0F, 8F, 0F);
    	C.setTextureSize(64, 32);
    	C.mirror = true;
    	setRotation(C, 0F, 0F, 0F);
    	DROT = new ModelRenderer(this, 5, 0);
    	DROT.addBox(-1F, -1F, -1F, 2, 2, 2);
    	DROT.setRotationPoint(0F, 5F, 0F);
    	DROT.setTextureSize(64, 32);
    	DROT.mirror = true;
    	setRotation(DROT, 0.7853982F, 0.7853982F, 0.7853982F);
    	E = new ModelRenderer(this, 65, 0);
    	E.addBox(-2F, 0F, 2F, 4, 8, 7);
    	E.setRotationPoint(0F, 15F, 0F);
    	E.setTextureSize(64, 32);
    	E.mirror = true;
    	setRotation(E, 0F, 0.7853982F, 0F);
    	F = new ModelRenderer(this, 65, 0);
    	F.addBox(-2F, 0F, 2F, 4, 8, 7);
    	F.setRotationPoint(0F, 15F, 0F);
    	F.setTextureSize(64, 32);
    	F.mirror = true;
    	setRotation(F, 0F, -0.7853982F, 0F);
    	G = new ModelRenderer(this, 0, 0);
    	G.addBox(6F, 0F, 0F, 1, 4, 1);
    	G.setRotationPoint(0F, 19F, -2F);
    	G.setTextureSize(64, 32);
    	G.mirror = true;
    	setRotation(G, 0F, 0F, 0F);
    	H = new ModelRenderer(this, 0, 6);
    	H.addBox(5F, 0F, 0F, 1, 1, 1);
    	H.setRotationPoint(0F, 19F, -2F);
    	H.setTextureSize(64, 32);
    	H.mirror = true;
    	setRotation(H, 0F, 0F, 0F);
    	I = new ModelRenderer(this, 0, 6);
    	I.addBox(5F, 0F, 0F, 1, 1, 1);
    	I.setRotationPoint(0F, 19F, 0F);
    	I.setTextureSize(64, 32);
    	I.mirror = true;
    	setRotation(I, 0F, 0F, 0F);
    	J = new ModelRenderer(this, 0, 0);
    	J.addBox(6F, 0F, 0F, 1, 4, 1);
    	J.setRotationPoint(0F, 19F, 0F);
    	J.setTextureSize(64, 32);
    	J.mirror = true;
    	setRotation(J, 0F, 0F, 0F);
    	K = new ModelRenderer(this, 21, 61);
    	K.addBox(0F, -1F, -4F, 2, 9, 5);
    	K.setRotationPoint(-7F, 15F, 1F);
    	K.setTextureSize(64, 32);
    	K.mirror = true;
    	setRotation(K, 0F, 0F, 0F);
    	L = new ModelRenderer(this, 21, 77);
    	L.addBox(0F, -1F, -1F, 1, 2, 2);
    	L.setRotationPoint(-8F, 16F, 0F);
    	L.setTextureSize(64, 32);
    	L.mirror = true;
    	setRotation(L, 0F, 0F, 0F);
    	M = new ModelRenderer(this, 0, 61);
    	M.addBox(-4F, 0F, 0F, 8, 10, 0);
    	M.setRotationPoint(0F, 3F, 0F);
    	M.setTextureSize(64, 32);
    	M.mirror = true;
    	setRotation(M, 0F, 0.7853982F, 0F);
    	N = new ModelRenderer(this, 0, 73);
    	N.addBox(-4F, 0F, 0F, 8, 10, 0);
    	N.setRotationPoint(0F, 3F, 0F);
    	N.setTextureSize(64, 32);
    	N.mirror = true;
    	setRotation(N, 0F, -0.7853982F, 0F);
    	O = new ModelRenderer(this, 0, 93);
    	O.addBox(-5F, -5F, 0F, 6, 6, 0);
    	O.setRotationPoint(0F, 6F, 0F);
    	O.setTextureSize(64, 32);
    	O.mirror = true;
    	setRotation(O, 0F, -0.7853982F, 0.7853982F);
    	P = new ModelRenderer(this, 0, 85);
    	P.addBox(-5F, -5F, 0F, 6, 6, 0);
    	P.setRotationPoint(0F, 6F, 0F);
    	P.setTextureSize(64, 32);
    	P.mirror = true;
    	setRotation(P, 0F, 0.7853982F, 0.7853982F);
    	Q = new ModelRenderer(this, 65, 17);
    	Q.addBox(-4F, 0F, 0F, 8, 6, 4);
    	Q.setRotationPoint(0F, 17F, -8F);
    	Q.setTextureSize(64, 32);
    	Q.mirror = true;
    	setRotation(Q, 0F, 0F, 0F);
    	R = new ModelRenderer(this, 65, 28);
    	R.addBox(-4F, 0F, 0F, 8, 3, 5);
    	R.setRotationPoint(0F, 17F, -8F);
    	R.setTextureSize(64, 32);
    	R.mirror = true;
    	setRotation(R, 0.5934119F, 0F, 0F);
    }
    
    public void render(float size)
    {
    	A.render(size);
    	B.render(size);
    	C.render(size);
    	DROT.render(size);
    	E.render(size);
    	F.render(size);
    	G.render(size);
    	H.render(size);
    	I.render(size);
    	J.render(size);
    	K.render(size);
    	L.render(size);
    	M.render(size);
    	N.render(size);
    	O.render(size);
    	P.render(size);
    	Q.render(size);
    	R.render(size);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
    	super.render(entity, f, f1, f2, f3, f4, f5);
    	setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    	A.render(f5);
    	B.render(f5);
    	C.render(f5);
    	DROT.render(f5);
    	E.render(f5);
    	F.render(f5);
    	G.render(f5);
    	H.render(f5);
    	I.render(f5);
    	J.render(f5);
    	K.render(f5);
    	L.render(f5);
    	M.render(f5);
    	N.render(f5);
    	O.render(f5);
    	P.render(f5);
    	Q.render(f5);
    	R.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
    	model.rotateAngleX = x;
    	model.rotateAngleY = y;
    	model.rotateAngleZ = z;
    }
}