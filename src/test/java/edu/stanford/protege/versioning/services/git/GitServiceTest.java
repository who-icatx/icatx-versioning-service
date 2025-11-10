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
    void shouldReturnNoEntitiesChangedWhenCommitMessageIsNull() {
        String result = (String) ReflectionTestUtils.invokeMethod(gitService, "buildCommitMessage", (String) null);
        assertEquals("No entities changed.", result);
    }

    @Test
    void shouldReturnNoEntitiesChangedWhenBlankOrEmptyEntities() {
        String resultBlank = (String) ReflectionTestUtils.invokeMethod(gitService, "buildCommitMessage", "   ");
        assertEquals("No entities changed.", resultBlank);

        String resultEmptyEntities = (String) ReflectionTestUtils.invokeMethod(gitService, "buildCommitMessage", " , , ");
        assertEquals("No entities changed.", resultEmptyEntities);
    }

    @Test
    void shouldListAllEntitiesWhenCountAtMost25() {
        String msg = String.join(", ",
                "http://id.who.int/icd/entity/1884305516",
                "http://id.who.int/icd/entity/1089460806",
                "http://id.who.int/icd/entity/1183150389"
        );
        String result = (String) ReflectionTestUtils.invokeMethod(gitService, "buildCommitMessage", msg);
        assertEquals("Changed " + msg + ".", result);
    }

    @Test
    void shouldListAllEntitiesWhenCountExactly25() {
        String[] entities = new String[25];
        for (int i = 0; i < 25; i++) {
            entities[i] = "http://id.who.int/icd/entity/" + (1000 + i);
        }
        String msg = String.join(", ", entities);
        String result = (String) ReflectionTestUtils.invokeMethod(gitService, "buildCommitMessage", msg);
        assertEquals("Changed " + msg + ".", result);
    }

    @Test
    void shouldSummarizeCountWhenMoreThan25Entities() {
        String[] entities = new String[26];
        for (int i = 0; i < 26; i++) {
            entities[i] = "http://id.who.int/icd/entity/" + (1000 + i);
        }
        String msg = String.join(", ", entities);
        String result = (String) ReflectionTestUtils.invokeMethod(gitService, "buildCommitMessage", msg);
        assertEquals("26 entities changed.", result);
    }

    @Test
    void shouldHandleRealWorldExampleWithin25() {
        String realWorldMessage = "http://id.who.int/icd/entity/1884305516, http://id.who.int/icd/entity/1089460806, " +
                "http://id.who.int/icd/entity/1183150389, http://id.who.int/icd/entity/24305669, " +
                "http://id.who.int/icd/entity/453948649, http://id.who.int/icd/entity/450551298";
        String result = (String) ReflectionTestUtils.invokeMethod(gitService, "buildCommitMessage", realWorldMessage);
        assertEquals("Changed " + realWorldMessage + ".", result);
    }

    @Test
    void shouldTrimWhitespaceAroundEntities() {
        String msg = " a ,  b ,   c  ";
        String result = (String) ReflectionTestUtils.invokeMethod(gitService, "buildCommitMessage", msg);
        assertEquals("Changed a, b, c.", result);
    }
}

