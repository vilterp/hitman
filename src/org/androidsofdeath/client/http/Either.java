package org.androidsofdeath.client.http;

import com.google.common.base.Function;

public abstract class Either<L, R> {

    public abstract L getLeft() throws WrongSideException;
    public abstract R getRight() throws WrongSideException;
    public abstract Object getValue();

    public <R2> Either<L,R2> bindRight(Function<R,R2> fun) {
        try {
            if(this instanceof Right) {
                return new Right<L, R2>(fun.apply(getRight()));
            } else {
                return new Left<L, R2>(getLeft());
            }
        } catch(WrongSideException e) {
            throw new RuntimeException(e);
        }

    }

}
