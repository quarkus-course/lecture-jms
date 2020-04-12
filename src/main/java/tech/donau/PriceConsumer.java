package tech.donau;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class PriceConsumer implements Runnable {

    @Inject
    ConnectionFactory factory;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private String lastPrice;

    public String getLastPrice() {
        return lastPrice;
    }

    public void onStart(@Observes StartupEvent event) {
        executorService.execute(this);
    }

    public void onStop(@Observes ShutdownEvent event) {
        executorService.shutdown();
    }

    @Override
    public void run() {
        try (JMSContext context = factory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            final JMSConsumer consumer = context.createConsumer(context.createQueue("prices"));
            while (true) {
                final Message receive = consumer.receive();
                if(receive == null) return;
                lastPrice = receive.getBody(String.class);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
