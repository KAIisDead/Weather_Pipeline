# Weather Data Pipeline

This is a small Java project I built to practice data engineering basics.
It grabs 7 days of weather data for 5 cities from a free weather API, saves
it into a database, cleans it up, and then checks that the data actually
makes sense (no missing values, no duplicates, no weird temperatures).

It's basically the same idea used in real data pipelines, just small enough
to build and understand in a weekend.

## How it works

There are 4 steps, and they run one after another:

1. **Extract** — get the weather data from the internet (Open-Meteo API,
   free, no API key needed)
2. **Load** — save that data into a local database file (SQLite)
3. **Transform** — clean it up and calculate averages per city, using SQL
4. **Check** — make sure the data isn't broken (missing values, duplicates,
   impossible temperatures)

At the end it prints a little summary table to the console.

## Cities it tracks
Johannesburg, Cape Town, Durban, Nairobi, and Cairo.

## Project files

```
weather-pipeline/
├── pom.xml
├── data/
│   └── weather.db          <- gets created automatically the first time you run it
└── src/main/java/weather/
    ├── WeatherRow.java      <- one row of weather data
    ├── Extractor.java       <- step 1: get data from the API
    ├── Loader.java          <- step 2: save it to the database
    ├── Transformer.java     <- step 3: clean it up with SQL
    ├── QualityChecker.java  <- step 4: check it's not broken
    └── Pipeline.java        <- runs everything and prints the results
```

## How to run it

1. Open the project in IntelliJ (or any Java IDE)
2. Let Maven download the libraries it needs
3. Run `Pipeline.java` (right-click it → Run)

That's it — no setup, no API key, no separate database to install.

If you want to actually look at the data, in IntelliJ go to
**View → Tool Windows → Database → + → Data Source → SQLite**, and point it
at `data/weather.db`.

## What the output looks like

```
PASS: no missing temperatures
PASS: no duplicate city and date
PASS: temperatures are realistic

CITY               AVG MAX    AVG MIN    RAIN mm
Cairo                 34.2       22.1        0.0
Johannesburg          24.6       12.3        4.5
Nairobi               23.8       14.0       12.1
Durban                22.9       17.5        8.3
Cape Town             19.4       11.2       15.7
Pipeline finished.
```

hboard on top of the results
