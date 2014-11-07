package hoten.pokemon.decs;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.io.IOUtils;

public class PokemonDataFetcher {

    public static void main(String[] args) throws InterruptedException {
        PokemonDataFetcher fetcher = new PokemonDataFetcher();
        List<Pokemon> pokemon = fetcher.fetchPokemonUpTo(649);
        Map<String, Pokemon> mapped = pokemon.stream()
                .collect(Collectors.toMap(p -> p.name, Function.identity()));
        String json = new Gson().toJson(mapped);
        System.out.println(json);
    }

    public List<Pokemon> fetchPokemonUpTo(int n) throws InterruptedException {
        ExecutorService ex = Executors.newCachedThreadPool();
        List<Pokemon> result = new ArrayList();
        int numThreads = 10;
        int numTasksForEach = n / numThreads;
        List<List<Integer>> tasks = chopped(IntStream.rangeClosed(1, n).boxed().collect(Collectors.toList()), numTasksForEach);
        for (int i = 0; i < numThreads; i++) {
            final List<Integer> ids = tasks.get(i);
            ex.submit(() -> {
                ids.stream().forEach((id) -> {
                    try {
                        result.add(fetchPokemon(id));
                    } catch (IOException ex1) {
                        Logger.getLogger(PokemonDataFetcher.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                });
            });
        }

        ex.shutdown();
        ex.awaitTermination(10, TimeUnit.MINUTES);

        return result;
    }

    public Pokemon fetchPokemon(int id) throws IOException {
        try (InputStream in = new URL("http://pokeapi.co/api/v1/pokemon/" + id).openStream()) {
            String json = IOUtils.toString(in);
            return new Gson().fromJson(json, Pokemon.class);
        }
    }

    private <T> List<List<T>> chopped(List<T> list, final int L) {
        List<List<T>> parts = new ArrayList();
        final int N = list.size();
        for (int i = 0; i < N; i += L) {
            parts.add(new ArrayList(list.subList(i, Math.min(N, i + L))));
        }
        return parts;
    }
}
