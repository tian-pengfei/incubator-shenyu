package org.apache.shenyu.client.core.client.parser;

public interface Parser<R, T> {
    R parse(T t);
}
