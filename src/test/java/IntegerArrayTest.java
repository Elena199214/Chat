import Homework6.IntegerArray;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class IntegerArrayTest {
        IntegerArray integerArray;

    @BeforeEach
    void init() {
        System.out.println("initialization");


        integerArray = new IntegerArray();
    }

    @AfterEach
    void tearDown(){
        System.out.println("test finished");
    }

    @Test
    @DisplayName("ArrayAfter4 Тест 1")
    void testArrayAfter4_1(){
        Assertions.assertArrayEquals(new int[]{1,7}, integerArray.ArrayAfter4(new int[] {1, 4, 4, 2, 3, 4, 1, 7}));
    }

    @Test
    @DisplayName("ArrayAfter4 Тест 2")
    void testArrayAfter4_2() {
        Assertions.assertArrayEquals(new int[] {5,6}, integerArray.ArrayAfter4(new int[] {1,4,2,4,3,4,5,6}));
    }

    @Test
    @DisplayName("ArrayAfter4 Тест 3")
    void testArrayAfter4_3() {
        Assertions.assertThrows(RuntimeException.class, () -> integerArray.ArrayAfter4(new int[] {1,2,3,4}), ""+ 4 +" последнее число в массиве ");
    }

    @Test
    @DisplayName("ArrayAfter4 Тест 4")
    void testArrayAfter4_4() {
        Assertions.assertThrows(RuntimeException.class, () -> integerArray.ArrayAfter4(new int[] {1,2,3}));
    }


    @Test
    @DisplayName("ArrayCheckNumbers Тест 1")
    void ArrayCheckNumbers1() {
        Assertions.assertEquals(true,integerArray.ArrayCheckNumbers(new int[]{1,2,3,4}));
    }

    @Test
    @DisplayName("ArrayCheckNumbers Тест 2")
    void ArrayCheckNumbers2() {
        Assertions.assertEquals(false,integerArray.ArrayCheckNumbers(new int[]{5,2,3,4}));
    }

    @Test
    @DisplayName("ArrayCheckNumbers Тест 3")
    void ArrayCheckNumbers3() {
        Assertions.assertTrue(integerArray.ArrayCheckNumbers(new int[]{1,2,3,6,4}));
    }

    @Test
    @DisplayName("ArrayCheckNumbers Тест 4")
    void ArrayCheckNumbers4() {
        Assertions.assertFalse(integerArray.ArrayCheckNumbers(new int[]{8,2,3,3}));
    }


}
