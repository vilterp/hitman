package org.androidsofdeath.client.http;

public class Left<L,R> extends Either<L,R> {

    private L val;

    public Left(L val) {
        this.val = val;
    }

    @Override
    public L getLeft() {
        return val;
    }

    @Override
    public R getRight() throws WrongSideException {
        throw new WrongSideException(this);
    }

    @Override
    public Object getValue() {
        return val;
    }

    @Override
    public String toString() {
        return "Left{" +
                "val=" + val +
                '}';
    }

}
