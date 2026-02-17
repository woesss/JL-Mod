package javax.microedition.lcdui.event;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class EventQueueTest {

    private EventQueue eventQueue;

    @Before
    public void setUp() {
        eventQueue = new EventQueue();
        eventQueue.startProcessing();
    }

    @After
    public void tearDown() {
        eventQueue.stopProcessing();
    }

    @Test
    public void testPostAndProcessEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] executed = {false};

        Event event = new TestEvent() {
            @Override
            public void process() {
                executed[0] = true;
                latch.countDown();
            }
        };

        eventQueue.postEvent(event);
        assertTrue("Event not processed in time", latch.await(2, TimeUnit.SECONDS));
        assertTrue(executed[0]);
    }

    @Test
    public void testMultipleEvents() throws InterruptedException {
        int count = 10;
        CountDownLatch latch = new CountDownLatch(count);
        AtomicInteger executedCount = new AtomicInteger(0);

        for (int i = 0; i < count; i++) {
            eventQueue.postEvent(new TestEvent() {
                @Override
                public void process() {
                    executedCount.incrementAndGet();
                    latch.countDown();
                }
            });
        }

        assertTrue("Events not processed in time", latch.await(5, TimeUnit.SECONDS));
        assertEquals(count, executedCount.get());
    }

    @Test
    public void testStressTest() throws InterruptedException {
        // Post many events from multiple threads
        int threadCount = 4;
        int eventsPerThread = 100;
        int totalEvents = threadCount * eventsPerThread;
        CountDownLatch latch = new CountDownLatch(totalEvents);
        AtomicInteger executedCount = new AtomicInteger(0);

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < eventsPerThread; j++) {
                    eventQueue.postEvent(new TestEvent() {
                        @Override
                        public void process() {
                            executedCount.incrementAndGet();
                            latch.countDown();
                        }
                    });
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        assertTrue("Stress test events not processed in time", latch.await(10, TimeUnit.SECONDS));
        assertEquals(totalEvents, executedCount.get());
    }

    // Abstract Event implementation for testing to avoid Android dependencies
    abstract static class TestEvent extends Event {
        @Override
        public abstract void process();

        @Override
        public void recycle() {
        }

        @Override
        public void enterQueue() {
        }

        @Override
        public void leaveQueue() {
        }

        @Override
        public boolean placeableAfter(Event event) {
            return true;
        }

        // Override run to avoid calling Log.e which is stubbed
        @Override
        public void run() {
            try {
                process();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                leaveQueue();
                recycle();
            }
        }
    }
}
