package deltaiot.console.json;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class StrictFieldsTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        // Delegate for non‑POJO types
        TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
        if (!(delegate instanceof ReflectiveTypeAdapterFactory.Adapter)) {
            return delegate;
        }

        // Collect expected JSON names from the class’s fields (including @SerializedName)
        Class<? super T> rawType = type.getRawType();
        Set<String> expectedNames = new HashSet<>();

        for (Class<?> c = rawType; c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (!f.isSynthetic()) {
                    SerializedName sn = f.getAnnotation(SerializedName.class);
                    expectedNames.add(sn != null ? sn.value() : f.getName());
                }
            }
        }

        return new TypeAdapter<>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                delegate.write(out, value);
            }

            @Override
            public T read(JsonReader in) throws IOException {
                final String typeName = rawType.getSimpleName();
                JsonElement tree = JsonParser.parseReader(in);
                if (!tree.isJsonObject()) {
                    throw new JsonParseException(String.format("Expected %s as JSON object", typeName));
                }
                JsonObject obj = tree.getAsJsonObject();
                Set<String> present = obj.keySet();

                // Detect missing fields
                List<String> missing = new ArrayList<>();
                for (String name : expectedNames) {
                    if (!present.contains(name)) {
                        missing.add(name);
                    }
                }
                if (!missing.isEmpty()) {
                    throw new JsonParseException(
                            String.format("Missing required field(s) for %s: %s", typeName, missing));
                }

                // Detect unknown (extra) fields
                List<String> extra = new ArrayList<>();
                for (String name : present) {
                    if (!expectedNames.contains(name)) {
                        extra.add(name);
                    }
                }
                if (!extra.isEmpty()) {
                    throw new JsonParseException(String.format("Unknown field(s) for %s: %s", typeName, extra));
                }

                return delegate.fromJsonTree(tree);
            }
        };
    }
}
