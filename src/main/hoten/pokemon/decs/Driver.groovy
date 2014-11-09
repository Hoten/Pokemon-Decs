package hoten.pokemon.decs;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class Driver {

    static Gson gson = new Gson()
    static BufferedImage background
    static SpriteSheet pokemonSpriteSheet

    static void main(String[] args) {
        background = ImageIO.read(new File("images/background.jpg"))
        pokemonSpriteSheet = new SpriteSheet("images/pokemon.png", 31)
        def pokemon = loadPokemonFromJson()
        generateForResidents(pokemon)
    }

    static void generateForResidents(List<Pokemon> pokemon) {
        def names = new File("names.txt").readLines()

        def pokemonToWorkWith = getFirstEvolutionOfPokemonWithThreeStages(pokemon)
        //Map<String, Pokemon> base3PokemonSecondStage = evolve(pokemon, base3Pokemon);
        //Map<String, Pokemon> base3PokemonThirdStage = evolve(pokemon, base3PokemonSecondStage);

        Collections.shuffle(names)
        Collections.shuffle(pokemonToWorkWith)

        Map<String, Pokemon> pokemonAssignments = [:]

        def random = new Random()
        names.each { n ->
            def p = pokemonToWorkWith[random.nextInt(pokemonToWorkWith.size())]
            pokemonToWorkWith.remove p
            pokemonAssignments.put(n, p)
        }

        pokemonAssignments.each { name, p ->
            def doorDec = createDoorDec(name, p)
            ImageIO.write(doorDec, "png", new File("output/${name}.png"))
        }

        String json = gson.toJson(pokemonAssignments)
        new PrintWriter("output/assignments.json").with {
            it.write(json)
        }
    }

    static def createDoorDec(String header, String footer, List<Pokemon> pokemon) {
        List<BufferedImage> pokemonSprites = pokemon.stream().map { p ->
            new ImageBuilder(pokemonSpriteSheet.getSprite(p.national_id - 1))
                    .resize(3.5)
                    .getResult()
        }.collect(Collectors.toList())

        ImageBuilder builder = new ImageBuilder(background)

        BufferedImage first = pokemonSprites.get(0)
        int centerX = background.getWidth() / 2 - first.getWidth() / 2
        int centerY = background.getHeight() / 2 - first.getHeight() / 2
        int radius = first.getWidth()
        for (int i = 0; i < pokemonSprites.size(); i++) {
            BufferedImage sprite = pokemonSprites.get(i)
            double theta = Math.PI * 2 * i / pokemonSprites.size()
            int x = (int) (Math.cos(theta) * radius) + centerX
            int y = (int) (Math.sin(theta) * radius) + centerY
            builder.drawAt(sprite, x, y)
        }

        builder
                .drawTextCentered(header, 100)
                .drawTextCentered(footer, background.getHeight() - 50)
                .getResult()
    }

    static def createDoorDec(String name, Pokemon pokemon) {
        def pokemonSprite = new ImageBuilder(pokemonSpriteSheet.getSprite(pokemon.national_id - 1))
                .resize(8)
                .getResult()
        new ImageBuilder(background)
                .drawAtCenter(pokemonSprite)
                .drawTextCentered(name, 150)
                .drawTextCentered(pokemon.name, background.getHeight() - 100)
                .getResult()
    }

    static List<Pokemon> loadPokemonFromJson() {
        String json = loadJson("pokemon.json")
        Type listType = new TypeToken<List<Pokemon>>() {
        }.getType()
        gson.fromJson(json, listType)
    }

    static List<Pokemon> evolve(List<Pokemon> allPokemon, List<Pokemon> toEvolve) {
        toEvolve.collect { p ->
            allPokemon.find { it.name == p.getFirstNonMegaEvolution().to }
        }
    }

    static boolean isFirstOfThreeStages(List<Pokemon> allThePokemon, Pokemon p) {
        def secondStageEvolution = p.getFirstNonMegaEvolution()
        if (secondStageEvolution == null) {
            return false
        }
        def secondStage = allThePokemon.find { it.name == secondStageEvolution.to }
        if (!secondStage) {
            return false
        }
        secondStage.getFirstNonMegaEvolution() != null
    }

    static List<Pokemon> getFirstEvolutionOfPokemonWithThreeStages(List<Pokemon> pokemon) {
        pokemon.findAll { p ->
            isFirstOfThreeStages(pokemon, p)
        }
    }

    static String loadJson(def fileName) {
        FileUtils.readFileToString(new File(fileName))
    }
}
