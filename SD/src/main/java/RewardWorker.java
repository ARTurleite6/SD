import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.Condition;

public class RewardWorker implements Runnable {

    private final Condition updateRewards;
    private final Business business;

    public RewardWorker(@NotNull Business business) {
        this.updateRewards = business.getUpdateRewards();
        this.business = business;
    }

    @Override
    public void run() {
        try {
            this.business.pontolock.lock();
            while (true) {
                while (this.business.pontoAlterado == null) {
                    this.updateRewards.await();
                }

                var vizinhos = this.business.getPontosVizinhoPonto(this.business.pontoAlterado);
                for(var ponto : vizinhos)
                    System.out.println("Recompensa alterada no Ponto" + ponto + "?: " + this.business.atualizaRewardsPonto(ponto));
                this.business.pontoAlterado = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            this.business.pontolock.unlock();
        }
    }
}
