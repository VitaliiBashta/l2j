package com.l2jserver.commons.util;

import java.security.SecureRandom;
import java.util.Random;

public class Rnd {
  /**
   * This class extends {@link java.util.Random} but do not compare and store atomically.<br>
   * Instead it`s using a simple volatile flag to ensure reading and storing the whole 64bit seed
   * chunk.<br>
   * This implementation is much faster on parallel access, but may generate the same seed for 2
   * threads.
   *
   * @author Forsaiken
   * @see java.util.Random
   */
  public static final class NonAtomicRandom extends Random {
    private static final long serialVersionUID = 1L;
    private volatile long _seed;

    public NonAtomicRandom() {
      this(++SEED_UNIQUIFIER + System.nanoTime());
    }

    public NonAtomicRandom(final long seed) {
      setSeed(seed);
    }

    @Override
    public int next(final int bits) {
      return (int) ((_seed = ((_seed * MULTIPLIER) + ADDEND) & MASK) >>> (48 - bits));
    }

    @Override
    public void setSeed(final long seed) {
      _seed = (seed ^ MULTIPLIER) & MASK;
    }
  }

  /** @author Forsaiken */
  protected static final class RandomContainer {
    private final Random _random;

    protected RandomContainer(final Random random) {
      _random = random;
    }

    public Random directRandom() {
      return _random;
    }

    /**
     * Get a random double number from 0 to 1
     *
     * @return A random double number from 0 to 1
     * @see com.l2jserver.commons.util.Rnd#nextDouble()
     */
    public double get() {
      return _random.nextDouble();
    }

    /**
     * Gets a random integer number from 0(inclusive) to n(exclusive)
     *
     * @param n The superior limit (exclusive)
     * @return A random integer number from 0 to n-1
     */
    public int get(final int n) {
      return (int) (_random.nextDouble() * n);
    }

    /**
     * Gets a random integer number from min(inclusive) to max(inclusive)
     *
     * @param min The minimum value
     * @param max The maximum value
     * @return A random integer number from min to max
     */
    public int get(final int min, final int max) {
      return min + (int) (_random.nextDouble() * ((max - min) + 1));
    }

    /**
     * Gets a random long number from min(inclusive) to max(inclusive)
     *
     * @param min The minimum value
     * @param max The maximum value
     * @return A random long number from min to max
     */
    public long get(final long min, final long max) {
      return min + (long) (_random.nextDouble() * ((max - min) + 1));
    }

    /**
     * Get a random boolean state (true or false)
     *
     * @return A random boolean state (true or false)
     * @see java.util.Random#nextBoolean()
     */
    public boolean nextBoolean() {
      return _random.nextBoolean();
    }

    /**
     * Fill the given array with random byte numbers from Byte.MIN_VALUE(inclusive) to
     * Byte.MAX_VALUE(inclusive)
     *
     * @param array The array to be filled with random byte numbers
     * @see java.util.Random#nextBytes(byte[] bytes)
     */
    public void nextBytes(final byte[] array) {
      _random.nextBytes(array);
    }

    /**
     * Get a random double number from 0 to 1
     *
     * @return A random double number from 0 to 1
     * @see java.util.Random#nextDouble()
     */
    public double nextDouble() {
      return _random.nextDouble();
    }

    /**
     * Get a random float number from 0 to 1
     *
     * @return A random integer number from 0 to 1
     * @see java.util.Random#nextFloat()
     */
    public float nextFloat() {
      return _random.nextFloat();
    }

    /**
     * Get a random gaussian double number from 0 to 1
     *
     * @return A random gaussian double number from 0 to 1
     * @see java.util.Random#nextGaussian()
     */
    public double nextGaussian() {
      return _random.nextGaussian();
    }

    /**
     * Get a random integer number from Integer.MIN_VALUE(inclusive) to Integer.MAX_VALUE(inclusive)
     *
     * @return A random integer number from Integer.MIN_VALUE to Integer.MAX_VALUE
     * @see java.util.Random#nextInt()
     */
    public int nextInt() {
      return _random.nextInt();
    }

    /**
     * Get a random long number from Long.MIN_VALUE(inclusive) to Long.MAX_VALUE(inclusive)
     *
     * @return A random integer number from Long.MIN_VALUE to Long.MAX_VALUE
     * @see java.util.Random#nextLong()
     */
    public long nextLong() {
      return _random.nextLong();
    }
  }

  /** @author Forsaiken */
  public enum RandomType {
    /**
     * For best random quality.
     *
     * @see java.security.SecureRandom
     */
    SECURE,

    /**
     * For average random quality.
     *
     * @see java.util.Random
     */
    UNSECURE_ATOMIC,

    /**
     * Like {@link RandomType#UNSECURE_ATOMIC}.<br>
     * Each thread has it`s own random instance.<br>
     * Provides best parallel access speed.
     *
     * @see ThreadLocalRandom
     */
    UNSECURE_THREAD_LOCAL,

    /**
     * Like {@link RandomType#UNSECURE_ATOMIC}.<br>
     * Provides much faster parallel access speed.
     *
     * @see NonAtomicRandom
     */
    UNSECURE_VOLATILE
  }

  /**
   * This class extends {@link java.util.Random} but do not compare and store atomically.<br>
   * Instead it`s using thread local ensure reading and storing the whole 64bit seed chunk.<br>
   * This implementation is the fastest, never generates the same seed for 2 threads.<br>
   * Each thread has it`s own random instance.
   *
   * @author Forsaiken
   * @see java.util.Random
   */
  public static final class ThreadLocalRandom extends Random {
    private static final class Seed {
      long _seed;

