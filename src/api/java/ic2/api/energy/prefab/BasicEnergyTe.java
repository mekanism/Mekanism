package ic2.api.energy.prefab;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * BasicEnergyTe.Sink and BasicEnergyTe.Source allow integrating tile entities into the energy net
 * by using them as the super class.
 *
 * <p>They implement the necessary forwards to make BasicSink/BasicSource work, provided the sub
 * classes always call those forwards when overriding them.
 *
 * <p>If it's not desirable to use BasicEnergyTe, the delegates like BasicSink can also be used
 * directly, provided they receive the forwards like implemented here.
 *
 * <p>Example usage, machine with 10k EU buffer, tier 2, 50 EU/tick, 100 tick process time:
 * <pre><code>
 * public class MyMachine extends BasicEnergyTe.Sink implements ITickable {
 *     public MyMachine() {
 *         super(10000, 2);
 *     }
 *
 *     {@literal @}Override
 *     public void readFromNBT(NBTTagCompound nbt) {
 *         super.readFromNbt(nbt); // important: keep super call
 *
 *         this.progess = nbt.getInteger("progress");
 *     }
 *
 *     {@literal @}Override
 *     public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
 *         nbt = super.writeToNBT(nbt); // important: keep super call
 *
 *         nbt.setInteger("progress", progress);
 *
 *         return nbt;
 *     }
 *
 *     {@literal @}Override
 *     public void update() {
 *         if (hasWork() && getEnergyBuffer().useEnergy(50) && ++progress >= 100) {
 *             progress = 0;
 *             doWork();
 *         }
 *     }
 *
 *     private int progress;
 * }
 * </code></pre>
 */
public class BasicEnergyTe<T extends BasicEnergyTile> extends TileEntity {
	public static class Sink extends BasicEnergyTe<BasicSink> {
		public Sink(int capacity, int tier) {
			energyBuffer = new BasicSink(this, capacity, tier);
		}
	}

	public static class Source extends BasicEnergyTe<BasicSource> {
		public Source(int capacity, int tier) {
			energyBuffer = new BasicSource(this, capacity, tier);
		}
	}

	protected BasicEnergyTe() { }

	public T getEnergyBuffer() {
		return energyBuffer;
	}

	// forwards >>

	@Override
	public void onLoad() {
		energyBuffer.onLoad();
	}

	@Override
	public void invalidate() {
		super.invalidate();

		energyBuffer.invalidate();
	}

	@Override
	public void onChunkUnload() {
		energyBuffer.onChunkUnload();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		energyBuffer.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		return energyBuffer.writeToNBT(super.writeToNBT(nbt));
	}

	// << forwards

	protected T energyBuffer;
}
