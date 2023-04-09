package org.apache.shenyu.client.core.client.registrar;

import org.apache.shenyu.client.core.client.matcher.Matcher;

public abstract class AbstractRegistrar<T> implements Registrar<T> {


    private final Matcher<T> matcher;

    protected AbstractRegistrar(Matcher<T> matcher) {
        this.matcher = matcher;
    }

    @Override
    public void register(T element) {
        if (matcher.match(element)) {
            doRegister(element);
        }
    }

    protected abstract void doRegister(T element);
}
