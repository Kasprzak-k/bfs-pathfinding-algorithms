import java.io.*;
import java.util.*;

public class LabirintoSolver {
    private static final char PAREDE = '#';
    private static final char CAMINHO = '.';
    private static final char INICIO = 'A';
    private static final char DESTINO = 'B';

    private static final int[][] DIRECOES = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    // Guarda só a posição mesmo
    private static class Posicao {
        final short x, y;

        Posicao(int x, int y) {
            this.x = (short) x;
            this.y = (short) y;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Posicao posicao = (Posicao) obj;
            return x == posicao.x && y == posicao.y;
        }

        @Override
        public int hashCode() {
            return x * 31 + y;
        }
    }

    public static void main(String[] args) {
        try {
            long startTime = System.currentTimeMillis();
            Runtime runtime = Runtime.getRuntime();

            char[][] labirinto = carregarLabirintoEficiente("caso4.txt");
            int distancia = encontrarDistanciaMinima(labirinto);

            long endTime = System.currentTimeMillis();
            long memoriaUsada = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);

            System.out.println("Tempo total: " + (endTime - startTime) + "ms");
            System.out.println("Memória usada: " + memoriaUsada + "MB");

            if (distancia != -1) {
                System.out.println("\nDeu bom! Caminho encontrado!");
                System.out.println("Distância mínima: " + distancia + " passos");
            } else {
                System.out.println("Deu ruim... Não achei caminho de A até B!");
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        } catch (OutOfMemoryError e) {
            System.err.println("Estourou a memória! Tentando outro approach...");
            tentarAbordagemAlternativa();
        }
    }

