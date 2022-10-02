package com.github.senocak.config

import com.github.senocak.listener.RabbitMqListener
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMqConfig {
    @Value("\${app.rabbitmq.HOST}") private val RABBITMQ_HOST: String? = null
    @Value("\${app.rabbitmq.PORT}") private val RABBITMQ_PORT: Int = 0
    @Value("\${app.rabbitmq.USER}") private val RABBITMQ_USER: String? = null
    @Value("\${app.rabbitmq.SECRET}") private val RABBITMQ_SECRET: String? = null
    @Value("\${app.rabbitmq.EXCHANGE}") private val EXCHANGE: String? = null
    @Value("\${app.rabbitmq.QUEUE}") private val QUEUE: String? = null
    @Value("\${app.rabbitmq.ROUTING_KEY}") private val ROUTING_KEY: String? = null

    /**
     * @return the queue
     */
    @Bean
    fun queue(): Queue {
        return Queue(QUEUE, false)
    }

    /**
     * @return the exchange
     */
    @Bean
    fun exchange(): TopicExchange {
        return TopicExchange(EXCHANGE)
    }

    /**
     * @param queue the queue to set
     * @param exchange the exchange to set
     * @return the binding
     */
    @Bean
    fun binding(queue: Queue?, exchange: TopicExchange?): Binding {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY)
    }

    /**
     * @param connectionFactory the connectionFactory to set
     * @param listenerAdapter the listenerAdapter to set
     * @return the container
     */
    @Bean
    fun container(connectionFactory: ConnectionFactory?, listenerAdapter: MessageListenerAdapter?):
            SimpleMessageListenerContainer {
        val container = SimpleMessageListenerContainer()
        container.connectionFactory = connectionFactory!!
        container.setQueueNames(QUEUE)
        container.setMessageListener(listenerAdapter!!)
        return container
    }

    /**
     * @param receiver the receiver to set
     * @return the listenerAdapter
     */
    @Bean
    fun listenerAdapter(receiver: RabbitMqListener?): MessageListenerAdapter {
        return MessageListenerAdapter(receiver, "receiveMessage")
    }

    /**
     * @return the connectionFactory
     */
    @Bean
    fun connectionFactory(): ConnectionFactory {
        val connectionFactory = CachingConnectionFactory()
        connectionFactory.host = RABBITMQ_HOST!!
        connectionFactory.port = RABBITMQ_PORT
        connectionFactory.username = RABBITMQ_USER!!
        connectionFactory.setPassword(RABBITMQ_SECRET!!)
        return connectionFactory
    }
}
