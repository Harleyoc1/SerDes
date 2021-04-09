package com.harleyoconnor.serdes.util;

import java.util.HashMap;
import java.util.Map;

/**
 * A container for conveniently manipulating a {@link HashMap} by chaining methods.
 *
 * @author Harley O'Connor
 * @see HashMap
 */
// TODO: Move to JavaUtilities (and extend capabilities).
public class MapContainer<M extends Map<K, V>, K, V> {

    private final M map;

    public MapContainer(final M map) {
        this.map = map;
    }

    public MapContainer<M, K, V> put (final K key, final V value) {
        this.map.put(key, value);
        return this.getThis();
    }

    public MapContainer<M, K, V> putAll (final Map<K, V> map) {
        this.map.putAll(map);
        return this.getThis();
    }

    public MapContainer<M, K, V> putIfAbsent (final K key, final V value) {
        this.map.putIfAbsent(key, value);
        return this.getThis();
    }

    public MapContainer<M, K, V> getThis() {
        return this;
    }

    public M get () {
        return this.map;
    }

    public static final class HashMapContainer<K, V> extends MapContainer<HashMap<K, V>, K, V> {
        public HashMapContainer() {
            super(new HashMap<>());
        }

        public HashMapContainer(final HashMap<K, V> map) {
            super(map);
        }
    }

}
