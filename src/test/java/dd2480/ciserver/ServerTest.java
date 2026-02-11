package dd2480.ciserver;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.sun.net.httpserver.HttpExchange;

import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Unit tests for Server class.
 */
public class ServerTest {

    /**
     * Checks that the listen method is static and public, and that it exists in the
     * Server class.
     * 
     * @throws NoSuchMethodException if the listen method is not found.
     */
    @Test
    public void testListenExists() throws NoSuchMethodException {
        var listenMethod = Server.class.getMethod("listen", int.class);
        assertNotNull(listenMethod);
        assertTrue(java.lang.reflect.Modifier.isStatic(listenMethod.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isPublic(listenMethod.getModifiers()));
    }

    /**
     * Checks that a valid request body is read correctly and returned as a string.
     * 
     * @throws Exception if an error occurs during test execution.
     */
    @Test
    public void testReadRequestBodyWithValidBody() throws Exception {
        HttpExchange mockExchange = mock(HttpExchange.class);
        String testRequestBody = "This is a test request body.";
        InputStream inputStream = new ByteArrayInputStream(
                testRequestBody.getBytes(StandardCharsets.UTF_8));
        when(mockExchange.getRequestBody()).thenReturn(inputStream);

        String mockResult = Server.readRequestBody(mockExchange);
        assertEquals(testRequestBody, mockResult);
    }
}
