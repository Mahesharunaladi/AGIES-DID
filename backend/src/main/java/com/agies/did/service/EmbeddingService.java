package com.agies.did.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {
  private static final int DIMENSIONS = 384;

  public double[] embed(String text) {
    double[] vector = new double[DIMENSIONS];
    byte[] bytes = text == null ? new byte[0] : text.getBytes(StandardCharsets.UTF_8);
    for (int i = 0; i < DIMENSIONS; i++) {
      byte[] digest = sha256(bytes, i);
      int raw = Byte.toUnsignedInt(digest[i % digest.length]);
      vector[i] = (raw / 127.5d) - 1.0d;
    }
    return normalize(vector);
  }

  private byte[] sha256(byte[] bytes, int salt) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.update((byte) salt);
      return digest.digest(bytes);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is unavailable", e);
    }
  }

  private double[] normalize(double[] vector) {
    double magnitude = 0.0d;
    for (double value : vector) {
      magnitude += value * value;
    }
    magnitude = Math.sqrt(magnitude);
    if (magnitude == 0.0d) {
      return vector;
    }
    for (int i = 0; i < vector.length; i++) {
      vector[i] = vector[i] / magnitude;
    }
    return vector;
  }
}
