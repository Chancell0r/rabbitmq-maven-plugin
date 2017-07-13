package com.github.iesen.rabbitmq.plugin.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.github.iesen.rabbitmq.plugin.RabbitMQConstants;
import com.github.iesen.rabbitmq.plugin.RabbitManagerFactory;
import com.github.iesen.rabbitmq.plugin.manager.RabbitManager;

/**
 * Stops rabbitmq server
 */
@Mojo(name = "stop")
public class StopRabbitMojo extends AbstractMojo {

    @Parameter(defaultValue = "false")
    private boolean cleanUpRabbit;

    @Parameter
    private String installDirectory;

    @Parameter(defaultValue = RabbitMQConstants.RABBITMQ_DEFAULT_PORT)
    private String port;

    @Parameter(defaultValue = RabbitMQConstants.RABBITMQ_DEFAULT_VERSION)
    private String version;

    public void execute() throws MojoExecutionException {
        RabbitMQConstants.createInstance(version, installDirectory);
        RabbitManager manager = RabbitManagerFactory.create(getLog());
        if (!manager.isRabbitRunning()) {
            cleanUpRabbit(manager);
            throw new MojoExecutionException("RabbitMQ is not started");
        }
        if (!manager.rabbitExtracted()) {
            throw new MojoExecutionException("RabbitMQ is not extracted");
        }
        getLog().info("Stopping RabbitMQ...");
        manager.stop();
        cleanUpRabbit(manager);
    }

    private void cleanUpRabbit(RabbitManager manager) throws MojoExecutionException {
        if(cleanUpRabbit) {
            getLog().info("Removing RabbitMQ...");
            manager.removeRabbitMq();
        }
    }

}
