# Intelligent Mod

Mod do **Minecraft Java 26.3** (Fabric) — realistyczne przetrwanie.
Autorzy: **FranekRR2 / Sagzamon**

## Co robi

- **Ciepło z pieców** – każdy płonący piec / wielki piec / ognisko ogrzewa swój chunk (i trochę sąsiednie).
- **Pragnienie** – pasek wody na HUD. Ubywa szybciej przy sprincie, w gorących biomach i w ciepłym chunku. Gdy pasek się skończy, po 20 s gracz **umiera** (`ModConfig.DEATH_DELAY_SECONDS`).
  - Picie: **PPM pustą ręką na wodzie** (czasem efekt głodu – brudna woda), butelka wody, mleko, miód, arbuz, jabłko. Deszcz też trochę nawadnia.
- **Ruchome Piaski** – generują się na pustyniach i plażach. Jak Powder Snow, ale gęstsze: mocno spowalniają, wciągają, duszą pod powierzchnią. Mają własną animowaną teksturę i cząsteczki piasku.
- **Sucha Ziemia** – gdy w chunku jest ciepło (≥ 2), trawa i ziemia (bez wody w pobliżu) zamieniają się w Suchą Ziemię. Ratuje ją tylko **woda w pobliżu** albo **deszcz** (wraca jako zwykła ziemia, trawa odrasta normalnie).
- **Parowanie** – przy dużym cieple (≥ 4) powierzchnia wody wysycha.
- **Deszcz** – napełnia wyschnięte zbiorniki, tworzy kałuże w dziurach, a **burza** powoduje powodzie (woda z powodzi znika po deszczu).
- **Pożary** – przy cieple ≥ 4 drewniane rzeczy przy płonących piecach zaczynają się palić.
- **Logo** w prawym górnym rogu HUD, a w menu głównym logo + „Made by FranekRR2 / Sagzamon”.
- **Prawy Shift** – ekran z autorami i listą funkcji moda (klawisz zmienisz w Sterowaniu).
- Komenda `/intelligent status` – pokazuje wodę, ciepło i temperaturę.

## Budowanie

Potrzebujesz **JDK 25** i **Gradle 9.4+**.

```
gradle build
```

Gotowy plik jest w `build/libs/intelligent-mod-1.1.0.jar`. Wrzuć go do `mods/` razem z **Fabric API 0.161.0+26.3** (Fabric Loader 0.19.5+).

Na GitHubie wystarczy wypchnąć repo — workflow `.github/workflows/build.yml` zbuduje moda i wystawi jar w zakładce *Actions → Artifacts*.

## Znane ograniczenia

- Mapa ciepła i zapamiętane „wyparowane” bloki są w pamięci (nie zapisują się). Po restarcie serwera ciepło odtwarza się w kilka sekund, ale wyschnięte jeziora nie wrócą same w deszczu.
- Mod działa tylko wokół graczy (promień 40 bloków).
