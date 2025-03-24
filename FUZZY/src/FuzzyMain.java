import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class FuzzyMain {
    public static void main(String[] args) {
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

                fuzzificaGenero(genres, asVariaveis);
                fuzzificaKeywords(keywords, asVariaveis);
                grupoVoteAverage.fuzzifica(voteAverage, asVariaveis);
                grupoVoteCount.fuzzifica(voteCount, asVariaveis);
                grupoRuntime.fuzzifica(runtime, asVariaveis);
                grupoReleaseDate.fuzzifica(releaseYear, asVariaveis);

                rodaRegraE(asVariaveis, "Nota Média", "Recente", "Recomendado");
                rodaRegraE(asVariaveis, "Nota Alta", "Muito Relevante", "Excelente");
                rodaRegraE(asVariaveis, "Nota Fenomenal", "Muitos Votos", "Excelente");
                rodaRegraE(asVariaveis, "Nota Média", "Muitos Votos", "Recomendado");
                rodaRegraE(asVariaveis, "Nota Alta", "Relevante", "Excelente");
                rodaRegraE(asVariaveis, "Nota Média", "Relevante", "Recomendado");
                rodaRegraE(asVariaveis, "Nota Média", "Muito Relevante", "Recomendado");
                rodaRegraE(asVariaveis, "Nota Alta", "Excelente", "Muito Recomendado");
                rodaRegraE(asVariaveis, "Nota Fenomenal", "Excelente", "Muito Recomendado");
                rodaRegraE(asVariaveis, "Nota Média", "Excelente", "Interessante");
                rodaRegraE(asVariaveis, "Recente", "Muitos Votos", "Muito Recomendado");
                rodaRegraE(asVariaveis, "Nota Alta", "Muito Longo", "Excelente");
                rodaRegraE(asVariaveis, "Nota Fenomenal", "Recente", "Excelente");
                rodaRegraE(asVariaveis, "Menos Antigo", "Razoável", "Interessante");
                rodaRegraE(asVariaveis, "Nota Alta", "Razoável", "Recomendado");
                rodaRegraE(asVariaveis, "Nota Alta", "Recente", "Excelente");
                rodaRegraE(asVariaveis, "Médio", "Relevante", "Interessante");
                rodaRegraE(asVariaveis, "Nota Alta", "Muito Longo", "Excelente");
                rodaRegraE(asVariaveis, "Nota Baixa", "Curto", "Não Recomendado");
                rodaRegraE(asVariaveis, "Recente", "Média de Votos", "Interessante");
                rodaRegraE(asVariaveis, "Nota Alta", "Ação", "Recomendado");
                rodaRegraE(asVariaveis, "Antigo", "Média de Votos", "Razoável");

                float recomendado = asVariaveis.getOrDefault("Recomendado", 0.0f);
                float excelente = asVariaveis.getOrDefault("Excelente", 0.0f);
                float muitoRelevante = asVariaveis.getOrDefault("Muito Recomendado", 0.0f);
                float interessante = asVariaveis.getOrDefault("Interessante", 0.0f);

                float score = (recomendado * 3 + excelente * 10 + muitoRelevante * 5 + interessante * 2) /
                        (recomendado + excelente + muitoRelevante + interessante + 1e-6f);

                movieScores.add(new MovieScore(title, score, asVariaveis));

            }
            bfr.close();

            Collections.sort(movieScores, Comparator.comparingDouble(MovieScore::getScore).reversed());

            int topMoviesCount = Math.min(10, movieScores.size());
            for (int i = 0; i < topMoviesCount; i++) {
                MovieScore movie = movieScores.get(i);
                System.out.println("Filme: " + movie.getTitle());
                System.out.println("Notas Fuzzy:");
                for (String key : movie.getFuzzyVariables().keySet()) {
                    System.out.println(key + ": " + movie.getFuzzyVariables().get(key));
                }
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