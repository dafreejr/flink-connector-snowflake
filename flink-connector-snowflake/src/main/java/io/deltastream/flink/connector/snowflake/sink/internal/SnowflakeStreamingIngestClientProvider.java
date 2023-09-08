package io.deltastream.flink.connector.snowflake.sink.internal;

import org.apache.flink.annotation.Internal;
import org.apache.flink.util.Preconditions;

import org.apache.flink.shaded.guava30.com.google.common.collect.Maps;

import io.deltastream.flink.connector.snowflake.sink.config.SnowflakeWriterConfig;
import net.snowflake.ingest.streaming.SnowflakeStreamingIngestClient;
import net.snowflake.ingest.streaming.SnowflakeStreamingIngestClientFactory;
import net.snowflake.ingest.utils.ParameterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

/**
 * Facade to provide the underlying {@link SnowflakeStreamingIngestClient}. The lifecycle of the
 * created client is not managed in this class.
 */
@Internal
public class SnowflakeStreamingIngestClientProvider {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(SnowflakeStreamingIngestClientProvider.class);

    private static final String STREAMING_INGEST_CLIENT_PREFIX_NAME = "FLINK_INGEST_CLIENT";

    /**
     * Created based on the Snowflake client APIs and configuration. See <a
     * href="https://javadoc.io/doc/net.snowflake/snowflake-ingest-sdk/2.0.2/net/snowflake/ingest/streaming/SnowflakeStreamingIngestClient.html">SnowflakeStreamingIngestClient</a>
     * and <a
     * href="https://docs.snowflake.com/en/user-guide/data-load-snowpipe-streaming-configuration">Configuring
     * Snowpipe Streaming</a>
     *
     * @param ingestName {@link java.lang.String} a name to uniquely identify the upstream
     *     application
     * @param connectorProps {@link java.util.Properties} All required and optional Snowflake
     *     streaming configs
     * @return {@link SnowflakeStreamingIngestClient} A new ingestion client
     */
    public static SnowflakeStreamingIngestClient createClient(
            final String ingestName,
            final Properties connectorProps,
            final SnowflakeWriterConfig writerConfig) {
        final String ingestClientName =
                SnowflakeInternalUtils.createClientOrChannelName(
                        STREAMING_INGEST_CLIENT_PREFIX_NAME, ingestName, null);

        SnowflakeStreamingIngestClient ingestClient =
                SnowflakeStreamingIngestClientFactory.builder(ingestClientName)
                        .setProperties(connectorProps)
                        .setParameterOverrides(getParameterOverrides(writerConfig))
                        .build();

        LOGGER.info(
                "Successfully initialized Snowflake streaming ingest client {}", ingestClientName);
        return ingestClient;
    }

    /**
     * Create a {@link java.util.Map} to provide for overriding the {@link
     * SnowflakeStreamingIngestClient} properties.
     *
     * @param writerConfig {@link SnowflakeWriterConfig}
     * @return {@link java.util.Map}
     */
    private static Map<String, Object> getParameterOverrides(
            final SnowflakeWriterConfig writerConfig) {
        Preconditions.checkNotNull(writerConfig, "writerConfig");
        return Maps.newHashMap(
                Map.of(
                        ParameterProvider.BUFFER_FLUSH_INTERVAL_IN_MILLIS, // buffer time
                        writerConfig.getMaxBufferTimeMs(),
                        ParameterProvider
                                .ENABLE_SNOWPIPE_STREAMING_METRICS, // snowpipe streaming metrics
                        true));
    }
}
