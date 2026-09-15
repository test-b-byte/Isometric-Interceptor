# AirCom
 
AirCom is a Java desktop game that reimagines Battleship as an isometric, missile-based combat game. Two players take turns attacking and defending across a shared board, launching missiles from their ships and trying to intercept incoming fire before it lands.
 
Originally built as a CS 5004 project, then extended afterward as a personal sandbox project.
 
## How it plays
 
Each round alternates between two phases:
 
- **Attack** — the attacking player picks a launch point (one of their ships) and a target on the opposing side of the board, then fires a missile along a calculated flight path.
- **Defense** — the defending player picks their own launch point and an interceptor position, trying to land a shot somewhere along the attacker's flight path before it reaches its target.
Launching a defensive interceptor risks exposing the defender's own ship positions, so there's a risk/reward tension in choosing to defend versus staying hidden. Ships have different lengths and launch speeds (carrier, battleship, cruiser, destroyer), which affects how quickly they can respond.
 
The game ends when one player's ships are all eliminated.
 
## Running it
 
This is an IntelliJ IDEA project. Open the `AirCom` folder in IntelliJ and run `Main.java` (in `aircom.render`), or compile manually and run:
 
```
javac -d out src/*.java
java -cp out aircom.render.Main
```
 
A window will open with the game board rendered via Swing.
 
## Architecture
 
- **State pattern** — each turn phase (`AttackState`, `DefenseState`, `ResultState`, `GameOverState`) is its own state, keeping turn logic isolated instead of one large game loop tracking whose turn it is.
- **Observer pattern** — `GameObserver` and `BoardObserver` let the board broadcast turn outcomes (hits, misses, interceptions, eliminations) without the model needing to know about rendering.
- **Adapter pattern** — `PlayerInput` decouples the model layer from Swing/keyboard handling, so input could later be swapped out (AI player, different input method) without touching game logic.
## Project structure
 
```
src/    — game logic and rendering (aircom.model, aircom.render)
test/   — unit tests (JUnit)
```
 
## Status
 
Actively evolving as a personal project. Recent additions beyond the original coursework submission include an animated water texture, parabolic missile arcs, an intercept animation, screen-space explosions, and ship sprites rendered through `ShipRenderer`.
