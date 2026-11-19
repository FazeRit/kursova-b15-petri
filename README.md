# Вузол комутації повідомлень (B15): стохастична мережа Петрі

Курсова робота з дисципліни «Моделювання систем», КПІ ім. Ігоря Сікорського, ІП-35, Гуцан Денис.

Модель побудовано з класів бібліотеки Petri-object simulation І. В. Стеценко
(модуль `petri-math` репозиторію https://github.com/StetsenkoInna/PetriObjModelPaint):
`PetriP`, `PetriT`, `ArcIn`, `ArcOut`, `PetriNet`, `PetriSim`, `PetriObjModel`, `SimulationStatisticCollector`.

## Запуск

```
git clone https://github.com/StetsenkoInna/PetriObjModelPaint
cd PetriObjModelPaint && mvn -pl petri-math -am install -DskipTests && cd ..
mvn compile exec:java
```

Режими: `protocol | verify | rng | transient | runs | base | limit | unlimited | proposals | all`,
наприклад `mvn compile exec:java -Dexec.args="base results"`. Результати зберігаються у файли CSV каталогу `results`.
