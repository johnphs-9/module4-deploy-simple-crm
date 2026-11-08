package sg.edu.ntu.simple_crm_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.BeforeEach;

public class DemoServiceTest {

    DemoService demoService;

    @BeforeEach
    public void init() {
        demoService = new DemoService();
    }

    @Test
    public void calculateAge_validYear_returnsCorrectAge() {
        // 1. ARRANGE — demoService already created by init()
        int expectedAge = 35;

        // 2. ACT
        int actualAge = demoService.calculateAge(1990, 2025);

        // 3. ASSERT
        assertEquals(expectedAge, actualAge, "Age should be current year minus year of birth");
    }

    @ParameterizedTest
    @CsvSource({
            "1990, 2025, 35",
            "1965, 2025, 60",
            "2000, 2025, 25",
            "2025, 2025, 0"
    })

    public void calculateAge_parameterizedTest(int yearOfBirth, int currentYear, int expectedAge) {
        // 1. ACT
        int actualAge = demoService.calculateAge(yearOfBirth, currentYear);

        // 2. ASSERT
        assertEquals(expectedAge, actualAge);
    }

    @Test
    public void formFullName_validNames_returnsCorrectFullName() {
        // 1. ARRANGE
        String firstName = "John";
        String lastName = "Doe";
        String expectedFullName = "John Doe";

        // 2. ACT
        String actualFullName = demoService.formatFullName(firstName, lastName);

        // 3. ASSERT
        assertEquals(expectedFullName, actualFullName);
    }

    @ParameterizedTest
    @CsvSource({
            "John, Doe, John Doe",
            "Jane, Smith, Jane Smith",
            "Alice, Johnson, Alice Johnson"
    })

    public void formatFullName_parameterizedTest(String firstName, String lastName, String expectedFullName) {
        // 1. ACT
        String actualFullName = demoService.formatFullName(firstName, lastName);

        // 2. ASSERT
        assertEquals(expectedFullName, actualFullName);
    }

    @Test
    void getInitials_returnsBothInitialsInCaps() {
        // Arrange
        DemoService service = new DemoService();
        String first = "mei";
        String last = "tan";

        // Act
        String result = service.getInitials(first, last);

        // Assert
        assertEquals("MT", result);
    }

}
