import com.bervan.common.user.User;
import com.bervan.common.user.UserRepository;
import com.bervan.common.user.UserToUserRelation;
import com.bervan.common.user.UserToUserRelationRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

public class BaseTest {
    protected static UserRepository userRepository;
    protected static UserToUserRelationRepository userToUserRelationRepository;
    protected static RabbitMQContainer rabbitMQContainer;
    protected static MariaDBContainer mariaDBContainer;
    protected static String baseUrl = "http://localhost:9091";

    static {
        rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.11")
                .withExposedPorts(5672);
        rabbitMQContainer.start();
        System.setProperty("spring.rabbitmq.username", rabbitMQContainer.getAdminUsername());
        System.setProperty("spring.rabbitmq.password", rabbitMQContainer.getAdminPassword());
        System.setProperty("spring.rabbitmq.host", rabbitMQContainer.getHost());
        System.setProperty("spring.rabbitmq.port", rabbitMQContainer.getAmqpPort().toString());

        mariaDBContainer = new MariaDBContainer<>(DockerImageName.parse("mariadb:10.5.5"))
                .withDatabaseName("my_tools_db")
                .withUsername("my_tools_db_user")
                .withPassword("my_tools_db_password");
    }

    public BaseTest() {

    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        mariaDBContainer.start();
        registry.add("spring.datasource.url", mariaDBContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mariaDBContainer::getUsername);
        registry.add("spring.datasource.password", mariaDBContainer::getPassword);
    }

    public void setup(UserRepository userRepository, UserToUserRelationRepository userToUserRelationRepository) {
        this.userRepository = userRepository;
        this.userToUserRelationRepository = userToUserRelationRepository;
    }

    protected void createTestUser() {
        if (userRepository.findByUsername("testUser").isEmpty()) {
            User testUser = new User();
            testUser.setUsername("testUser");
            testUser.setPassword(new BCryptPasswordEncoder().encode("testUser!2#4%6"));
            testUser.setRole("ROLE_USER");
            userRepository.save(testUser);

            UserToUserRelation userRelation = new UserToUserRelation();
            userRelation.setChild(testUser);
            userRelation.setParent(testUser);
            userRelation.addOwner(testUser);
            userToUserRelationRepository.save(userRelation);
        }
    }
}