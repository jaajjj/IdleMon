package com.example.idlemon

enum class PokemonType(val nom: String) {
    ACIER("Acier"),
    COMBAT("Combat"),
    DRAGON("Dragon"),
    EAU("Eau"),
    FEU("Feu"),
    FEE("Fée"),
    GLACE("Glace"),
    INSECTE("Insecte"),
    NORMAL("Normal"),
    PLANTE("Plante"),
    POISON("Poison"),
    PSY("Psy"),
    ROCHE("Roche"),
    SOL("Sol"),
    SPECTRE("Spectre"),
    TENEBRES("Ténèbres"),
    VOL("Vol"),
    ELECTRIK("Électrik");

    var faiblesses: List<PokemonType> = emptyList()
    var resistances: List<PokemonType> = emptyList()
    var immunites: List<PokemonType> = emptyList()

    companion object {
        fun initialiserTable() {
            NORMAL.faiblesses = listOf(COMBAT)
            NORMAL.immunites = listOf(SPECTRE)

            FEU.faiblesses = listOf(EAU, SOL, ROCHE)
            FEU.resistances = listOf(FEU, PLANTE, GLACE, INSECTE, ACIER, FEE)

            EAU.faiblesses = listOf(PLANTE, ELECTRIK)
            EAU.resistances = listOf(FEU, EAU, GLACE, ACIER)

            PLANTE.faiblesses = listOf(FEU, GLACE, POISON, VOL, INSECTE)
            PLANTE.resistances = listOf(EAU, PLANTE, ELECTRIK, SOL)

            ELECTRIK.faiblesses = listOf(SOL)
            ELECTRIK.resistances = listOf(ELECTRIK, VOL, ACIER)

            GLACE.faiblesses = listOf(FEU, COMBAT, ROCHE, ACIER)
            GLACE.resistances = listOf(GLACE)

            COMBAT.faiblesses = listOf(VOL, PSY, FEE)
            COMBAT.resistances = listOf(INSECTE, ROCHE, TENEBRES)

            POISON.faiblesses = listOf(SOL, PSY)
            POISON.resistances = listOf(PLANTE, COMBAT, POISON, INSECTE, FEE)

            SOL.faiblesses = listOf(EAU, PLANTE, GLACE)
            SOL.resistances = listOf(POISON, ROCHE)
            SOL.immunites = listOf(ELECTRIK)

            VOL.faiblesses = listOf(ELECTRIK, GLACE, ROCHE)
            VOL.resistances = listOf(PLANTE, COMBAT, INSECTE)
            VOL.immunites = listOf(SOL)

            PSY.faiblesses = listOf(INSECTE, SPECTRE, TENEBRES)
            PSY.resistances = listOf(COMBAT, PSY)

            INSECTE.faiblesses = listOf(FEU, VOL, ROCHE)
            INSECTE.resistances = listOf(PLANTE, COMBAT, SOL)

            ROCHE.faiblesses = listOf(EAU, PLANTE, COMBAT, SOL, ACIER)
            ROCHE.resistances = listOf(NORMAL, FEU, POISON, VOL)

            SPECTRE.faiblesses = listOf(SPECTRE, TENEBRES)
            SPECTRE.resistances = listOf(POISON, INSECTE)
            SPECTRE.immunites = listOf(NORMAL, COMBAT)

            DRAGON.faiblesses = listOf(GLACE, DRAGON, FEE)
            DRAGON.resistances = listOf(FEU, EAU, PLANTE, ELECTRIK)

            TENEBRES.faiblesses = listOf(COMBAT, INSECTE, FEE)
            TENEBRES.resistances = listOf(SPECTRE, TENEBRES)
            TENEBRES.immunites = listOf(PSY)

            ACIER.faiblesses = listOf(FEU, COMBAT, SOL)
            ACIER.resistances =
                    listOf(NORMAL, PLANTE, GLACE, VOL, PSY, INSECTE, ROCHE, DRAGON, ACIER, FEE)
            ACIER.immunites = listOf(POISON)

            FEE.faiblesses = listOf(POISON, ACIER)
            FEE.resistances = listOf(COMBAT, INSECTE, TENEBRES)
            FEE.immunites = listOf(DRAGON)
        }
    }

    fun calculerEfficaciteContre(typeAttaque: PokemonType, pokemonDefenseur: Pokemon): Double {
        var multiplicateur = 1.0
        val typeDef = pokemonDefenseur.species.types
        for (type in typeDef) {
            if (type.faiblesses.contains(typeAttaque)) {
                multiplicateur *= 2.0
            }
            if (type.resistances.contains(typeAttaque)) {
                multiplicateur *= 0.5
            }
            if (type.immunites.contains(typeAttaque)) {
                multiplicateur *= 0.0
            }
        }
        return multiplicateur
    }
}
