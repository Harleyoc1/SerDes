package com.harleyoconnor.serdes.util;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collector;

/**
 * Implementations of Collector that implement various useful reduction
 * operations, such as accumulating elements into collections, summarizing
 * elements according to various criteria, etc.
 *
 * <p>This class is essentially an extension of {@link java.util.stream.Collectors},
 * providing even more useful implementations of {@link Collector}.</p>
 *
 * @author Harley O'Connor
 * @see Collector
 * @see java.util.stream.Collectors
 * @since SerDes 0.0.4
 */
// TODO: Move to JavaUtilities and add more useful collectors.
public final class CommonCollectors {

    /**
     * Returns a {@code Collector} that accumulates the input elements into an
     * unmodifiable, linked {@link Set}. The collector uses {@link LinkedHashSet} as
     * an accumulator, using {@link Collections#unmodifiableSet(Set)} as a finisher
     * to return an unmodifiable view of the created {@link LinkedHashSet}.
     *
     * <p>The returned Collector disallows null values and will throw
     * {@link NullPointerException} if it is presented with a null value. If the input
     * contains duplicate elements, an arbitrary element of the duplicates is preserved.</p>
     *
     * @param <T> The type of the input elements.
     * @return A {@code Collector} that accumulates the input elements into an
     *         unmodifiable, linked {@link Set}.
     */
    @SuppressWarnings("unchecked") // Apparently Java cannot automatically convert java.util.Set<T> to java.util.Set<T>.
    public static <T> Collector<T, ?, Set<T>> toUnmodifiableLinkedSet() {
        return Collector.of(LinkedHashSet::new, LinkedHashSet::add,
                (left, right) -> { left.addAll(right); return left; },
                set -> (Set<T>) Collections.unmodifiableSet(set));
    }

}
