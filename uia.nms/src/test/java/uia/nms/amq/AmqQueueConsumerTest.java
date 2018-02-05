package uia.nms.amq;

import org.junit.Test;

import uia.nms.MessageBody;
import uia.nms.MessageHeader;
import uia.nms.NmsConsumer;
import uia.nms.NmsEndPoint;
import uia.nms.NmsMessageListener;
import uia.nms.NmsProducer;

public class AmqQueueConsumerTest {

    @Test
    public void testCom() throws Exception {
        NmsEndPoint endPoint = new NmsEndPoint(null, null, "tcp://10.160.1.53", "61616");

        NmsConsumer sub = new AmqQueueFactory().createConsumer(endPoint);

        sub.addLabel("value");
        sub.addMessageListener(new NmsMessageListener() {

            @Override
            public void messageReceived(NmsConsumer sub, MessageHeader header, MessageBody body) {
                System.out.println("got it");
                System.out.println(body.getContent());
            }
        });

        sub.start("HTKS.FME.DC.PLAN.S");
        Thread.sleep(5000);
        sub.stop();
    }

    @Test
    public void testPubReply1() throws Exception {
        NmsEndPoint endPoint = new NmsEndPoint(null, null, "tcp://10.160.1.53", "61616");

        final NmsProducer pub = new AmqQueueFactory().createProducer(endPoint);
        NmsConsumer sub = new AmqQueueFactory().createConsumer(endPoint);

        sub.addLabel("value");
        sub.addMessageListener(new NmsMessageListener() {

            @Override
            public void messageReceived(NmsConsumer sub, MessageHeader header, MessageBody body) {
                System.out.println("Receive: " + body.getContent().get("value"));
                System.out.println("Reply To: " + header.responseSubject);
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {

                }
                pub.send(header.responseSubject, "value", "You are cute", false, header.correlationID);
            }
        });

        sub.start("HTKS.FME.DC.PLAN.S");
        pub.start();
        String result = pub.send("HTKS.FME.DC.PLAN.S", "value", "xxxx", false, 3000, "HTKS.FME.DC.PLAN.R");
        System.out.println("Get reply: " + result);
        Thread.sleep(2000);

        pub.stop();
        sub.stop();
    }

    @Test
    public void testPubReply2() throws Exception {
        NmsEndPoint endPoint = new NmsEndPoint(null, null, "tcp://localhost", "61616");

        final NmsProducer pub = new AmqQueueFactory().createProducer(endPoint);
        NmsConsumer sub = new AmqQueueFactory().createConsumer(endPoint);

        sub.addLabel("xml");
        sub.addMessageListener(new NmsMessageListener() {

            @Override
            public void messageReceived(NmsConsumer sub, MessageHeader header, MessageBody body) {
                System.out.println("Receive: " + body.getContent().get("xml"));
                System.out.println("Reply To: " + header.responseSubject);
                pub.send(header.responseSubject, "xml", "You are cute", false, header.correlationID);
            }
        });

        sub.start("Judy.Test");
        pub.start();
        String result = pub.send("Judy.Test", "xml", "Judy", false, 3000);
        System.out.println("Get reply: " + result);
        Thread.sleep(2000);

        pub.stop();
        sub.stop();
    }
}