package user_1.problem_1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProblemTest {
    private final Solution calculator = new Solution();
    @Test
    @DisplayName("1 + 2 = 3")
    void addsTwoNumbers() {
        assertEquals(3, calculator.add(1, 2), "1 + 2 should equal 3");
    }
    @Test
    @DisplayName("3 - 1 = 2")
    void minusTwoNumbers() {
        assertEquals(2, calculator.minus(3, 1), "3 - 1 should equal 2");
    }
    @ParameterizedTest(name = "{0} + {1} = {2}")
    @CsvSource({
            "0,    1,   1",
            "1,    2,   3",
            "49,  51, 100",
            "1,  100, 101"
    })
    void add(int first, int second, int expectedResult) {
        assertEquals(expectedResult, calculator.add(first, second),
                () -> first + " + " + second + " should equal " + expectedResult);
    }
}