package edu.stanford.protege.versioning.services.git;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GitServiceTest {

    @InjectMocks
    private GitService gitService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(gitService, "repoSsh", "git@test.com:repo.git");
        ReflectionTestUtils.setField(gitService, "jsonFileLocation", "/tmp/");
    }

    @Test
    void shouldReturnNullWhenCommitMessageIsNull() {
        // When
        String result = (String) ReflectionTestUtils.invokeMethod(gitService, "truncateCommitMessage", (String) null);

        // Then
        assertNull(result);
    }

    @Test
    void shouldReturnOriginalMessageWhenShorterThanMaxSize() {
        // Given
        String shortMessage = "http://id.who.int/icd/entity/1884305516";

        // When
        String result = (String) ReflectionTestUtils.invokeMethod(gitService, "truncateCommitMessage", shortMessage);

        // Then
        assertEquals(shortMessage, result);
    }

    @Test
    void shouldReturnOriginalMessageWhenExactlyMaxSize() {
        // Given
        String exactSizeMessage = "a".repeat(100);

        // When
        String result = (String) ReflectionTestUtils.invokeMethod(gitService, "truncateCommitMessage", exactSizeMessage);

        // Then
        assertEquals(exactSizeMessage, result);
    }

    @Test
    void shouldTruncateAtLastCommaAndAddRemainingEntitiesCount() {
        // Given
        String longMessage = "http://id.who.int/icd/entity/1884305516, http://id.who.int/icd/entity/1089460806, " +
                "http://id.who.int/icd/entity/1183150389, http://id.who.int/icd/entity/24305669, " +
                "http://id.who.int/icd/entity/453948649, http://id.who.int/icd/entity/450551298";

        // When
        String result = (String) ReflectionTestUtils.invokeMethod(gitService, "truncateCommitMessage", longMessage);

        // Then
        assertNotNull(result);
        assertTrue(result.length() > 100);
        assertTrue(result.contains(" and "));
        assertTrue(result.contains(" entities more"));
        // Should end with comma before " and X entities more"
        int andIndex = result.indexOf(" and ");
        assertTrue(andIndex > 0);
        assertEquals(',', result.charAt(andIndex - 1));
    }

    @Test
    void shouldTruncateAtMaxSizeWhenNoCommaFound() {
        // Given
        String longMessageWithoutComma = "a".repeat(150);

        // When
        String result = (String) ReflectionTestUtils.invokeMethod(gitService, "truncateCommitMessage", longMessageWithoutComma);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("a".repeat(100)));
        assertTrue(result.contains(" and "));
        assertTrue(result.contains(" entities more"));
    }

    @Test
    void shouldHandleRealWorldExampleWithManyEntities() {
        // Given - similar to the example from the user
        String realWorldMessage = "http://id.who.int/icd/entity/1884305516, http://id.who.int/icd/entity/1089460806, " +
                "http://id.who.int/icd/entity/1183150389, http://id.who.int/icd/entity/24305669, " +
                "http://id.who.int/icd/entity/453948649, http://id.who.int/icd/entity/450551298, " +
                "http://id.who.int/icd/entity/1302162486, http://id.who.int/icd/entity/734194039, " +
                "http://id.who.int/icd/entity/1168844343, http://id.who.int/icd/entity/1310273882, " +
                "http://id.who.int/icd/entity/1292787411, http://id.who.int/icd/entity/1086302589, " +
                "http://id.who.int/icd/entity/893526201, http://id.who.int/icd/entity/67614020, " +
                "http://id.who.int/icd/entity/953243647, http://id.who.int/icd/entity/1941299304, " +
                "http://id.who.int/icd/entity/1130054042, http://id.who.int/icd/entity/1428595607, " +
                "http://id.who.int/icd/entity/799329433, http://id.who.int/icd/entity/629545248, " +
                "http://id.who.int/icd/entity/502334750, http://id.who.int/icd/entity/447434750, " +
                "http://id.who.int/icd/entity/352068750, http://id.who.int/icd/entity/1265576634";

        // When
        String result = (String) ReflectionTestUtils.invokeMethod(gitService, "truncateCommitMessage", realWorldMessage);

        // Then
        assertNotNull(result);
        // Should contain some entities and the "and X entities more" suffix
        assertTrue(result.contains("http://id.who.int/icd/entity/"));
        assertTrue(result.contains(" and "));
        assertTrue(result.contains(" entities more"));
        // Count the entities in the original message
        int totalEntities = realWorldMessage.split(",").length;
        // Extract the number from "and X entities more"
        String entitiesMorePart = result.substring(result.indexOf(" and ") + 5);
        int remainingEntities = Integer.parseInt(entitiesMorePart.split(" ")[0]);
        // Verify that total entities = displayed entities + remaining entities
        int displayedEntities = result.substring(0, result.indexOf(" and ")).split(",").length;
        assertEquals(totalEntities, displayedEntities + remainingEntities);
    }

    @Test
    void shouldHandleMessageWithCommaExactlyAtMaxSize() {
        // Given - message where comma is exactly at position 100
        String message = "a".repeat(99) + "," + "b".repeat(50);

        // When
        String result = (String) ReflectionTestUtils.invokeMethod(gitService, "truncateCommitMessage", message);

        // Then
        assertNotNull(result);
        assertTrue(result.contains(" and "));
        assertTrue(result.contains(" entities more"));
        // Should include the comma at position 100
        assertTrue(result.startsWith("a".repeat(99) + ","));
    }
}

