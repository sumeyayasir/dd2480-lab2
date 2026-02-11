package dd2480.ciserver;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for App class.
 */
public class AppTest {

    /**
     * Unit test to check that the main method simply exists in the App class. Since
     * the purpose of the main is to invoke the server by calling listen, there is
     * not much to test that is of value.
     */
    @Test
    public void testMainExists() throws NoSuchMethodException {
        var main = App.class.getMethod("main", String[].class);
        assertNotNull(main);
    }
}
