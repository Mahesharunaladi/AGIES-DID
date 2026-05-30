package com.agies.did.service;

import com.agies.did.model.AgentState;
import com.agies.did.model.TrustEvaluationRequest;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.SessionConfig;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StateGraphService implements DisposableBean {
  private final Driver driver;
  private final boolean enabled;
  private final ExecutorService graphExecutor = Executors.newSingleThreadExecutor(runnable -> {
    Thread thread = new Thread(runnable, "stategraph-writer");
    thread.setDaemon(true);
    return thread;
  });

  public StateGraphService(
      @Value("${agies.neo4j.enabled}") boolean enabled,
      @Value("${agies.neo4j.uri}") String uri,
      @Value("${agies.neo4j.username}") String username,
      @Value("${agies.neo4j.password}") String password,
      @Value("${agies.neo4j.timeout-seconds}") long timeoutSeconds
  ) {
    this.enabled = enabled;
    if (!enabled) {
      this.driver = null;
      return;
    }
    Config config = Config.builder()
        .withConnectionTimeout(timeoutSeconds, TimeUnit.SECONDS)
        .withConnectionAcquisitionTimeout(timeoutSeconds, TimeUnit.SECONDS)
        .build();
    this.driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password), config);
  }

  public void record(TrustEvaluationRequest request, double trustScore, String trustBand) {
    if (!enabled) {
      return;
    }
    graphExecutor.submit(() -> writeRecord(request, trustScore, trustBand));
  }

  private void writeRecord(TrustEvaluationRequest request, double trustScore, String trustBand) {
    try (var session = driver.session(SessionConfig.defaultConfig())) {
      session.executeWrite(tx -> {
        tx.run("""
            MERGE (a:Agent {id: $agentId})
            SET a.trustScore = $trustScore,
                a.trustBand = $trustBand,
                a.updatedAt = datetime()
            WITH a
            CREATE (e:TrustEvent {
              prompt: $prompt,
              trustScore: $trustScore,
              trustBand: $trustBand,
              tools: $tools,
              occurredAt: datetime()
            })
            MERGE (a)-[:EMITTED]->(e)
            """, org.neo4j.driver.Values.parameters(
            "agentId", request.agentId(),
            "prompt", request.prompt(),
            "trustScore", trustScore,
            "trustBand", trustBand,
            "tools", request.requestedTools() == null ? List.of() : request.requestedTools()
        ));
        return null;
      });
    } catch (Exception ignored) {
      // The gateway stays usable in dev even if Neo4j is not ready yet.
    }
  }

  public AgentState currentState(String agentId) {
    if (!enabled) {
      return new AgentState(agentId, 0.0d, "GRAPH_DISABLED", List.of(), Instant.now());
    }
    try (var session = driver.session(SessionConfig.defaultConfig())) {
      return session.executeRead(tx -> {
        var result = tx.run("""
            MATCH (a:Agent {id: $agentId})
            OPTIONAL MATCH (a)-[:EMITTED]->(e:TrustEvent)
            WITH a, e ORDER BY e.occurredAt DESC
            WITH a, collect(e.tools)[0] AS recentTools
            RETURN a.trustScore AS trustScore, a.trustBand AS trustBand, recentTools AS recentTools
            """, org.neo4j.driver.Values.parameters("agentId", agentId));
        if (!result.hasNext()) {
          return new AgentState(agentId, 0.0d, "UNKNOWN", List.of(), Instant.now());
        }
        var row = result.next();
        return new AgentState(
            agentId,
            row.get("trustScore").asDouble(0.0d),
            row.get("trustBand").asString("UNKNOWN"),
            row.get("recentTools").asList(value -> value.asString()),
            Instant.now()
        );
      });
    } catch (Exception ignored) {
      return new AgentState(agentId, 0.0d, "UNKNOWN", List.of(), Instant.now());
    }
  }

  @Override
  public void destroy() {
    graphExecutor.shutdownNow();
    if (driver != null) {
      driver.close();
    }
  }
}
