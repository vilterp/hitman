package org.androidsofdeath.client.http;

public class WrongSideException extends Exception {

    private Either either;

    public WrongSideException(Either either) {
        super(String.format("got wrong side of %s", either));
        this.either = either;
    }

    public Either getEither() {
        return either;
    }

}
