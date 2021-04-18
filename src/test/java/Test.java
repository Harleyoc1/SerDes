import com.harleyoconnor.serdes.util.CommonCollectors;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Harley O'Connor
 */
public final class Test {

    @org.junit.jupiter.api.Test
    public void test() {
        final LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>(Arrays.asList("testing", "retention", "of", "order"));
        final LinkedHashSet<String> anotherLinkedHashSet = new LinkedHashSet<>(Arrays.asList("more", "testing", "need", "another", "set"));

        final LinkedHashSet<String> combined = Stream.concat(linkedHashSet.stream(), anotherLinkedHashSet.stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        System.out.println(combined);

        final Set<String> unmodifiable = linkedHashSet.stream().collect(CommonCollectors.toUnmodifiableLinkedSet());

//        unmodifiable.add("should produce unsupported operation exception");

        System.out.println(unmodifiable);
    }

}