    private static char[][] carregarLabirintoEficiente(String nomeArquivo) throws IOException {
        System.out.println("Carregando labirinto...");

        // Primeiro vejo qual o tamanho da coisa
        int altura = 0;
        int largura = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                altura++;
                largura = Math.max(largura, linha.length());
            }
        }

        System.out.println("Tamanho do labirinto: " + altura + "x" + largura);

        // Agora sim carrego tudo
        char[][] labirinto = new char[altura][];

        try (BufferedReader br = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha;
            int i = 0;
            while ((linha = br.readLine()) != null) {
                labirinto[i] = linha.toCharArray();
                i++;
            }
        }

        return labirinto;
    }

    // BFS focado só na distância, sem guardar caminho completo
    private static int encontrarDistanciaMinima(char[][] labirinto) {
        System.out.println("Procurando caminho mais curto...");

        // Acho onde tão o A e o B
        Posicao inicio = null;
        Posicao destino = null;

        for (int i = 0; i < labirinto.length; i++) {
            char[] linha = labirinto[i];
            for (int j = 0; j < linha.length; j++) {
                if (linha[j] == INICIO) {
                    inicio = new Posicao(i, j);
                } else if (linha[j] == DESTINO) {
                    destino = new Posicao(i, j);
                }
            }
            if (inicio != null && destino != null) break; // Já achou os dois, pode parar
        }

        if (inicio == null || destino == null) {
            System.out.println("Não encontrei o A ou o B!");
            return -1;
        }

        System.out.println("Ponto A: (" + inicio.x + ", " + inicio.y + ")");
        System.out.println("Ponto B: (" + destino.x + ", " + destino.y + ")");

        return bfsApenasDistancia(labirinto, inicio, destino);
    }

    private static int bfsApenasDistancia(char[][] labirinto, Posicao inicio, Posicao destino) {
        Queue<Posicao> fila = new ArrayDeque<>();

        // Matriz de distâncias (uso short pra economizar)
        short[][] distancias = new short[labirinto.length][];
        for (int i = 0; i < labirinto.length; i++) {
            distancias[i] = new short[labirinto[i].length];
            Arrays.fill(distancias[i], (short) -1);
        }

        fila.offer(inicio);
        distancias[inicio.x][inicio.y] = 0;

        int nosExplorados = 0;
        int maxFilaSize = 0;

        while (!fila.isEmpty()) {
            Posicao atual = fila.poll();
            nosExplorados++;
            short distAtual = distancias[atual.x][atual.y];

            // Chegou no destino?
            if (atual.x == destino.x && atual.y == destino.y) {
                System.out.println("Cheguei no B!");
                System.out.println("Nós explorados: " + nosExplorados);
                System.out.println("Maior tamanho da fila: " + maxFilaSize);
                return distAtual;
            }

            // Vou ver os vizinhos
            for (int[] dir : DIRECOES) {
                int novoX = atual.x + dir[0];
                int novoY = atual.y + dir[1];

                if (ehValido(labirinto, novoX, novoY) && distancias[novoX][novoY] == -1) {
                    char celula = labirinto[novoX][novoY];
                    if (celula != PAREDE) {
                        distancias[novoX][novoY] = (short) (distAtual + 1);
                        fila.offer(new Posicao(novoX, novoY));
                    }
                }
            }

            maxFilaSize = Math.max(maxFilaSize, fila.size());

            // Só pra saber que tá rodando...
            if (nosExplorados % 1500000 == 0) {
                System.gc(); // Peço pro Java dar uma limpada
                System.out.println("Andamento: " + nosExplorados + " nós vistos, fila: " + fila.size());
            }
        }

        System.out.println("Não achei caminho depois de ver " + nosExplorados + " nós");
        return -1;
    }

    private static boolean ehValido(char[][] labirinto, int x, int y) {
        return x >= 0 && x < labirinto.length && y >= 0 && y < labirinto[x].length;
    }

    // Plan B pra labirintos gigantes
    private static void tentarAbordagemAlternativa() {
        try {
            System.out.println("Usando approach alternativo...");

            Posicao[] posicoes = encontrarPosicoesAB("caso7.txt");
            if (posicoes == null) return;

            Posicao inicio = posicoes[0];
            Posicao destino = posicoes[1];

            System.out.println("Ponto A: (" + inicio.x + ", " + inicio.y + ")");
            System.out.println("Ponto B: (" + destino.x + ", " + destino.y + ")");

            int distancia = bfsComStreaming("caso5.txt", inicio, destino);

            if (distancia != -1) {
                System.out.println("\nDeu certo! Caminho encontrado!");
                System.out.println("Distância mínima: " + distancia + " passos");
            } else {
                System.out.println("Não foi dessa vez...");
            }

        } catch (IOException e) {
            System.err.println("Erro no approach alternativo: " + e.getMessage());
        }
    }

    private static Posicao[] encontrarPosicoesAB(String nomeArquivo) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha;
            int linhaNum = 0;
            Posicao inicio = null;
            Posicao destino = null;

            while ((linha = br.readLine()) != null && (inicio == null || destino == null)) {
                for (int col = 0; col < linha.length(); col++) {
                    char c = linha.charAt(col);
                    if (c == INICIO) {
                        inicio = new Posicao(linhaNum, col);
                    } else if (c == DESTINO) {
                        destino = new Posicao(linhaNum, col);
                    }
                }
                linhaNum++;
            }

            if (inicio != null && destino != null) {
                return new Posicao[]{inicio, destino};
            }
        }
        return null;
    }

    // BFS que lê do arquivo na hora (pra casos extremos)
    private static int bfsComStreaming(String nomeArquivo, Posicao inicio, Posicao destino) throws IOException {
        // Descobrindo o tamanho primeiro
        int altura = 0;
        int largura = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                altura++;
                largura = Math.max(largura, linha.length());
            }
        }

        short[][] distancias = new short[altura][largura];
        for (int i = 0; i < altura; i++) {
            Arrays.fill(distancias[i], (short) -1);
        }

        Queue<Posicao> fila = new ArrayDeque<>();
        fila.offer(inicio);
        distancias[inicio.x][inicio.y] = 0;

        int nosExplorados = 0;

        while (!fila.isEmpty()) {
            Posicao atual = fila.poll();
            nosExplorados++;
            short distAtual = distancias[atual.x][atual.y];

            if (atual.equals(destino)) {
                System.out.println("Cheguei no destino!");
                return distAtual;
            }

            for (int[] dir : DIRECOES) {
                int novoX = atual.x + dir[0];
                int novoY = atual.y + dir[1];

                if (novoX >= 0 && novoX < altura && novoY >= 0 && novoY < largura &&
                        distancias[novoX][novoY] == -1) {

                    // Tenho que ler do arquivo pra ver o que tem aqui
                    char celula = lerCelula(nomeArquivo, novoX, novoY, largura);
                    if (celula != PAREDE && celula != ' ') {
                        distancias[novoX][novoY] = (short) (distAtual + 1);
                        fila.offer(new Posicao(novoX, novoY));
                    }
                }
            }
        }

        return -1;
    }

    private static char lerCelula(String nomeArquivo, int x, int y, int largura) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha = null;
            for (int i = 0; i <= x; i++) {
                linha = br.readLine();
            }
            if (linha != null && y < linha.length()) {
                return linha.charAt(y);
            }
        }
        return ' '; // Se tá fora do mapa
    }
}