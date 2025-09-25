package co.com.pragma.metrics.aws;

import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.metrics.MetricCollection;
import software.amazon.awssdk.metrics.MetricRecord;
import software.amazon.awssdk.metrics.SdkMetric;

import java.time.Duration;
import java.util.List;

import static org.mockito.Mockito.when;

class MicrometerMetricPublisherTest {

    @Test
    void metricTest_withMocksOnly() {
        LoggingMeterRegistry loggingMeterRegistry = LoggingMeterRegistry
                .builder ( LoggingRegistryConfig.DEFAULT )
                .build ( );

        MicrometerMetricPublisher micrometerMetricPublisher =
                new MicrometerMetricPublisher ( loggingMeterRegistry );

        MetricCollection metricCollection = Mockito.mock ( MetricCollection.class );

        @SuppressWarnings("unchecked")
        MetricRecord < Duration > durationRecord = (MetricRecord < Duration >) Mockito.mock ( MetricRecord.class );
        @SuppressWarnings("unchecked")
        MetricRecord < Integer > counterRecord = (MetricRecord < Integer >) Mockito.mock ( MetricRecord.class );
        @SuppressWarnings("unchecked")
        MetricRecord < String > stringRecord = (MetricRecord < String >) Mockito.mock ( MetricRecord.class );
        @SuppressWarnings("unchecked")
        MetricRecord < Boolean > booleanRecord = (MetricRecord < Boolean >) Mockito.mock ( MetricRecord.class );

        @SuppressWarnings("unchecked")
        SdkMetric < Duration > latencyMetric = (SdkMetric < Duration >) Mockito.mock ( SdkMetric.class );
        @SuppressWarnings("unchecked")
        SdkMetric < Integer > requestMetric = (SdkMetric < Integer >) Mockito.mock ( SdkMetric.class );
        @SuppressWarnings("unchecked")
        SdkMetric < String > serviceMetric = (SdkMetric < String >) Mockito.mock ( SdkMetric.class );
        @SuppressWarnings("unchecked")
        SdkMetric < Boolean > successMetric = (SdkMetric < Boolean >) Mockito.mock ( SdkMetric.class );

        when ( latencyMetric.name ( ) ).thenReturn ( "latency" );
        when ( requestMetric.name ( ) ).thenReturn ( "requests" );
        when ( serviceMetric.name ( ) ).thenReturn ( "service" );
        when ( successMetric.name ( ) ).thenReturn ( "success" );

        when ( durationRecord.metric ( ) ).thenReturn ( latencyMetric );
        when ( durationRecord.value ( ) ).thenReturn ( Duration.ofMillis ( 200 ) );

        when ( counterRecord.metric ( ) ).thenReturn ( requestMetric );
        when ( counterRecord.value ( ) ).thenReturn ( 5 );

        when ( stringRecord.metric ( ) ).thenReturn ( serviceMetric );
        when ( stringRecord.value ( ) ).thenReturn ( "orders" );

        when ( booleanRecord.metric ( ) ).thenReturn ( successMetric );
        when ( booleanRecord.value ( ) ).thenReturn ( true );

        List < MetricRecord < ? > > records = List.of ( durationRecord, counterRecord, stringRecord, booleanRecord );
        when ( metricCollection.stream ( ) ).thenReturn ( records.stream ( ) );

        micrometerMetricPublisher.publish ( metricCollection );
        micrometerMetricPublisher.close ( );

    }
}