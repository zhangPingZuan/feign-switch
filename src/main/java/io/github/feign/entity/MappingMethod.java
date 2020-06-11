package io.github.feign.entity;

public interface MappingMethod {
    String getMappingPath();
    String getFullMappingPath();
    String getQualifiedAnnotationName();
    String getSuperMappingPath();
}
