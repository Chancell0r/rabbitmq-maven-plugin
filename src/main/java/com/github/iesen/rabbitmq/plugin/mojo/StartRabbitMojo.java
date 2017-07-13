package com.github.iesen.rabbitmq.plugin.mojo;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.github.iesen.rabbitmq.plugin.RabbitMQConstants;
import com.github.iesen.rabbitmq.plugin.RabbitManagerFactory;
import com.github.iesen.rabbitmq.plugin.api.RabbitMQRestClient;
import com.github.iesen.rabbitmq.plugin.api.RabbitMQRestClientImpl;
import com.github.iesen.rabbitmq.plugin.manager.RabbitManager;
import com.github.iesen.rabbitmq.plugin.mojo.parameter.Exchange;
import com.github.iesen.rabbitmq.plugin.mojo.parameter.Queue;

/**
 * Starts rabbitmq and configures exchanges, queues and bindings.
 * Downloads rabbitmq if it is not present.
 */
@Mojo(name = "start")
public class StartRabbitMojo extends AbstractMojo {

    @Parameter(defaultValue = "true")
    private boolean alwaysRestart = true;

    @Parameter(defaultValue = RabbitMQConstants.RABBITMQ_DEFAULT_PORT)
    private String port;

    @Parameter(defaultValue = "true")
    private boolean detached;

    @Parameter
    private String installDirectory;

    @Parameter(defaultValue = RabbitMQConstants.RABBITMQ_DEFAULT_VERSION)
    private String version;

    @Parameter
    private List<Exchange> exchanges;

    @Parameter
    private List<Queue> queues;

    private RabbitMQRestClient rabbitMQRestClient;

    public StartRabbitMojo() {
        rabbitMQRestClient = new RabbitMQRestClientImpl("http://localhost:15672", "guest", "guest", getLog());
    }

    @Override
    public void execute() throws MojoExecutionException {
        RabbitMQConstants.createInstance(version, installDirectory);
        final RabbitManager manager = RabbitManagerFactory.create(getLog());
        if (!manager.rabbitExtracted()) {
            manager.extractServer();
        }
        if (!manager.isErlangInstalled()) {
            manager.installErlang();
        }
        if (manager.isRabbitRunning() && alwaysRestart) {
            getLog().info("RabbitMQ already running...");
            getLog().info("Restarting RabbitMQ...");
            manager.stop();
        }

        if (!manager.isRabbitRunning()) {
            manager.start(port, detached);
        }
        // Configure exchanges and queues
        if (exchanges != null) {
            getLog().debug("Exchanges : " + ToStringBuilder.reflectionToString(exchanges));
            rabbitMQRestClient.createExchanges(exchanges);
        }
        if (queues != null) {
            getLog().debug("Queues : " + ToStringBuilder.reflectionToString(queues));
            rabbitMQRestClient.createQueues(queues);
        }
    }

    public void setRabbitMQRestClient(final RabbitMQRestClient rabbitMQRestClient) {
        this.rabbitMQRestClient = rabbitMQRestClient;
    }

    public void setExchanges(final List<Exchange> exchanges) {
        this.exchanges = exchanges;
    }

    public void setQueues(final List<Queue> queues) {
        this.queues = queues;
    }
}

