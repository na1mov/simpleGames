package javarushSpaceInvaders.gameobjects;

import javarushSpaceInvaders.Direction;
import javarushSpaceInvaders.ShapeMatrix;

public class Bullet extends GameObject {
    private int dy;
    public boolean isAlive = true;

    public Bullet(double x, double y, Direction direction) {
        super(x, y);
        setMatrix(ShapeMatrix.BULLET);
        this.dy = direction == Direction.UP ? -1 : 1;
    }

    public void kill() {
        isAlive = false;
    }

    public void move() {
        y += dy;
    }
}
