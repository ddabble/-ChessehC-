# !ChessehC!

!ChessehC! (sounds fun if pronounced *chess-itch*) is an (unfinished) real-time version of chess, created from scratch with its own 3D graphics engine.

The game was originally my solution to exercise 04 for the 2017 course Object-Oriented Programming (TDT4100) at NTNU, with extensions to the functionality in exercise 07 and 10. The exercises are reproduced and translated from Norwegian below:
> ### First exercise (04)
> 
> Choose and complete at least one of the following tasks:
>  * Tic-tac-toe (Easiest)
>  * Battleship (Easy/Medium)
>  * Sudoku (Medium)
>  * Sokoban (Hard)
>  * Own choice: Choose you own grid based game with similar complexity to the games above
> 
> ### Second exercise (07)
> Extend your game with the following new functionality:
>  * Undo and redo actions
>  * Save and load game state
>  * Display using JavaFX (Optional)
> 
> > *I didn't complete this exercise, though I implemented a lot of other stuff* :stuck_out_tongue:
> 
> ### Third exercise (10)
> Implement the observer pattern in your game:
>  * Make the GUI listen to changes in the game
>  * Let your game implement the `GenericGridGame` interface, so that prewritten GUIs may employ your game class

## Run/build instructions
_Requires having installed **JDK 8+**._
_<br>Also requires graphics drivers supporting **OpenGL 3.3**._

#### Windows
```cmd
> gradlew run
```

#### Linux
```bash
> ./gradlew run -Pplatform=linux
```

#### Mac
```bash
> ./gradlew run -Pplatform=macos
```

## Controls
Hit `Enter` to make the other chess pieces start moving.
<br>
`Esc` to pause/unpause.

#### Left player:
 * `WASD` for movement
 * `F`/`G` to turn left/right
 * `Space` to attack

#### Right player:
 * `PL;'` on English/American keyboards, `PLØÆ` on Norwegian keyboards, for movement
 * `4`/`5` on keypad to turn left/right
 * Right `Ctrl` to attack
