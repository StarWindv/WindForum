package top.starwindv.forum.Utils;


import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;


@SuppressWarnings("unused")
public class Values implements Iterable<Object> {
    private final Object[] elements;
    private int hashCode;

    private Values(Object... elements) {
        this.elements = elements == null ? new Object[0] : Arrays.copyOf(elements, elements.length);
    }

    public static Values from(Object... elements) {
        return new Values(elements);
    }

    public final Object get(int index) {
        if (index < 0 || index >= elements.length) {
            throw new IndexOutOfBoundsException(
                String.format(
                    "Total Length: %d, Got Index: %d", 
                        elements.length, 
                        index
                )
            );
        }
        return elements[index];
    }

    public final int size() {
        return elements.length;
    }

    public <T> T get(int index, Class<T> clazz) {
        Object obj = get(index);
        return clazz.cast(obj);
    }

    public boolean isEmpty() {
        return elements.length == 0;
    }

    @NotNull
    @Override
    public Iterator<Object> iterator() {
        return Arrays.asList(elements).iterator();
    }

    @Override
    public String toString() {
        if (elements.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            sb.append(elements[i]);
            if (i != elements.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Values values = (Values) o;
        return Arrays.equals(elements, values.elements);
    }

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = Arrays.hashCode(elements);
            hashCode = result;
        }
        return result;
    }

    public String serialize() {
        return String.format(
            "{\"status\" : %s, \"message\": %s, \"values\": %s, \"inner_status\": %s}\n",
            this.getFirst(),
            this.getMessage(),
            this.getResult(),
            this.getInnerStatus()
        );
    }

    public final Object getFirst() {
        return this.get(0);
    }

    public final boolean getStatus() {
        boolean result = false;
        if (this.size() >= 1) {
            result = this.get(0, boolean.class);
        }
        return result;
    }

    public final String getMessage() {
        if (this.size()<2) { return "[WindForumValues] No Message"; }
        return this.get(1, String.class);
    }

    public final Object getResult() {
        if (this.size()<3) { return "[WindForumValues] No Result"; }
        return this.get(2);
    }

    public final Object getInnerStatus() {
        if (this.size()<4) { return "[WindForumValues] No Status"; }
        return this.get(3);
    }
}
