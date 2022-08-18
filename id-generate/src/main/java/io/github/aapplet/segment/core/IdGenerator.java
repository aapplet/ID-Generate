package io.github.aapplet.segment.core;

public interface IdGenerator {

    /**
     * id generate
     *
     * @param key service key
     * @return service id
     */
    long nextId(String key);

}
