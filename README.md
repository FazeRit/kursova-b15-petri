# Вузол комутації повідомлень (B15): стохастична мережа Петрі

Курсова робота з дисципліни «Комп’ютерне моделювання дискретно-подійних систем», КПІ ім. Ігоря Сікорського, ІП-35, Гуцан Денис.

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

## Структура

- `model` — мережа Петрі вузла комутації, параметри, збір статистики, генератор випадкових чисел, прогони моделі.
- `stats` — статистичні методи: оцінки, довірчі інтервали, χ², регресія, ANOVA.
- `experiments` — експерименти розділів 3–5, кожен у своєму класі з методом `run`.
- `Main` — вибір режиму, `Output` — вивід у консоль і CSV.

Діаграму класів наведено у файлі `docs/class-diagram.png`.
