package org.squbich.calltree.serialize;

import java.util.List;

import org.squbich.calltree.model.calls.CallHierarchy;
import org.squbich.calltree.model.calls.ClassCaller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HierarchySerializer {
    private ObjectMapper objectMapper;

    public static final HierarchySerializer of(final List<String> visibleElements) {
        return new HierarchySerializer(createObjectMapper(visibleElements));
    }

    public String serialize(CallHierarchy hierarchy) {
        try {
            return objectMapper.writeValueAsString(hierarchy);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static ObjectMapper createObjectMapper(List<String> elements) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.setFilterProvider(JsonFilterProviderFactory.of().create(elements));
        return objectMapper;
    }
}