      Seed(final long seed) {
        setSeed(seed);
      }

      int next(final int bits) {
        return (int) ((_seed = ((_seed * MULTIPLIER) + ADDEND) & MASK) >>> (48 - bits));
      }

      void setSeed(final long seed) {
        _seed = (seed ^ MULTIPLIER) & MASK;
      }
    }

    private static final long serialVersionUID = 1L;
    private final ThreadLocal<Seed> _seedLocal;

    public ThreadLocalRandom() {
      _seedLocal =
          new ThreadLocal<>() {
            @Override
            public final Seed initialValue() {
              return new Seed(++SEED_UNIQUIFIER + System.nanoTime());
            }
          };
    }

    public ThreadLocalRandom(final long seed) {
      _seedLocal =
          new ThreadLocal<>() {
            @Override
            public final Seed initialValue() {
              return new Seed(seed);
            }
          };
    }

    @Override
    public int next(final int bits) {
      return _seedLocal.get().next(bits);
    }

    @Override
    public void setSeed(final long seed) {
      if (_seedLocal != null) {
        _seedLocal.get().setSeed(seed);
      }
    }
  }

  private static final long ADDEND = 0xBL;

  private static final long MASK = (1L << 48) - 1;

  private static final long MULTIPLIER = 0x5DEECE66DL;

  private static final RandomContainer rnd = newInstance(RandomType.UNSECURE_THREAD_LOCAL);

  protected static volatile long SEED_UNIQUIFIER = 8682522807148012L;

  public static Random directRandom() {
    return rnd.directRandom();
  }

  /**
   * Get a random double number from 0 to 1
   *
   * @return A random double number from 0 to 1
   * @see com.l2jserver.commons.util.Rnd#nextDouble()
   */
  public static double get() {
    return rnd.nextDouble();
  }

  /**
   * Gets a random integer number from 0(inclusive) to n(exclusive)
   *
   * @param n The superior limit (exclusive)
   * @return A random integer number from 0 to n-1
   */
  public static int get(final int n) {
    return rnd.get(n);
  }

  /**
   * Gets a random integer number from min(inclusive) to max(inclusive)
   *
   * @param min The minimum value
   * @param max The maximum value
   * @return A random integer number from min to max
   */
  public static int get(final int min, final int max) {
    return rnd.get(min, max);
  }

  /**
   * Gets a random long number from min(inclusive) to max(inclusive)
   *
   * @param min The minimum value
   * @param max The maximum value
   * @return A random long number from min to max
   */
  public static long get(final long min, final long max) {
    return rnd.get(min, max);
  }

  public static RandomContainer newInstance(final RandomType type) {
    switch (type) {
      case UNSECURE_ATOMIC:
        return new RandomContainer(new Random());

      case UNSECURE_VOLATILE:
        return new RandomContainer(new NonAtomicRandom());

      case UNSECURE_THREAD_LOCAL:
        return new RandomContainer(new ThreadLocalRandom());

      case SECURE:
        return new RandomContainer(new SecureRandom());
    }

    throw new IllegalArgumentException();
  }

  /**
   * Get a random boolean state (true or false)
   *
   * @return A random boolean state (true or false)
   * @see java.util.Random#nextBoolean()
   */
  public static boolean nextBoolean() {
    return rnd.nextBoolean();
  }

  /**
   * Fill the given array with random byte numbers from Byte.MIN_VALUE(inclusive) to
   * Byte.MAX_VALUE(inclusive)
   *
   * @param array The array to be filled with random byte numbers
   * @see java.util.Random#nextBytes(byte[] bytes)
   */
  public static void nextBytes(final byte[] array) {
    rnd.nextBytes(array);
  }

  /**
   * Get a random double number from 0 to 1
   *
   * @return A random double number from 0 to 1
   * @see java.util.Random#nextDouble()
   */
  public static double nextDouble() {
    return rnd.nextDouble();
  }

  /**
   * Get a random float number from 0 to 1
   *
   * @return A random integer number from 0 to 1
   * @see java.util.Random#nextFloat()
   */
  public static float nextFloat() {
    return rnd.nextFloat();
  }

  /**
   * Get a random gaussian double number from 0 to 1
   *
   * @return A random gaussian double number from 0 to 1
   * @see java.util.Random#nextGaussian()
   */
  public static double nextGaussian() {
    return rnd.nextGaussian();
  }

  /**
   * Get a random integer number from Integer.MIN_VALUE(inclusive) to Integer.MAX_VALUE(inclusive)
   *
   * @return A random integer number from Integer.MIN_VALUE to Integer.MAX_VALUE
   * @see java.util.Random#nextInt()
   */
  public static int nextInt() {
    return rnd.nextInt();
  }

  /**
   * Gets a random integer number from 0 (inclusive) to n (exclusive).
   *
   * @param n the superior limit (exclusive)
   * @return a random integer number from 0 to n-1
   * @see com.l2jserver.commons.util.Rnd#get(int n)
   */
  public static int nextInt(final int n) {
    return get(n);
  }

  /**
   * Get a random long number from Long.MIN_VALUE(inclusive) to Long.MAX_VALUE(inclusive)
   *
   * @return A random integer number from Long.MIN_VALUE to Long.MAX_VALUE
   * @see java.util.Random#nextLong()
   */
  public static long nextLong() {
    return rnd.nextLong();
  }

}
