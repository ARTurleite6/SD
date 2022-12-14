public class Business {
    private final int N = 20;
    private final int D = 2;
    private final Parque[][] mapa;

    public Business() {
        this.mapa = new Parque[N][N];
        this.loadMap();
    }

    private void loadMap() {
        for(int y = 0; y < N; ++y) {
            for(int x = 0; x < N; ++x) {
                this.mapa[y][x] = new Parque(new Ponto(x, y), D, N);
            }
        }
    }
}
