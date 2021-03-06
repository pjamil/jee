package com.hclc.jee.uuid.generation.batch.cachedjson.control;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import static com.hclc.jee.uuid.generation.Parameters.CACHED_JSON_GENERATION_THREADS;
import static com.hclc.jee.uuid.generation.Parameters.CACHED_JSON_NUMBER_OF_CACHED_BATCHES;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Startup
public class CachedJsonGenerator {
    private final BlockingQueue<JsonArray> batches = new LinkedBlockingQueue<>(CACHED_JSON_NUMBER_OF_CACHED_BATCHES);
    private List<Future<?>> futures = new ArrayList<>(CACHED_JSON_GENERATION_THREADS);

    @Resource(lookup = "java:jboss/ee/concurrency/executor/cachedJsonGeneratorExecutor")
    ManagedExecutorService cachedJsonGeneratorExecutor;

    @PostConstruct
    public void startGeneration() {
        for (int i = 0; i < CACHED_JSON_GENERATION_THREADS; i++) {
            futures.add(cachedJsonGeneratorExecutor.submit(new CachedJsonGenerationThread(batches)));
        }
    }

    public JsonArray getNextBatch() {
        try {
            return batches.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonObject status() {
        return Json.createObjectBuilder()
                .add("size", batches.size())
                .add("max", CACHED_JSON_NUMBER_OF_CACHED_BATCHES)
                .build();
    }

    public double fillFactor() {
        return 1.0 * batches.size() / CACHED_JSON_NUMBER_OF_CACHED_BATCHES;
    }

    @PreDestroy
    public void stopGeneration() {
        futures.stream().forEach(f -> f.cancel(true));
    }
}
