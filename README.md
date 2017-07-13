#RabbitMQ Maven Plugin

[![Join the chat at https://gitter.im/iesen/rabbitmq-maven-plugin](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/iesen/rabbitmq-maven-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

A maven plugin that downloads, install and configures a RabbitMQ instance within maven lifecycle

## Example Usage

Below is an example configuration for the plugin. You can specify the port, rabbitmq version, rabbit mq install directory, and if you would like rabbitmq removed on stop. You can add exchanges, queues and their binding in the configuration section.

```xml
<plugin>
    <groupId>com.github.kohlsj</groupId>
    <artifactId>rabbitmq-maven-plugin</artifactId>
    <version>0.1.10</version>
    <configuration>
        <detached>true</detached>
        <port>5672</port>
        <installDirectory>target/rabbitmq</installDirectory>
        <cleanUpRabbit>true</cleanUpRabbit>
        <version>3.6.9</version>
        <exchanges>
            <exchange>
                <name>myexchange</name>
                <type>topic</type>
            </exchange>
            <queues>
                <queue>
                    <name>myqueue</name>
                    <bindings>
                        <binding>
                            <exchangeName>myexchange</exchangeName>
                            <routingKey>message.pingpong.*</routingKey>
                        </binding>
                    </bindings>
                </queue>
            </queues>
        </exchanges>
    </configuration>
</plugin>
```
##Configuration Values:

installDirectory : default is ~/.rabbitmq_maven_plugin (.rabbitmq_maven_plugin will append to the installDirectory if specified) <br>
cleanUpRabbit : will remove directory specified by installDirectory + .rabbitmq_maven_plugin<br>
port : specify the port that rabbitmq will be ran on.<br>
version : rabbitMq version. <br>
alwaysRestart : if rmq is already running this specifies whether or not to restart during mvn rabbitmq:start

##More Information

The plugin first checks if RabbitMQ is installed at the specified directory before, if not then it will install it
(also installs Erlang runtime if OS is Windows). Just after installation it enables the management plugin and applies
the settings specified in the plugin configuration.

##Supported Platforms

Windows and Mac OS is supported.

Linux is supported but without erlang support.




