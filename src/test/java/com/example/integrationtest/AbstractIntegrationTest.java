package com.example.integrationtest;

import com.example.Application;

import org.bson.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.output.WaitingConsumer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@SpringBootTest(classes = {Application.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {AbstractIntegrationTest.MongoDbInitializer.class})
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public abstract class AbstractIntegrationTest {
    private static final String MONGO_DB_CONFIG_PREFIX = "spring.data.mongodb.";

    private static boolean testContainersInitialized = false;
    private static final GenericContainer<?> MONGO_CONTAINER = new GenericContainer<>("mongo:3.4.10-jessie").withExposedPorts(27017);

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${local.server.port}")
    protected int localPort;

    @BeforeClass
    public static void initTestContainers() {
        if (!testContainersInitialized) {
            MONGO_CONTAINER.start();
            testContainersInitialized = true;

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                MONGO_CONTAINER.stop();
            }));
        }
    }

    @Before
    public final void iTestSetUp() {
        mongoTemplate.getCollection("test").deleteMany(new Document());
    }

    private static void waitUntilMongoDbIsStarted() {
        ToStringConsumer toStringConsumer = new ToStringConsumer();
        WaitingConsumer waitingConsumer = new WaitingConsumer();
        Consumer<OutputFrame> composedConsumer = toStringConsumer.andThen(waitingConsumer);
        MONGO_CONTAINER.followOutput(composedConsumer);
        try {
            waitingConsumer.waitUntil(frame -> frame.getUtf8String().contains("waiting for connections"), 30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Assert.fail("Failed to start MongoDB");
        }
    }

    public static class MongoDbInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            waitUntilMongoDbIsStarted();
            String host = "mongodb://" + MONGO_CONTAINER.getContainerIpAddress();
            String port = MONGO_CONTAINER.getMappedPort(27017).toString();
            TestPropertyValues.of(MONGO_DB_CONFIG_PREFIX + "uri:" + host + ':' + port + "/db").applyTo(configurableApplicationContext.getEnvironment());
        }
    }
}
