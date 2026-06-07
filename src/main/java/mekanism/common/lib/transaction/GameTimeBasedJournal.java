package mekanism.common.lib.transaction;

import java.util.function.LongSupplier;
import mekanism.api.annotations.NothingNullByDefault;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public abstract class GameTimeBasedJournal<TYPE extends @Nullable Object> extends SnapshotJournal<TYPE> {

    private final LongSupplier gameTimeSupplier;
    private long currentGameTime;

    public GameTimeBasedJournal(LongSupplier gameTimeSupplier) {
        this.gameTimeSupplier = gameTimeSupplier;
    }

    protected void tickChanged(long gameTime) {
        currentGameTime = gameTime;
    }

    @Override
    public void updateSnapshots(TransactionContext transaction) {
        //Check if the tick changed before updating the snapshots
        checkTickChanged();
        super.updateSnapshots(transaction);
    }

    public void checkTickChanged() {
        long gameTime = gameTimeSupplier.getAsLong();
        if (currentGameTime != gameTime) {
            //If the tick is different from what the last cached tick was, we need to force mark that the tick changed before doing any transaction based handling
            tickChanged(gameTime);
        }
    }
}