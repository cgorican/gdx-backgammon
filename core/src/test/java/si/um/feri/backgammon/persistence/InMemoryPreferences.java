package si.um.feri.backgammon.persistence;

import com.badlogic.gdx.Preferences;

import java.util.HashMap;
import java.util.Map;

/** Minimal in-memory {@link Preferences} for tests. */
class InMemoryPreferences implements Preferences {
    final Map<String, Object> values = new HashMap<>();
    int flushes;

    @Override public Preferences putBoolean(String key, boolean val) { values.put(key, val); return this; }
    @Override public Preferences putInteger(String key, int val) { values.put(key, val); return this; }
    @Override public Preferences putLong(String key, long val) { values.put(key, val); return this; }
    @Override public Preferences putFloat(String key, float val) { values.put(key, val); return this; }
    @Override public Preferences putString(String key, String val) { values.put(key, val); return this; }
    @Override public Preferences put(Map<String, ?> vals) { values.putAll(vals); return this; }
    @Override public boolean getBoolean(String key) { return getBoolean(key, false); }
    @Override public int getInteger(String key) { return getInteger(key, 0); }
    @Override public long getLong(String key) { return getLong(key, 0); }
    @Override public float getFloat(String key) { return getFloat(key, 0); }
    @Override public String getString(String key) { return getString(key, ""); }
    @Override public boolean getBoolean(String key, boolean defValue) { return (Boolean) values.getOrDefault(key, defValue); }
    @Override public int getInteger(String key, int defValue) { return (Integer) values.getOrDefault(key, defValue); }
    @Override public long getLong(String key, long defValue) { return (Long) values.getOrDefault(key, defValue); }
    @Override public float getFloat(String key, float defValue) { return (Float) values.getOrDefault(key, defValue); }
    @Override public String getString(String key, String defValue) { return (String) values.getOrDefault(key, defValue); }
    @Override public Map<String, ?> get() { return values; }
    @Override public boolean contains(String key) { return values.containsKey(key); }
    @Override public void clear() { values.clear(); }
    @Override public void remove(String key) { values.remove(key); }
    @Override public void flush() { flushes++; }
}
