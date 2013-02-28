package org.androidsofdeath.client.http;

public class Right<L, R> extends Either<L, R> {

    private R val;

    public Right(R val) {
        this.val = val;
    }

    @Override
    public L getLeft() throws WrongSideException {
        throw new WrongSideException(this);
    }

    @Override
    public R getRight() {
        return val;
    }

    @Override
    public Object getValue() {
        return val;
    }

    @Override
    public String toString() {
        return "Right{" +
                "val=" + val +
                '}';
    }

}
