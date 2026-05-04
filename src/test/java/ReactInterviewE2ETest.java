import com.bervan.common.user.UserRepository;
import com.bervan.common.user.UserToUserRelationRepository;
import com.bervan.toolsapp.Application;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;

import java.io.File;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = Application.class)
@TestPropertySource("classpath:application-it.properties")
@ActiveProfiles("it")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReactInterviewE2ETest extends BaseTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserToUserRelationRepository userToUserRelationRepository;

    @Test
    @Order(0)
    public void setup() {
        super.setup(userRepository, userToUserRelationRepository);
        super.createTestUser();
    }

    @Test
    @Order(1)
    public void runPlaywrightIntegrationTests() throws Exception {
        // my-tools-vaadin-app → my-tools → IdeaProjects → my-tools-react
        // move to property
        File reactDir = new File("../../my-tools-react").getCanonicalFile();

        String command = "npx playwright test --config playwright.integration.config.ts e2e/integration/interview/interview.spec.ts --headed";
        ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
        pb.directory(reactDir);
        pb.environment().put("BACKEND_URL", baseUrl);
        pb.environment().put("CI", "true");
        pb.inheritIO();

        Process process = pb.start();
        int exitCode = process.waitFor();
        Assertions.assertEquals(0, exitCode, "Playwright integration tests failed — see output above");
    }
}
