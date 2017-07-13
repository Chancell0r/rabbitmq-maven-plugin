package com.github.iesen.rabbitmq.plugin.manager;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.github.iesen.rabbitmq.plugin.RabbitMQConstants;

/**
 */
public class LinuxRabbitManager extends MacRabbitManager {

    private static final int BUFFER = 2048;

    private final Log log;

    public LinuxRabbitManager(final Log log) {
        super(log);
        this.log = log;
    }

    @Override
    public void extractServer() throws MojoExecutionException {
        try {
            final RabbitMQConstants rabbitMQConstants = RabbitMQConstants.getInstance();
            final String rabbitDownloadUrl =
                    "https://www.rabbitmq.com/releases/rabbitmq-server/v" + rabbitMQConstants.getRabbitMqVersion() +
                    "/rabbitmq-server-generic-unix-" + rabbitMQConstants.getRabbitMqVersion() + ".tar.xz";
            log.debug("Downloading rabbitmq from " + rabbitDownloadUrl);
            FileUtils.download(rabbitDownloadUrl,
                    rabbitMQConstants.getRabbitMqParentDir() + File.separator + "rabbitmq-server-mac-standalone-" +
                    rabbitMQConstants.getRabbitMqVersion() + ".tar.xz");
            log.debug("Extracting downloaded files");
            FileUtils.extractTarXz(
                    rabbitMQConstants.getRabbitMqParentDir() + File.separator + "rabbitmq-server-mac-standalone-" +
                    rabbitMQConstants.getRabbitMqVersion() + ".tar.xz", rabbitMQConstants.getRabbitMqParentDir());
            // Give permissions
            final ProcessBuilder permissionProcess = new ProcessBuilder("/bin/chmod", "-R", "777", rabbitMQConstants.getRabbitMqParentDir());
            log.debug("Permission command " + permissionProcess.command());
            final Process permission = permissionProcess.start();
            permission.waitFor();
            // Enable management
            final ProcessBuilder managementEnabler = new ProcessBuilder(
                    rabbitMQConstants.getRabbitMqHome() + File.separator + "sbin" + File.separator +
                    "rabbitmq-plugins", "enable", "rabbitmq_management");
            log.debug("Enable management " + managementEnabler.command());
            final Process mgmt = managementEnabler.start();
            mgmt.waitFor();
        } catch (final IOException e) {
            throw new MojoExecutionException("Error extracting server", e);
        } catch (final InterruptedException e) {
            throw new MojoExecutionException("Error executing process", e);
        }
    }

    @Override
    public void installErlang() throws MojoExecutionException {
        throw new MojoExecutionException("unsupported operating system");
    }

    @Override
    public boolean isErlangInstalled() throws MojoExecutionException {
        final ProcessBuilder permissionProcess = new ProcessBuilder("erl", "-version");
        log.debug("Erlang version command " + permissionProcess.command());
        Process permission = null;
        try {
            permission = permissionProcess.start();
            permission.waitFor();
        } catch (final IOException e) {
            throw new MojoExecutionException("Erlang is not installed", e);
        } catch (final InterruptedException e) {
            throw new MojoExecutionException("Erlang is not installed", e);
        }
        return true;
    }

}
