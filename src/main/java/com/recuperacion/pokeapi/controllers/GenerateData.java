package com.recuperacion.pokeapi.controllers;

import com.recuperacion.pokeapi.client.PokeApiClient;
import com.recuperacion.pokeapi.models.PokemonDescription;
import com.recuperacion.pokeapi.models.PokemonModel;
import com.recuperacion.pokeapi.models.PokemonWeaknesses;
import com.recuperacion.pokeapi.models.ResultInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class GenerateData {
    private PokemonModel pokemonModel;
    private PokeApiClient pokeApiClient;
    private String language;

    public GenerateData(PokemonModel pokemonModel, String language, PokeApiClient pokeApiClient) {
        this.pokemonModel = pokemonModel;
        this.language = language;
        this.pokeApiClient = pokeApiClient;

    }

    public Optional<String> getInformation() {
        if (pokemonModel != null && pokemonModel.getName() != null){
            ResultInfo.ResultInfoBuilder resultInfoBuilder = ResultInfo.builder();
            resultInfoBuilder.name(pokemonModel.getName());
            resultInfoBuilder.height(pokemonModel.getHeight());
            resultInfoBuilder.weight(pokemonModel.getWeight());

             Optional<PokemonDescription.Texts> aux = pokeApiClient.getPokemonDescription(pokemonModel.getName())
                    .getFlavor_text_entries().stream()
                    .filter(texto -> language.equals(texto.getLanguage().getName())).findFirst();

            String description;

            if (aux.isEmpty()){
                description = "No hay una descripción para el idioma seleccionado :/";
            } else {
                description = aux.get().getFlavor_text();
            }

            resultInfoBuilder.description(description);

            String types = pokemonModel.getTypes().stream()
                    .map(type -> type.getType().getName()).reduce("", (text, element) -> text + " " +element);

            resultInfoBuilder.type(types);

            HashSet<String> weak = new HashSet<>();
            List<PokemonWeaknesses> provicional =  pokemonModel.getTypes().stream()
                    .map(type -> pokeApiClient.getTypeWeaknesses(type.getType().getName())).collect(Collectors.toList());

            provicional.stream().
                    forEach(weakList -> weakList.getDamage_relations().getDouble_damage_from().stream()
                            .forEach(weakness -> weak.add(weakness.getName())));

            String weaknesses = weak.stream().reduce("", (texto, weakness) -> texto + " " + weakness);

            resultInfoBuilder.weaknesses(weaknesses);


            String basePoint = pokemonModel.getStats().stream()
                    .map(stat -> stat.getStat().getName() + ": " + "*".repeat(stat.getBase_stat() / 10))
                    .reduce("", (texto, value) -> texto + "     " + value + "\n");

            resultInfoBuilder.basePoint(basePoint);

            log.info("\n*********** Resultado en consola **********" +
                    "\n" + resultInfoBuilder);

            return Optional.of(resultInfoBuilder.toString());
        }
        return Optional.of("No se encontró el pokemon que buscabas :/");
    }
}
