import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class FuzzyMain2 {
    public static void main(String[] args) {
        // Configuração das variáveis fuzzy
        GrupoVariaveis grupoVoteAverage = new GrupoVariaveis();
        grupoVoteAverage.add(new VariavelFuzzy("Nota Baixa", 0, 0, 5, 5));
        grupoVoteAverage.add(new VariavelFuzzy("Nota Média", 4, 5, 6, 7.5f));
        grupoVoteAverage.add(new VariavelFuzzy("Nota Alta", 7, 7.5f, 8.5f, 9));
        grupoVoteAverage.add(new VariavelFuzzy("Nota Fenomenal", 8.5f, 9, 10, 10));

        GrupoVariaveis grupoVoteCount = new GrupoVariaveis();
        grupoVoteCount.add(new VariavelFuzzy("Poucos Votos", 0, 0, 1000, 1000));
        grupoVoteCount.add(new VariavelFuzzy("Média de Votos", 800, 1000, 9000, 10000));
        grupoVoteCount.add(new VariavelFuzzy("Muitos Votos", 9500, 11000, 15000, 15000));

        GrupoVariaveis grupoRuntime = new GrupoVariaveis();
        grupoRuntime.add(new VariavelFuzzy("Curto", 0, 0, 80, 80));
        grupoRuntime.add(new VariavelFuzzy("Médio", 70, 80, 90, 100));
        grupoRuntime.add(new VariavelFuzzy("Longo", 90, 110, 115, 120));
        grupoRuntime.add(new VariavelFuzzy("Muito Longo", 116, 120, 200, 200));

        GrupoVariaveis grupoReleaseDate = new GrupoVariaveis();
        grupoReleaseDate.add(new VariavelFuzzy("Antigo", 0, 0, 1991, 1995));
        grupoReleaseDate.add(new VariavelFuzzy("Menos Antigo", 1994, 1998, 2000, 2002));
        grupoReleaseDate.add(new VariavelFuzzy("Recente", 2001, 2004, 2009, 2015));
        grupoReleaseDate.add(new VariavelFuzzy("Muito Recente", 2013, 2016, 2025, 2025));

        try {
            BufferedReader bfr = new BufferedReader(new FileReader(new File("C:\\Users\\Usuario\\Documents\\GitHub\\Projeto-FUZZY-IA\\FUZZY\\movie_dataset.csv")));
            String header = bfr.readLine(); // Lê o cabeçalho
            System.out.println("Header: " + header);

            String line;
            ArrayList<MovieScore> movieScores = new ArrayList<>();

            while ((line = bfr.readLine()) != null) {
                String[] spl = parseCSVLine(line); // Processa a linha corretamente
                HashMap<String, Float> asVariaveis = new HashMap<>();

                // Verificar se há colunas suficientes
                if (spl.length < 19) {
                    continue;
                }

                // Extração de campos
                String title = spl[18]; // Coluna "title"
                String genres = spl[2]; // Coluna "genres"
                String keywords = spl[5]; // Coluna "keywords"
                float voteAverage = !spl[19].isEmpty() ? Float.parseFloat(spl[19]) : 0.0f;
                int voteCount = !spl[20].isEmpty() ? Integer.parseInt(spl[20]) : 0;
                float runtime = !spl[14].isEmpty() ? Float.parseFloat(spl[14]) : 0.0f;
                int releaseYear = !spl[12].isEmpty() ? Integer.parseInt(spl[12].split("-")[0]) : 0;

                // Fuzzificação
                fuzzificaGenero(genres, asVariaveis);
                fuzzificaKeywords(keywords, asVariaveis);
                grupoVoteAverage.fuzzifica(voteAverage, asVariaveis);
                grupoVoteCount.fuzzifica(voteCount, asVariaveis);
                grupoRuntime.fuzzifica(runtime, asVariaveis);
                grupoReleaseDate.fuzzifica(releaseYear, asVariaveis);

                // Regras de inferência mais abrangentes
                rodaRegraE(asVariaveis, "Nota Média", "Recente", "Recomendado");
                rodaRegraE(asVariaveis, "Nota Alta", "Muito Relevante", "Excelente");
                rodaRegraE(asVariaveis, "Nota Fenomenal", "Muitos Votos", "Excelente");
                rodaRegraE(asVariaveis, "Nota Média", "Muitos Votos", "Recomendado");

                // Score Final com valor base
                float recomendado = asVariaveis.getOrDefault("Recomendado", 0.1f); // Valor base para evitar 0
                float excelente = asVariaveis.getOrDefault("Excelente", 0.1f); // Valor base para evitar 0
                float muitoRelevante = asVariaveis.getOrDefault("Muito Relevante", 0.0f);
                float score = (recomendado * 5 + excelente * 10 + muitoRelevante * 3) /
                        (recomendado + excelente + muitoRelevante + 1e-6f);

                // Adiciona o filme e seu score à lista
                movieScores.add(new MovieScore(title, score));
            }
            bfr.close();

            // Ordena os filmes pelo score em ordem decrescente
            Collections.sort(movieScores, Comparator.comparingDouble(MovieScore::getScore).reversed());

            // Exibe os filmes ordenados
            for (MovieScore movie : movieScores) {
                System.out.println("Filme: " + movie.getTitle());
                System.out.println("Score Final: " + movie.getScore());
                System.out.println("------------------------------");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void fuzzificaGenero(String genres, HashMap<String, Float> asVariaveis) {
        String[] genreList = genres.split(" ");
        for (String genre : genreList) {
            if ("horror fantasy animation comedy romance thriller crime".contains(genre)) {
                asVariaveis.put("Excelente", 1.0f);
            } else if ("family drama romance fantasy science fiction adventure".contains(genre)) {
                asVariaveis.put("Razoável", 1.0f);
            } else if ("western war documentary music action".contains(genre)) {
                asVariaveis.put("Péssimo", 1.0f);
            }
        }
    }

    private static void fuzzificaKeywords(String keywords, HashMap<String, Float> asVariaveis) {
        String[] keywordList = keywords.split(" ");
        for (String keyword : keywordList) {
            if ("space pop video game nightmare mother cat plot twist revenge relationship murder lover dream confession wife".contains(keyword)) {
                asVariaveis.put("Muito Relevante", 1.0f);
            } else if ("hero woman independent pirate comedy life new detective corruption serial killer earthquake jealousy mother witness".contains(keyword)) {
                asVariaveis.put("Relevante", 1.0f);
            } else if ("car american football horse surfing rape usa pornography sport musical prisoner military service virgin gore prison prostitute sex".contains(keyword)) {
                asVariaveis.put("Pouco Relevante", 1.0f);
            }
        }
    }

    private static void rodaRegraE(HashMap<String, Float> asVariaveis, String var1, String var2, String varr) {
        float v = Math.min(asVariaveis.getOrDefault(var1, 0.0f), asVariaveis.getOrDefault(var2, 0.0f));
        asVariaveis.put(varr, Math.max(asVariaveis.getOrDefault(varr, 0.0f), v));
    }

    private static String[] parseCSVLine(String line) {
        return line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
    }

    // Classe auxiliar para armazenar título e score
    static class MovieScore {
        private final String title;
        private final double score;

        public MovieScore(String title, double score) {
            this.title = title;
            this.score = score;
        }

        public String getTitle() {
            return title;
        }

        public double getScore() {
            return score;
        }
    }
}