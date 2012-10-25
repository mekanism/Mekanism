package universalelectricity.prefab.ore;

import java.util.Random;

import net.minecraft.src.ChunkProviderEnd;
import net.minecraft.src.ChunkProviderGenerate;
import net.minecraft.src.ChunkProviderHell;
import net.minecraft.src.IChunkProvider;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MathHelper;
import net.minecraft.src.World;

/**
 * This class is used for storing ore generation data. If you are too lazy to
 * generate your own ores, you can do
 * {@link #OreGenerator.ORES_TO_GENERATE.add()} to add your ore to the list of
 * ores to generate.
 * 
 * @author Calclavia
 * 
 */
public class OreGenReplace extends OreGenBase
{

	public int minGenerateLevel;
	public int maxGenerateLevel;
	public int amountPerChunk;
	public int amountPerBranch;
	public int replaceID;

	public boolean generateSurface;
	public boolean generateNether;
	public boolean generateEnd;

	/**
	 * @param name
	 *            - The name of the ore for display
	 * @param textureFile
	 *            - The 16x16 png texture of your ore to override
	 * @param minGenerateLevel
	 *            - The highest generation level of your ore
	 * @param maxGenerateLevel
	 *            - The lowest generation level of your ore
	 * @param amountPerChunk
	 *            - The amount of ores to generate per chunk
	 * @param amountPerBranch
	 *            - The amount of ores to generate in a clutter. E.g coal
	 *            generates with a lot of other coal next to it. How much do you
	 *            want?
	 */
	public OreGenReplace(String name, String oreDiectionaryName, ItemStack stack, int replaceID, int minGenerateLevel, int maxGenerateLevel, int amountPerChunk, int amountPerBranch, String harvestTool, int harvestLevel)
	{
		super(name, oreDiectionaryName, stack, harvestTool, harvestLevel);
		this.minGenerateLevel = minGenerateLevel;
		this.maxGenerateLevel = maxGenerateLevel;
		this.amountPerChunk = amountPerChunk;
		this.amountPerBranch = amountPerBranch;
		this.replaceID = replaceID;
	}

	public void generate(World world, Random random, int varX, int varZ)
	{

		for (int i = 0; i < this.amountPerChunk; i++)
		{
			int x = varX + random.nextInt(16);
			int z = varZ + random.nextInt(16);
			int y = random.nextInt(this.maxGenerateLevel - this.minGenerateLevel) + this.minGenerateLevel;
			generateReplace(world, random, x, y, z);
		}
	}

	public boolean generateReplace(World par1World, Random par2Random, int par3, int par4, int par5)
	{
		float var6 = par2Random.nextFloat() * (float) Math.PI;
		double var7 = (double) ((float) (par3 + 8) + MathHelper.sin(var6) * (float) this.amountPerBranch / 8.0F);
		double var9 = (double) ((float) (par3 + 8) - MathHelper.sin(var6) * (float) this.amountPerBranch / 8.0F);
		double var11 = (double) ((float) (par5 + 8) + MathHelper.cos(var6) * (float) this.amountPerBranch / 8.0F);
		double var13 = (double) ((float) (par5 + 8) - MathHelper.cos(var6) * (float) this.amountPerBranch / 8.0F);
		double var15 = (double) (par4 + par2Random.nextInt(3) - 2);
		double var17 = (double) (par4 + par2Random.nextInt(3) - 2);

		for (int var19 = 0; var19 <= this.amountPerBranch; ++var19)
		{
			double var20 = var7 + (var9 - var7) * (double) var19 / (double) this.amountPerBranch;
			double var22 = var15 + (var17 - var15) * (double) var19 / (double) this.amountPerBranch;
			double var24 = var11 + (var13 - var11) * (double) var19 / (double) this.amountPerBranch;
			double var26 = par2Random.nextDouble() * (double) this.amountPerBranch / 16.0D;
			double var28 = (double) (MathHelper.sin((float) var19 * (float) Math.PI / (float) this.amountPerBranch) + 1.0F) * var26 + 1.0D;
			double var30 = (double) (MathHelper.sin((float) var19 * (float) Math.PI / (float) this.amountPerBranch) + 1.0F) * var26 + 1.0D;
			int var32 = MathHelper.floor_double(var20 - var28 / 2.0D);
			int var33 = MathHelper.floor_double(var22 - var30 / 2.0D);
			int var34 = MathHelper.floor_double(var24 - var28 / 2.0D);
			int var35 = MathHelper.floor_double(var20 + var28 / 2.0D);
			int var36 = MathHelper.floor_double(var22 + var30 / 2.0D);
			int var37 = MathHelper.floor_double(var24 + var28 / 2.0D);

			for (int var38 = var32; var38 <= var35; ++var38)
			{
				double var39 = ((double) var38 + 0.5D - var20) / (var28 / 2.0D);

				if (var39 * var39 < 1.0D)
				{
					for (int var41 = var33; var41 <= var36; ++var41)
					{
						double var42 = ((double) var41 + 0.5D - var22) / (var30 / 2.0D);

						if (var39 * var39 + var42 * var42 < 1.0D)
						{
							for (int var44 = var34; var44 <= var37; ++var44)
							{
								double var45 = ((double) var44 + 0.5D - var24) / (var28 / 2.0D);

								int block = par1World.getBlockId(var38, var41, var44);
								if (var39 * var39 + var42 * var42 + var45 * var45 < 1.0D && (this.replaceID == 0 || block == this.replaceID))
								{
									par1World.setBlockAndMetadata(var38, var41, var44, this.oreID, this.oreMeta);
								}
							}
						}
					}
				}
			}
		}

		return true;
	}

	@Override
	public boolean isOreGeneratedInWorld(World world, IChunkProvider chunkGenerator)
	{
		return ((this.generateSurface && chunkGenerator instanceof ChunkProviderGenerate) || (this.generateNether && chunkGenerator instanceof ChunkProviderHell) || (this.generateEnd && chunkGenerator instanceof ChunkProviderEnd));
	}
}
