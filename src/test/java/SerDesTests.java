import com.harleyoconnor.serdes.ClassSerDes;
import org.junit.jupiter.api.Test;

/**
 * @author Harley O'Connor
 */
public final class SerDesTests {

    @Test
    public void test() throws IllegalAccessException {
        final var user = new User("Harley", "O'Connor", "Harleyoc1");
        final var serDes = ClassSerDes.getOrCreate(User.class);
        final var serDesTwo = ClassSerDes.getOrCreate(User.class);

        System.out.println(serDes);
    }

}
