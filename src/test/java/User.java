import com.harleyoconnor.serdes.ClassSerDes;
import com.harleyoconnor.serdes.IndexedSerDesable;
import com.harleyoconnor.serdes.SerDes;
import com.harleyoconnor.serdes.SerDesable;
import com.harleyoconnor.serdes.annotation.Name;
import com.harleyoconnor.serdes.annotation.Primary;
import com.harleyoconnor.serdes.field.PrimaryField;

/**
 * @author Harley O'Connor
 */
public record User (@Name("FirstName") String firstName,
                    String surname,
                    @Primary String username
) implements SerDesable<User, String> {

    @Override
    public SerDes<User, String> getSerDes() {
        return ClassSerDes.getOrCreate(User.class);
    }

    @Override
    public PrimaryField<User, String> getPrimaryField() {
        return this.getSerDes().getPrimaryField();
    }

}