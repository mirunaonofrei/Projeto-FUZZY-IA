import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class FuzzyMain {
    public static void main(String[] args) {
        // Grupos de variáveis fuzzy para diferentes características
        GrupoVariaveis grupoVoteAverage = new GrupoVariaveis();
        grupoVoteAverage.add(new VariavelFuzzy("Nota Baixa", 0, 0, 4.5f, 5));
        grupoVoteAverage.add(new VariavelFuzzy("Nota Média", 4.3f, 5, 5.5f, 7.5f));
        grupoVoteAverage.add(new VariavelFuzzy("Nota Alta", 6.5f, 8f, 8.5f, 9));
        grupoVoteAverage.add(new VariavelFuzzy("Nota Fenomenal", 8.3f, 9.3f, 10, 10));

        GrupoVariaveis grupoVoteCount = new GrupoVariaveis();
        grupoVoteCount.add(new VariavelFuzzy("Poucos Votos", 0, 0, 1000, 1000));
        grupoVoteCount.add(new VariavelFuzzy("Média de Votos", 800, 1000, 9000, 10000));
        grupoVoteCount.add(new VariavelFuzzy("Muitos Votos", 9500, 11000, 15000, 15000));

        GrupoVariaveis grupoRuntime = new GrupoVariaveis();
        grupoRuntime.add(new VariavelFuzzy("Curto", 0, 0, 77, 80));
        grupoRuntime.add(new VariavelFuzzy("Médio", 75, 85, 95, 100));
        grupoRuntime.add(new VariavelFuzzy("Longo", 90, 110, 115, 120));
        grupoRuntime.add(new VariavelFuzzy("Muito Longo", 114, 120, 200, 200));

        GrupoVariaveis grupoReleaseDate = new GrupoVariaveis();
        grupoReleaseDate.add(new VariavelFuzzy("Antigo", 0, 0, 1991, 1995));
        grupoReleaseDate.add(new VariavelFuzzy("Menos Antigo", 1994, 1998, 2000, 2002));
        grupoReleaseDate.add(new VariavelFuzzy("Recente", 2001, 2004, 2009, 2015));
        grupoReleaseDate.add(new VariavelFuzzy("Muito Recente", 2013, 2016, 2025, 2025));

        try {
            BufferedReader bfr = new BufferedReader(new FileReader(new File("movie_dataset.csv")));
            String header = bfr.readLine();
            System.out.println("Header: " + header);
            String line;
            ArrayList<MovieScore> movieScores = new ArrayList<>();

            while ((line = bfr.readLine()) != null) {
                String[] spl = parseCSVLine(line);
                HashMap<String, Float> asVariaveis = new HashMap<>();

                if (spl.length < 19) {
                    continue;
                }

                String title = spl[18];
                String genres = spl[2];
                String keywords = spl[5];
                float voteAverage = !spl[19].isEmpty() ? Float.parseFloat(spl[19]) : 0.0f;
                int voteCount = !spl[20].isEmpty() ? Integer.parseInt(spl[20]) : 0;
                float runtime = !spl[14].isEmpty() ? Float.parseFloat(spl[14]) : 0.0f;
                int releaseYear = !spl[12].isEmpty() ? Integer.parseInt(spl[12].split("-")[0]) : 0;

                // Fuzzificação dos gêneros e palavras-chave
                fuzzificaGenero(genres, asVariaveis);
                fuzzificaKeywords(keywords, asVariaveis);
                grupoVoteAverage.fuzzifica(voteAverage, asVariaveis);
                grupoVoteCount.fuzzifica(voteCount, asVariaveis);
                grupoRuntime.fuzzifica(runtime, asVariaveis);
                grupoReleaseDate.fuzzifica(releaseYear, asVariaveis);

                rodaRegra(asVariaveis, "Nota Fenomenal", "Muitos Votos", "Excelente", "E");
                rodaRegra(asVariaveis, "Nota Fenomenal", "Muito Relevante", "Excelente", "E");
                rodaRegra(asVariaveis, "Nota Alta", "Muito Relevante", "Excelente", "E");
                rodaRegra(asVariaveis, "Nota Alta", "Relevante", "Excelente", "E");
                rodaRegra(asVariaveis, "Nota Alta", "Excelente", "Muito Recomendado", "E");
                rodaRegra(asVariaveis, "Nota Fenomenal", "Excelente", "Muito Recomendado", "E");
                rodaRegra(asVariaveis, "Nota Média", "Recente", "Recomendado", "E");
                rodaRegra(asVariaveis, "Nota Média", "Muitos Votos", "Recomendado", "E");
                rodaRegra(asVariaveis, "Nota Média", "Relevante", "Recomendado", "E");
                rodaRegra(asVariaveis, "Nota Média", "Muito Relevante", "Recomendado", "E");
                rodaRegra(asVariaveis, "Nota Alta", "Razoável", "Recomendado", "E");
                rodaRegra(asVariaveis, "Nota Média", "Excelente", "Interessante", "E");
                rodaRegra(asVariaveis, "Menos Antigo", "Razoável", "Interessante", "E");
                rodaRegra(asVariaveis, "Médio", "Relevante", "Interessante", "E");
                rodaRegra(asVariaveis, "Recente", "Média de Votos", "Interessante", "E");

                //regras com "OU"
                rodaRegra(asVariaveis, "Nota Alta", "Nota Fenomenal", "Excelente", "OU");
                rodaRegra(asVariaveis, "Muitos Votos", "Muito Relevante", "Muito Recomendado", "OU");

                //regras com "NOT"
                rodaRegra(asVariaveis, "Nota Baixa", "Nota Média", "Interessante", "NOT");
                rodaRegra(asVariaveis, "Antigo", "Recente", "Interessante", "NOT");

                // Penalizações
                float penalizacao = 1.0f;
                if (asVariaveis.getOrDefault("Nota Baixa", 0.0f) == 1.0f ||
                        asVariaveis.getOrDefault("Poucos Votos", 0.0f) == 1.0f ||
                        asVariaveis.getOrDefault("Antigo", 0.0f) == 1.0f ||
                        asVariaveis.getOrDefault("Muito Longo", 0.0f) == 1.0f) {
                    penalizacao = 0.5f;
                }

                // Cálculo final do score
                float score = calculaScore(asVariaveis) * penalizacao;

                movieScores.add(new MovieScore(title, score, asVariaveis));
            }

            bfr.close();

            // Ordenar filmes por score
            Collections.sort(movieScores, Comparator.comparingDouble(MovieScore::getScore).reversed());

            // Mostrar os top 10 filmes
            /*int topMoviesCount = Math.min(10, movieScores.size());
            for (int i = 0; i < topMoviesCount; i++) {
                MovieScore movie = movieScores.get(i);
                System.out.println("Filme: " + movie.getTitle());
                DecimalFormat df = new DecimalFormat("#.###");
                System.out.println("Score Final: " + df.format(movie.getScore()));
                System.out.println("------------------------------");
            }*/
            for (MovieScore movie : movieScores) { 
                System.out.println("Filme: " + movie.getTitle());
                DecimalFormat df = new DecimalFormat("#.###");
                System.out.println("Score Final: " + df.format(movie.getScore()));
                System.out.println("------------------------------");
            }
            

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Fuzzificação dos gêneros
    private static void fuzzificaGenero(String genres, HashMap<String, Float> asVariaveis) {
        String[] genreList = genres.split(" ");
        for (String genre : genreList) {
            if ("animation superhero romance".contains(genre)) {
                asVariaveis.put("Excelente", 1.0f); 
            } else if ("family comedy".contains(genre)) {
                asVariaveis.put("Relevante", 0.8f); 
            } else if ("action drama horror thriller".contains(genre)) {
                asVariaveis.put("Razoável", 0.5f); 
            }
        }
    }

    // Fuzzificação das palavras-chave
    private static void fuzzificaKeywords(String keywords, HashMap<String, Float> asVariaveis) {
        String[] keywordList = keywords.split(" ");
        for (String keyword : keywordList) {
            if ("hero love space adventure dream".contains(keyword)) {
                asVariaveis.put("Muito Relevante", 1.0f);
            } else if ("comedy life mystery".contains(keyword)) {
                asVariaveis.put("Relevante", 0.8f); 
            }
        }
    }

    // Regra de fuzzy E, OU ou NOT
    private static void rodaRegra(HashMap<String, Float> asVariaveis, String var1, String var2, String varr,
            String operacao) {
        float v1 = asVariaveis.getOrDefault(var1, 0.0f);
        float v2 = asVariaveis.getOrDefault(var2, 0.0f);

        float resultado = 0.0f;

        switch (operacao) {
            case "E": // AND
                resultado = Math.min(v1, v2);
                break;
            case "OU": // OR
                resultado = Math.max(v1, v2);
                break;
            case "NOT": // NOT
                resultado = 1.0f - v1; // Inverte o valor de var1 (considerando valores entre 0 e 1)
                break;
            default:
                throw new IllegalArgumentException("Operação não reconhecida: " + operacao);
        }

        // Armazena o resultado da regra no HashMap
        asVariaveis.put(varr, Math.max(asVariaveis.getOrDefault(varr, 0.0f), resultado));
    }

    // Cálculo do score baseado nas variáveis fuzzy
    private static float calculaScore(HashMap<String, Float> asVariaveis) {
        float recomendado = asVariaveis.getOrDefault("Recomendado", 0.0f);
        float excelente = asVariaveis.getOrDefault("Excelente", 0.0f);
        float muitoRelevante = asVariaveis.getOrDefault("Muito Relevante", 0.0f);
        float interessante = asVariaveis.getOrDefault("Interessante", 0.0f);

        // Cálculo do score final
        return (recomendado * 1.5f + excelente * 4.5f + muitoRelevante * 3 + interessante * 1);
    }

    // Função para parsear a linha CSV
    private static String[] parseCSVLine(String line) {
        return line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
    }

    // Classe para armazenar o score dos filmes
    static class MovieScore {
        private final String title;
        private final double score;
        private final HashMap<String, Float> fuzzyVariables;

        public MovieScore(String title, double score, HashMap<String, Float> fuzzyVariables) {
            this.title = title;
            this.score = score;
            this.fuzzyVariables = fuzzyVariables;
        }

        public String getTitle() {
            return title;
        }

        public double getScore() {
            return score;
        }

        public HashMap<String, Float> getFuzzyVariables() {
            return fuzzyVariables;
        }
    }
}