package com.github.aapplet.segment;

public interface IdGenerator {

    /**
     * id generate
     *
     * @param key business name
     * @return new id
     */
    long get(String key);

}
