package com.accenture.newuserjms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.annotation.PostConstruct;
import javax.jms.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

@Service
public class JmsClient {

    @Autowired
    private ConnectionFactory connectionFactory;
    @Autowired
    private Queue receiveUserDiscountXmlQueue;
    @Autowired
    private Queue sendNewUsersXmlQueue;
    @Autowired
    private Queue userQueue;

    Connection connection;
    Session session;
    String login;

    @PostConstruct
    public void init() throws JMSException {
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        MessageConsumer consumer = session.createConsumer(sendNewUsersXmlQueue);
        connection.start();
        consumer.setMessageListener(
                new MessageListener() {
                    public void onMessage(Message message) {

                        try {
                            String text = ((TextMessage) message).getText();
                            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                            DocumentBuilder builder = factory.newDocumentBuilder();
                            ByteArrayInputStream input = new ByteArrayInputStream(text.getBytes("UTF-8"));
                            Document doc = builder.parse(input);
                            login = doc.getDocumentElement().getAttribute("login");
                            sendMess(login);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void sendMess(String l) throws Exception {
        Message message;
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        message = session.createMessage();
        message.setStringProperty("login", l);
        message.setBooleanProperty("accepted", true);
        MessageProducer producer = session.createProducer(userQueue);
        producer.send(message);
        producer.close();
    }
}

