package io.vepo.redes;

public class MetricPropagation<T extends Number> {
    private enum Type {
        ADDITIVE,
        MULTIPLICATIVE,
        CONCAVE,
        COMPLEMENTARY_MULTIPLICATIVE
    }

    public static <T extends Number> MetricPropagation<T> fromAdditive(T value) {
        return new MetricPropagation<>(value, Type.ADDITIVE);
    }

    public static <T extends Number> MetricPropagation<T> fromMultiplicative(T value) {
        return new MetricPropagation<>(value, Type.MULTIPLICATIVE);
    }

    public static <T extends Number> MetricPropagation<T> fromConcave(T value) {
        return new MetricPropagation<>(value, Type.CONCAVE);
    }

    public static <T extends Number> MetricPropagation<T> fromComplementaryMultiplicative(T value) {
        return new MetricPropagation<>(value, Type.COMPLEMENTARY_MULTIPLICATIVE);
    }

    private final T value;
    private final Type type;

    private MetricPropagation(T value, Type type) {
        this.value = value;
        this.type = type;
    }

    public MetricPropagation<T> and(T newValue) {
        return switch (type) {
            case ADDITIVE -> new MetricPropagation<>(add(value, newValue), type);
            case MULTIPLICATIVE -> new MetricPropagation<>(multiply(value, newValue), type);
            case CONCAVE -> new MetricPropagation<>(min(value, newValue), type);
            case COMPLEMENTARY_MULTIPLICATIVE -> new MetricPropagation<>(complementaryMultiply(value, newValue), type);
        };
    }

    @SuppressWarnings("unchecked")
    private T add(T a, T b) {
        if (a instanceof Integer) {
            return (T) ((Integer) (a.intValue() + b.intValue()));
        } else if (a instanceof Long) {
            return (T) ((Long) (a.longValue() + b.longValue()));
        } else if (a instanceof Double) {
            return (T) ((Double) (a.doubleValue() + b.doubleValue()));
        } else if (a instanceof Float) {
            return (T) ((Float) (a.floatValue() + b.floatValue()));
        }
        throw new IllegalStateException("Unknown Type!");
    }

    @SuppressWarnings("unchecked")
    private T multiply(T a, T b) {
        if (a instanceof Integer) {
            return (T) ((Integer) (a.intValue() * b.intValue()));
        } else if (a instanceof Long) {
            return (T) ((Long) (a.longValue() * b.longValue()));
        } else if (a instanceof Double) {
            return (T) ((Double) (a.doubleValue() * b.doubleValue()));
        } else if (a instanceof Float) {
            return (T) ((Float) (a.floatValue() * b.floatValue()));
        }
        throw new IllegalStateException("Unknown Type!");
    }

    @SuppressWarnings("unchecked")
    private T complementaryMultiply(T a, T b) {
        if (a instanceof Integer) {
            throw new IllegalStateException("Integer cannot be Complementary Multiplicative");
        } else if (a instanceof Long) {
            throw new IllegalStateException("Long cannot be Complementary Multiplicative");
        } else if (a instanceof Double) {
            return (T) ((Double) (1 - ((1 - a.doubleValue()) * (1 - b.doubleValue()))));
        } else if (a instanceof Float) {
            return (T) ((Float) (1 - ((1 - a.floatValue()) * (1 - b.floatValue()))));
        }
        throw new IllegalStateException("Unknown Type!");
    }

    @SuppressWarnings("unchecked")
    private T min(T a, T b) {
        if (a instanceof Integer) {
            return (T) ((Integer) (Math.min(a.intValue(), b.intValue())));
        } else if (a instanceof Long) {
            return (T) ((Long) (Math.min(a.longValue(), b.longValue())));
        } else if (a instanceof Double) {
            return (T) ((Double) (Math.min(a.doubleValue(), b.doubleValue())));
        } else if (a instanceof Float) {
            return (T) ((Float) (Math.min(a.floatValue(), b.floatValue())));
        }
        throw new IllegalStateException("Unknown Type!");
    }

    public T get() {
        return value;
    }
}
