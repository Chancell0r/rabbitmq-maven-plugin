package com.github.iesen.rabbitmq.plugin.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.github.iesen.rabbitmq.plugin.RabbitMQConstants;
import com.google.common.collect.Lists;

/**
 */
public class MacRabbitManager implements RabbitManager {

    private static final int BUFFER = 2048;
    private final Log log;

    public MacRabbitManager(Log log) {
        this.log = log;
    }

    @Override
    public boolean rabbitExtracted() {
        RabbitMQConstants rabbitMQConstants = RabbitMQConstants.getInstance();
        File rabbitHome = new File(rabbitMQConstants.getRabbitMqHome());
        log.info("Checking " + rabbitHome.getAbsolutePath());
        boolean exists = rabbitHome.exists();
        log.info("RabbitMQ " + (exists ? "found" : "not found"));
        return exists;
    }

    @Override
    public void extractServer() throws MojoExecutionException {
        try {
            RabbitMQConstants rabbitMQConstants = RabbitMQConstants.getInstance();
            String rabbitDownloadUrl = "https://www.rabbitmq.com/releases/rabbitmq-server/v" + rabbitMQConstants.getRabbitMqVersion() + "/rabbitmq-server-mac-standalone-" + rabbitMQConstants.getRabbitMqVersion() + ".tar.xz";
            log.debug("Downloading rabbitmq from " + rabbitDownloadUrl);
            FileUtils.download(rabbitDownloadUrl, rabbitMQConstants.getRabbitMqParentDir() + File.separator + "rabbitmq-server-mac-standalone-" + rabbitMQConstants.getRabbitMqVersion() + ".tar.xz");
            log.debug("Extracting downloaded files");
            FileUtils.extractTarXz(rabbitMQConstants.getRabbitMqParentDir() + File.separator + "rabbitmq-server-mac-standalone-" + rabbitMQConstants.getRabbitMqVersion() + ".tar.xz", rabbitMQConstants.getRabbitMqParentDir());
            // Give permissions
            ProcessBuilder permissionProcess = new ProcessBuilder("/bin/chmod", "-R", "777", rabbitMQConstants.getRabbitMqParentDir());
            log.debug("Permission command " + permissionProcess.command());
            Process permission = permissionProcess.start();
            permission.waitFor();
            // Enable management
            ProcessBuilder managementEnabler = new ProcessBuilder(rabbitMQConstants.getRabbitMqHome() + File.separator + "sbin" + File.separator + "rabbitmq-plugins", "enable", "rabbitmq_management");
            log.debug("Enable management " + managementEnabler.command());
            Process mgmt = managementEnabler.start();
            mgmt.waitFor();
        } catch (IOException e) {
            throw new MojoExecutionException("Error extracting server", e);
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Error executing process", e);
        }
    }

    @Override
    public void removeRabbitMq() throws MojoExecutionException {
        try {
            RabbitMQConstants rabbitMQConstants = RabbitMQConstants.getInstance();
            ProcessBuilder removeRabbitMqProcess = new ProcessBuilder("/bin/rm", "-rf", rabbitMQConstants.getRabbitMqParentDir());
            log.debug("Permission command " + removeRabbitMqProcess.command());
            Process permission = removeRabbitMqProcess.start();
            permission.waitFor();
        } catch (IOException e) {
            throw new MojoExecutionException("Error extracting server", e);
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Error executing process", e);
        }
    }

    @Override
    public boolean isRabbitRunning() throws MojoExecutionException {
        try {
            RabbitMQConstants rabbitMQConstants = RabbitMQConstants.getInstance();
            String rabbitctlPath = rabbitMQConstants.getRabbitMqHome() + File.separator + "sbin" + File.separator + "rabbitmqctl";
            if (!(new File(rabbitctlPath).exists())) {
                throw new MojoExecutionException("RabbitMQ has not started yet");
            }
            ProcessBuilder statusProcessBuilder = new ProcessBuilder(rabbitMQConstants.getRabbitMqHome() + File.separator + "sbin" + File.separator + "rabbitmqctl", "status");
            log.info(statusProcessBuilder.command().toString());
            Process statusProcess = statusProcessBuilder.start();
            String statusLine;
            StringBuilder statusResult = new StringBuilder();
            BufferedReader inStatus = new BufferedReader(new InputStreamReader(statusProcess.getInputStream()));
            while ((statusLine = inStatus.readLine()) != null) {
                log.debug(statusLine);
                statusResult.append(statusLine).append('\n');
            }
            inStatus.close();
            return statusResult.toString().contains("pid");
        } catch (IOException e) {
            throw new MojoExecutionException("Error checking server status via rabbitmqctl", e);
        }
    }

    @Override
    public void stop() throws MojoExecutionException {
        try {
            RabbitMQConstants rabbitMQConstants = RabbitMQConstants.getInstance();
            String rabbitctlPath = rabbitMQConstants.getRabbitMqHome() + File.separator + "sbin" + File.separator + "rabbitmqctl";
            if (!(new File(rabbitctlPath).exists())) {
                throw new MojoExecutionException("RabbitMQ is not started");
            }
            ProcessBuilder statusProcessBuilder = new ProcessBuilder(rabbitctlPath, "stop");
            Process statusProcess = statusProcessBuilder.start();
            String line;
            BufferedReader inStop = new BufferedReader(new InputStreamReader(statusProcess.getInputStream()));
            while ((line = inStop.readLine()) != null) {
                log.debug(line);
            }
            inStop.close();
            statusProcess.waitFor();
        } catch (IOException e) {
            throw new MojoExecutionException("Error stopping server via rabbitmqctl", e);
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Error waiting server stop", e);
        }
    }

    @Override
    public void start(String port, boolean detached) throws MojoExecutionException {
        try {
            RabbitMQConstants rabbitMQConstants = RabbitMQConstants.getInstance();
            List<String> startCommand = Lists.newArrayList(rabbitMQConstants.getRabbitMqHome() + File.separator + "sbin" + File.separatorChar + "rabbitmq-server");
            if (detached) {
                startCommand.add("-detached");
            }
            ProcessBuilder processBuilder = new ProcessBuilder(startCommand);
            log.info("Starting broker:" + processBuilder.command());
            processBuilder.environment().put("RABBITMQ_NODE_PORT", port);
            Process p = processBuilder.start();
            int result = p.waitFor();
            log.debug("Start result: " + result);
            if(isRabbitRunning())
               log.info("RabbitMQ Running on: " + port);
            List<String> managementCommand= Lists.newArrayList(rabbitMQConstants.getRabbitMqHome() + File.separator + "sbin" + File.separator + "rabbitmq-plugins", "enable",
               "rabbitmq_management");
            ProcessBuilder managementEnabler = new ProcessBuilder(managementCommand);
            log.info("Enable management" + managementEnabler.command());
            Process mgmtProcess = managementEnabler.start();
            mgmtProcess.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException("Error starting server", e);
        }
    }

    @Override
    public void installErlang() throws MojoExecutionException {
        throw new UnsupportedOperationException("Erlang is bundled in OS X systems");
    }

    @Override
    public boolean isErlangInstalled() throws MojoExecutionException {
        return true;
    }

}
