package swing2048game;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Collections;
import java.util.PriorityQueue;

public class Model {
    private Tile[][] gameTiles;
    private static final int FIELD_WIDTH = 4;
    Stack<Tile[][]> previousStates = new Stack<>();
    Stack<Integer> previousScores = new Stack<>();
    boolean isSaveNeeded = true;
    int maxTile = 2;
    int score = 0;

    public Model() {
        resetGameTiles();
    }

    Tile[][] getGameTiles() {
        return gameTiles;
    }

    void resetGameTiles() {
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    private void addTile() {
        List<Tile> emptyTiles = getEmptyTiles();
        if (!emptyTiles.isEmpty()) {
            int index = (int) (Math.random() * emptyTiles.size()) % emptyTiles.size();
            Tile emptyTile = emptyTiles.get(index);
            emptyTile.value = Math.random() < 0.9 ? 2 : 4;
        }
    }

    private List<Tile> getEmptyTiles() {
        final List<Tile> list = new ArrayList<>();
        for (Tile[] tileArray : gameTiles) {
            for (Tile t : tileArray)
                if (t.isEmpty()) {
                    list.add(t);
                }
        }
        return list;
    }

    private boolean compressTiles(Tile[] tiles) {
        int insertPosition = 0;
        boolean result = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (!tiles[i].isEmpty()) {
                if (i != insertPosition) {
                    tiles[insertPosition] = tiles[i];
                    tiles[i] = new Tile();
                    result = true;
                }
                insertPosition++;
            }
        }
        return result;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean result = false;
        LinkedList<Tile> tilesList = new LinkedList<>();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (tiles[i].isEmpty()) {
                continue;
            }

            if (i < FIELD_WIDTH - 1 && tiles[i].value == tiles[i + 1].value) {
                int updatedValue = tiles[i].value * 2;
                if (updatedValue > maxTile) {
                    maxTile = updatedValue;
                }
                score += updatedValue;
                tilesList.addLast(new Tile(updatedValue));
                tiles[i + 1].value = 0;
                result = true;
            } else {
                tilesList.addLast(new Tile(tiles[i].value));
            }
            tiles[i].value = 0;
        }

        for (int i = 0; i < tilesList.size(); i++) {
            tiles[i] = tilesList.get(i);
        }

        return result;
    }

    private Tile[][] rotateClockwise(Tile[][] tiles) {
        final int N = tiles.length;
        Tile[][] result = new Tile[N][N];
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                result[c][N - 1 - r] = tiles[r][c];
            }
        }
        return result;
    }

    public void left() {
        if (isSaveNeeded) {
            saveState(gameTiles);
        }
        boolean moveFlag = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i])) {
                moveFlag = true;
            }
        }
        if (moveFlag) {
            addTile();
        }
        isSaveNeeded = true;
    }

    public void right() {
        saveState(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        left();
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
    }

    public void up() {
        saveState(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        left();
        gameTiles = rotateClockwise(gameTiles);
    }

    public void down() {
        saveState(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        left();
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
    }

    private int getEmptyTilesCount() {
        return getEmptyTiles().size();
    }

    private boolean isFull() {
        return getEmptyTilesCount() == 0;
    }

    boolean canMove() {
        if (!isFull()) {
            return true;
        }

        for (int x = 0; x < FIELD_WIDTH; x++) {
            for (int y = 0; y < FIELD_WIDTH; y++) {
                Tile t = gameTiles[x][y];
                if ((x < FIELD_WIDTH - 1 && t.value == gameTiles[x + 1][y].value)
                        || ((y < FIELD_WIDTH - 1) && t.value == gameTiles[x][y + 1].value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void saveState(Tile[][] tiles) {
        Tile[][] tempTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles.length; j++) {
                tempTiles[i][j] = new Tile(tiles[i][j].value);
            }
        }
        previousStates.push(tempTiles);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback() {
        if (previousStates.size() != 0 && previousScores.size() != 0) {
            gameTiles = previousStates.pop();
            score = previousScores.pop();
        }
    }

    public void randomMove() {
        switch (((int) (Math.random() * 100)) % 4) {
            case 0:
                left();
                break;
            case 1:
                right();
                break;
            case 2:
                up();
                break;
            case 3:
                down();
                break;
        }
    }

    public boolean hasBoardChanged() {
        Tile[][] lastBoard = previousStates.peek();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (lastBoard[i][j].value != gameTiles[i][j].value) {
                    return true;
                }
            }
        }
        return false;
    }

    public MoveEfficiency getMoveEfficiency(Move move) {
        move.move();
        if (!hasBoardChanged()) {
            rollback();
            return new MoveEfficiency(-1, 0, move);
        }

        int emptyTilesCount = getEmptyTiles().size();

        MoveEfficiency moveEfficiency = new MoveEfficiency(emptyTilesCount, score, move);
        rollback();

        return moveEfficiency;
    }

    public void autoMove() {
        PriorityQueue<MoveEfficiency> fourMoves = new PriorityQueue<>(4, Collections.reverseOrder());
        fourMoves.add(getMoveEfficiency(this::up));
        fourMoves.add(getMoveEfficiency(this::down));
        fourMoves.add(getMoveEfficiency(this::right));
        fourMoves.add(getMoveEfficiency(this::left));
        fourMoves.peek().getMove().move();
    }
}
