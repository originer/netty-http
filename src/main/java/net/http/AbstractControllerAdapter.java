package net.http;

public abstract class AbstractControllerAdapter {
    public AbstractControllerAdapter() {
    }

    public abstract ApiController getControllerByUri(String var1);
}
