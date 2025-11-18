import java.io.*;
import java.util.*;

public class LabirintoSolver {
    private static final char PAREDE = '#';
    private static final char INICIO = 'A';
    private static final char DESTINO = 'B';

    private static class Posicao {
        int x, y, distancia;
        Posicao anterior;

        Posicao(int x, int y, int distancia, Posicao anterior) {
            this.x = x;
            this.y = y;
            this.distancia = distancia;
            this.anterior = anterior;
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
            return Objects.hash(x, y);
        }
    }

    public static void main(String[] args) {
        try {
            char[][] labirinto = carregarLabirinto("caso4.txt");
            List<Posicao> caminho = encontrarCaminhoMaisCurto(labirinto);

            if (caminho != null) {
                exibirResultadoResumido(labirinto, caminho);
            } else {
                System.out.println("Não foi possível encontrar um caminho de A até B!");
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }

    private static char[][] carregarLabirinto(String nomeArquivo) throws IOException {
        List<String> linhas = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                linhas.add(linha);
            }
        }

        int altura = linhas.size();
        int largura = linhas.get(0).length();

        char[][] labirinto = new char[altura][largura];
        for (int i = 0; i < altura; i++) {
            labirinto[i] = linhas.get(i).toCharArray();
        }

        return labirinto;
    }

    private static List<Posicao> encontrarCaminhoMaisCurto(char[][] labirinto) {
        // Encontrar posições de A e B
        Posicao inicio = null;
        Posicao destino = null;

        for (int i = 0; i < labirinto.length; i++) {
            for (int j = 0; j < labirinto[i].length; j++) {
                if (labirinto[i][j] == INICIO) {
                    inicio = new Posicao(i, j, 0, null);
                } else if (labirinto[i][j] == DESTINO) {
                    destino = new Posicao(i, j, 0, null);
                }
            }
        }

        if (inicio == null || destino == null) {
            System.out.println("Posições A ou B não encontradas!");
            return null;
        }

        System.out.println("Posição inicial A: (" + inicio.x + ", " + inicio.y + ")");
        System.out.println("Posição final B: (" + destino.x + ", " + destino.y + ")");

        // BFS para encontrar o caminho mais curto
        Queue<Posicao> fila = new LinkedList<>();
        Set<Posicao> visitados = new HashSet<>();
        int[][] direcoes = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        fila.offer(inicio);
        visitados.add(inicio);

        int nosExplorados = 0;

        while (!fila.isEmpty()) {
            Posicao atual = fila.poll();
            nosExplorados++;

            // Verificar se chegou ao destino
            if (atual.x == destino.x && atual.y == destino.y) {
                System.out.println("Posições exploradas: " + nosExplorados);

                // Reconstruir o caminho
                List<Posicao> caminho = new ArrayList<>();
                while (atual != null) {
                    caminho.add(0, atual);
                    atual = atual.anterior;
                }
                return caminho;
            }

            // Explorar vizinhos
            for (int[] dir : direcoes) {
                int novoX = atual.x + dir[0];
                int novoY = atual.y + dir[1];

                if (novoX >= 0 && novoX < labirinto.length &&
                        novoY >= 0 && novoY < labirinto[0].length) {

                    char celula = labirinto[novoX][novoY];
                    if (celula != PAREDE) {
                        Posicao vizinho = new Posicao(novoX, novoY, atual.distancia + 1, atual);

                        if (!visitados.contains(vizinho)) {
                            visitados.add(vizinho);
                            fila.offer(vizinho);
                        }
                    }
                }
            }
        }

        return null;
    }

    private static void exibirResultadoResumido(char[][] labirinto, List<Posicao> caminho) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("CAMINHO ENCONTRADO!");
        System.out.println("=".repeat(50));

        // Informações principais
        System.out.println("Resumo:");
        System.out.println("  - Distância percorrida: " + (caminho.size() - 1) + " passos");
        System.out.println("  - Posições no caminho: " + caminho.size());
        System.out.println("  - Tamanho do labirinto: " + labirinto.length + "x" + labirinto[0].length);

        // Primeiras e ultimas coordenadas
        System.out.println("\nTrajetória (primeiros 10 passos):");
        for (int i = 0; i < Math.min(10, caminho.size()); i++) {
            Posicao pos = caminho.get(i);
            System.out.println("  " + (i + 1) + ". (" + pos.x + ", " + pos.y + ")");
        }

        if (caminho.size() > 10) {
            System.out.println("  ...");
            System.out.println("  " + caminho.size() + ". (" + caminho.get(caminho.size()-1).x + ", " + caminho.get(caminho.size()-1).y + ")");
        }

        // Visualizaçao mais compacta do caminho
        System.out.println("\nVisualização do caminho:");
        exibirVisualizacaoCompacta(labirinto, caminho);

        System.out.println("\nAs coordenadas completas estão disponíveis no código.");
    }

    private static void exibirVisualizacaoCompacta(char[][] labirinto, List<Posicao> caminho) {
        // Encontrar limites do caminho para mostrar uma area relevante
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

        for (Posicao pos : caminho) {
            minX = Math.min(minX, pos.x);
            maxX = Math.max(maxX, pos.x);
            minY = Math.min(minY, pos.y);
            maxY = Math.max(maxY, pos.y);
        }

        // Expandir um pouco os limites para dar mais contexto
        int margem = 5;
        minX = Math.max(0, minX - margem);
        maxX = Math.min(labirinto.length - 1, maxX + margem);
        minY = Math.max(0, minY - margem);
        maxY = Math.min(labirinto[0].length - 1, maxY + margem);

        // Cria conjunto de posiçoes do caminho para busca rapida
        Set<String> posicoesCaminho = new HashSet<>();
        for (Posicao pos : caminho) {
            posicoesCaminho.add(pos.x + "," + pos.y);
        }

        System.out.println("Área visualizada: linhas " + minX + "-" + maxX + ", colunas " + minY + "-" + maxY);
        System.out.println();

        // Mostra apenas a area relevante
        for (int i = minX; i <= maxX; i++) {
            System.out.print("  ");
            for (int j = minY; j <= maxY; j++) {
                if (posicoesCaminho.contains(i + "," + j)) {
                    if (labirinto[i][j] == INICIO) {
                        System.out.print("A");
                    } else if (labirinto[i][j] == DESTINO) {
                        System.out.print("B");
                    } else {
                        System.out.print("X");
                    }
                } else {
                    System.out.print(labirinto[i][j]);
                }
            }
            System.out.println();
        }

        System.out.println("\nLegenda: A → X → B  (caminho) | # (parede) | . (espaço livre)");
    }
}