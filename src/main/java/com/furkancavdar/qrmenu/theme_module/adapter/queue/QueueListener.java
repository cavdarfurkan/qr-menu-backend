package com.furkancavdar.qrmenu.theme_module.adapter.queue;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueListener implements SmartLifecycle {

  private volatile boolean isRunning = false;
  private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

  //    @Qualifier("queueConsumerClient")
  //    private final RedisTemplate<String, String> redisTemplate;

  private final StringRedisTemplate redisTemplate;

  //    public QueueListener(@Qualifier("queueConsumerClient") RedisTemplate<String, String>
  // redisTemplate) {
  //        this.redisTemplate = redisTemplate;
  //    }

  @Override
  public void start() {
    isRunning = true;
    Thread.startVirtualThread(this::listen);
  }

  @Override
  public void stop() {
    isRunning = false;
    try {
      executor.shutdown();
      if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public boolean isRunning() {
    return this.isRunning;
  }

  private void listen() {
    while (isRunning) {
      try {
        ListOperations.MoveFrom<String> from =
            ListOperations.MoveFrom.fromHead("queue:build:completed:main");
        ListOperations.MoveTo<String> to =
            ListOperations.MoveTo.toTail("queue:build:completed:process");

        String message = redisTemplate.opsForList().move(from, to, Duration.ofSeconds(0));
        if (message == null) {
          Thread.sleep(1);
          continue;
        }

        executor.submit(() -> processMessage(message));
      } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
        try {
          Thread.sleep(1);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  private void processMessage(String message) {
    try {
      // TODO
      System.out.println("process: " + message);
      // testEx();
      //            Thread.sleep(Duration.ofSeconds(10));

      redisTemplate.opsForList().remove("queue:build:completed:process", 1, message);
    } catch (Exception e) {
      System.err.println("Error processing message: " + e.getMessage());
      handleFailedMessage(message);
    }
  }

  private void handleFailedMessage(String message) {
    // Return to main queue or handle as needed
    // redisTemplate.opsForList().rightPush("queue:build:completed:main", message);
    // Clean up process queue
    // redisTemplate.opsForList().remove("queue:build:completed:process", 1, message);

    // TODO
    System.out.println("handleFailedMessage: " + message);
  }

  private void testEx() {
    throw new RuntimeException("testEx");
  }
}
