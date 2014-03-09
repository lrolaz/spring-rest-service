package scratch.spring.webapp.steps;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.copyOfRange;
import static java.util.Map.Entry;

/**
 * An object that holds dot notation properties e.g. one.two.three="third layer value"
 */
public class PropertyObject {

    private final Map<String, Object> map;

    public PropertyObject() {
        this(new HashMap<String, Object>());
    }

    public PropertyObject(PropertyObject propertyObject) {
        this(propertyObject.toMap());
    }

    public PropertyObject(Map<String, Object> map) {

        this.map = deepCopyMap(map);
    }

    private static Map<String, Object> deepCopyMap(Map<String, Object> origin) {

        return deepCopyMap(origin, new HashMap<String, Object>());
    }

    private static Map<String, Object> deepCopyMap(Map<String, Object> origin, Map<String, Object> destination) {

        final Set<Entry<String, Object>> entries = origin.entrySet();

        return deepCopyMap(entries.iterator(), destination);
    }

    private static Map<String, Object> deepCopyMap(Iterator<Entry<String, Object>> origin, Map<String, Object> destination) {

        if (!origin.hasNext()) {
            return destination;
        }

        final Entry<String, Object> entry = origin.next();

        if (entry.getValue() instanceof Map) {

            @SuppressWarnings("unchecked")
            final Set<Entry<String, Object>> entries = ((Map<String, Object>) entry.getValue()).entrySet();

            destination.put(entry.getKey(), deepCopyMap(entries.iterator(), new HashMap<String, Object>()));

        } else {
            destination.put(entry.getKey(), entry.getValue());
        }

        return deepCopyMap(origin, destination);
    }

    /**
     * Get the property at the supplied property path.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String propertyPath) {

        final T value = traverse(map, propertyPath.split("\\."), null, new GetLeaf<T>(), new CheckBranch());

        if (value instanceof Map) {
            return (T) deepCopyMap((Map<String, Object>) value);
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    private static <T> T traverse(Map<String, Object> map, String[] propertyPath, Object value, Leaf<T> leaf,
                                  Branch branch) {

        final String head = head(propertyPath);
        final String[] tail = tail(propertyPath);

        if (isEmpty(tail)) {
            return leaf.run(map, head, value);
        }

        branch.run(map, head);

        return traverse((Map<String, Object>) map.get(head), tail, value, leaf, branch);
    }

    private static String head(String[] array) {
        return array[0];
    }

    private static String[] tail(String[] array) {

        if (2 > array.length) {
            return new String[0];
        }

        return copyOfRange(array, 1, array.length);
    }

    private static boolean isEmpty(String[] array) {
        return null == array || 0 == array.length;
    }

    /**
     * Set the property at the supplied property path.
     */
    public void set(String propertyPath, Object value) {

        traverse(map, propertyPath.split("\\."), value, new SetLeaf(), new CreateBranch());
    }

    @SuppressWarnings("unchecked")
    public <T> T remove(String propertyPath) {

        return (T) traverse(map, propertyPath.split("\\."), null, new RemoveLeaf(), new CheckBranch());
    }

    public void clear() {
        map.clear();
    }

    /**
     * @return a map representation of this {@code PropertyObject}.
     */
    public Map<String, Object> toMap() {
        return deepCopyMap(map);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PropertyObject that = (PropertyObject) o;

        return map.equals(that.map);

    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    private static interface Leaf<T> {
        public T run(Map<String, Object> map, String key, Object value);
    }

    private static interface Branch {
        public void run(Map<String, Object> map, String key);
    }

    private static class GetLeaf<T> implements Leaf<T> {

        @SuppressWarnings("unchecked")
        @Override
        public T run(Map<String, Object> map, String key, Object value) {

            checkKey(map, key);

            return (T) map.get(key);
        }

        public static void checkKey(Map<String, Object> map, String key) {

            if (!map.containsKey(key)) {
                throw new IllegalArgumentException("no value exists for property (" + key + ")");
            }
        }
    }

    private static class SetLeaf implements Leaf<Void> {

        @Override
        public Void run(Map<String, Object> map, String key, Object value) {

            map.put(key, value);

            return null;
        }
    }

    private static class RemoveLeaf<T> implements Leaf<T> {

        @SuppressWarnings("unchecked")
        @Override
        public T run(Map<String, Object> map, String key, Object value) {

            GetLeaf.checkKey(map, key);

            return (T) map.remove(key);
        }
    }

    private static class CheckBranch implements Branch {

        @Override
        public void run(Map<String, Object> map, String key) {

            GetLeaf.checkKey(map, key);
        }
    }

    private static class CreateBranch implements Branch {

        @Override
        public void run(Map<String, Object> map, String key) {

            map.put(key, new HashMap<String, Object>());
        }
    }
}
