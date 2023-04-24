package com.example.demo;

import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
@RestController
@Slf4j
public class DemoApplication {

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("/")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("hello, Daddy");
    }

    @GetMapping(value = "/data")
    public ResponseEntity<StreamingResponseBody> streamData() {
        log.info("Start stream");
        StreamingResponseBody responseBody = response -> {
            for (int i = 1; i <= 100; i++) {
                try {
                    Thread.sleep(100);
                    log.info("Streaming");
                    response.write(("Data stream line - " + i + "\n").getBytes());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(responseBody);
    }

    @GetMapping("/rbe")
    public ResponseEntity<ResponseBodyEmitter> handleRbe() {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
        executorService.execute(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    emitter.send(
                            "/rbe" + " @ " + new Date(), MediaType.TEXT_PLAIN);
                    sleep(100);
                }
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        return ResponseEntity.ok(emitter);
    }

    @GetMapping("/sse")
    public SseEmitter handleSse() {
        SseEmitter emitter = new SseEmitter();
        executorService.execute(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    emitter.send("/sse" + " @ " + new Date());
                    sleep(100);
                }
                // we could send more events
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    @SneakyThrows
    @PostMapping("post")
    public ResponseEntity<String> get(HttpServletRequest request) {
        InputStream inputStream = request.getInputStream();
        return ResponseEntity.ok("hello");
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            log.error("Err", e);
        }
    }
}