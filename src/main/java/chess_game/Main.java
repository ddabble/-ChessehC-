package chess_game;

import game_observer.GameObserver;

public class Main
{
	public static void main(String[] args)
	{
		Game game = new Game();
		GameObserver gameObserver = new GameObserver(game);
		game.run();
		gameObserver.start(game.getGameObjectManager());

		game.terminate();
		gameObserver.terminate();
	}
}
