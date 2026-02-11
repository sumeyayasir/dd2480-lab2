package dd2480.ciserver;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for App class.
 */
public class AppTest {

    /**
     * Checks that the main method exists, and that it is public and static.
     * 
     * @throws NoSuchMethodException if the main method is not found.
     */
    @Test
    public void testMainExists() throws NoSuchMethodException {
        var main = App.class.getMethod("main", String[].class);
        assertNotNull(main);
    }
}
