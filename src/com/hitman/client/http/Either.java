package com.hitman.client.http;

import android.renderscript.Long2;
import com.google.common.base.Function;

public abstract class Either<L, R> {

    public abstract L getLeft() throws WrongSideException;
    public abstract R getRight() throws WrongSideException;
    public abstract Object getValue();

    public <R2> Either<L,R2> bindRight(Function<R,R2> fun) {
        try {
            return new Right<L, R2>(fun.apply(getRight()));
        } catch (WrongSideException e) {
            try {
                return new Left<L, R2>(getLeft());
            } catch (WrongSideException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    public <L2> Either<L2,R> bindLeft(Function<L,L2> fun) {
        try {
            return new Left<L2,R>(fun.apply(getLeft()));
        } catch (WrongSideException e) {
            try {
                return new Right<L2,R>(getRight());
            } catch (WrongSideException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

}
