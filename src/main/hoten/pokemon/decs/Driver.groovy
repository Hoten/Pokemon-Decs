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
        Map<String, Pokemon> pokemon = loadPokemonFromJson()
        generateForResidents(pokemon)
    }

    static void generateForResidents(Map<String, Pokemon> pokemon) {
        List pokemonToWorkWith = getFirstEvolutionOfPokemonWithThreeStages(pokemon)
        //Map<String, Pokemon> base3PokemonSecondStage = evolve(pokemon, base3Pokemon);
        //Map<String, Pokemon> base3PokemonThirdStage = evolve(pokemon, base3PokemonSecondStage);

        pokemonToWorkWith.remove pokemonToWorkWith.find { it.name == 'Aron' }

        def names = new File("names.txt").readLines()

        Map<String, Pokemon> pokemonAssignments = [:]

        def assignName = { n, p = null ->
            if (!p) {
                p = pokemonToWorkWith[Math.random() * pokemonToWorkWith.size() as int]
                pokemonToWorkWith.remove p
            }
            pokemonAssignments.put(n, p)
        }

        Collections.shuffle(names)
        Collections.shuffle(pokemonToWorkWith)

        names.each assignName

        pokemonAssignments.each { name, p ->
            BufferedImage doorDec = createDoorDec(name, p)
            String fileName = String.format("output/%s.png", name)
            ImageIO.write(doorDec, "png", new File(fileName))
        };

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

        ImageBuilder builder = new ImageBuilder(background);

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

    static Map<String, Pokemon> loadPokemonFromJson() {
        String json = loadJson("pokemon.json")
        Type listType = new TypeToken<HashMap<String, Pokemon>>() {
        }.getType()
        gson.fromJson(json, listType)
    }

    static Map<String, Pokemon> evolve(def allPokemon, def toEvolve) {
        toEvolve.collect { k, v ->
            allPokemon.get(v.getFirstNonMegaEvolution().to).with {
                [it.name, it]
            }
        }
    }

    static boolean isFirstOfThreeStages(Map<String, Pokemon> allThePokemon, Pokemon p) {
        def secondStageEvolution = p.getFirstNonMegaEvolution()
        if (secondStageEvolution == null) {
            return false
        }
        if (!allThePokemon.containsKey(secondStageEvolution.to)) {
            return false
        }
        def secondStage = allThePokemon.get(secondStageEvolution.to)
        secondStage.getFirstNonMegaEvolution() != null
    }

    static def getFirstEvolutionOfPokemonWithThreeStages(Map<String, Pokemon> pokemon) {
        pokemon.findAll { k, v ->
            isFirstOfThreeStages(pokemon, v)
        }.values() as List
    }

    static String loadJson(def fileName) {
        FileUtils.readFileToString(new File(fileName))
    }
}
