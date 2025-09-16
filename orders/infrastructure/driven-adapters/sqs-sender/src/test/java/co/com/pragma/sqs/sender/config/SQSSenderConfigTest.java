package co.com.pragma.sqs.sender.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SQSSenderConfigTest {

    private SQSSenderConfig config;
    private SQSSenderProperties properties;
    private MetricPublisher publisher;

    @BeforeEach
    void setUp() {
        config = new SQSSenderConfig ( );
        properties = Mockito.mock ( SQSSenderProperties.class );
        publisher = mock ( MetricPublisher.class );
    }

    @Test
    void configSqs_shouldCreateSqsAsyncClient() {
        Mockito.when ( properties.region ( ) ).thenReturn ( "us-east-2" );

        SqsAsyncClient client = config.configSqs ( properties, publisher );

        assertThat ( client ).isNotNull ( );
        assertThat ( client.serviceClientConfiguration ( ).region ( ) ).isEqualTo ( Region.of ( "us-east-2" ) );
    }
}
