package hoten.pokemon.decs;

import java.util.ArrayList;
import java.util.List;

public class Pokemon {

    public String name;
    public int national_id;
    public List<Evolution> evolutions = new ArrayList();

    public Evolution getFirstNonMegaEvolution() {
        return evolutions.stream()
                .filter(e -> !e.to.contains("-mega"))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return name;
    }
}
