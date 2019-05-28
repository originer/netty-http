package net.http;

public abstract class AbstractContentAdapter {
    public AbstractContentAdapter() {
    }

    public abstract String parse(String var1);

    public abstract String wrap(String var1);
}
