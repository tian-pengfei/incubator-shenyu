package org.apache.shenyu.client.core.client.registrar;

public interface Registrar<T> {
    void register(T info);
}
