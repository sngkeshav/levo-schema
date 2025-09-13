package com.levo.schema.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levo.schema.request.SchemaWithContentWrapper;
import com.levo.schema.response.SchemaResponse;
import com.levo.schema.entity.Schema;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SchemaMapper {

    SchemaMapper INSTANCE = Mappers.getMapper(SchemaMapper.class);

    @Mapping(target = "applicationName", source = "application.name")
    @Mapping(target = "serviceName", source = "service.name")
    @Mapping(target = "scopeIdentifier", expression = "java(schema.getScopeIdentifier())")
    SchemaResponse toSchemaResponse(Schema schema);

    @Mapping(target = "id", source = "wrapper.schema.id")
    @Mapping(target = "applicationId", source = "wrapper.schema.applicationId")
    @Mapping(target = "serviceId", source = "wrapper.schema.serviceId")
    @Mapping(target = "version", source = "wrapper.schema.version")
    @Mapping(target = "filePath", source = "wrapper.schema.filePath")
    @Mapping(target = "fileFormat", source = "wrapper.schema.fileFormat")
    @Mapping(target = "isLatest", source = "wrapper.schema.isLatest")
    @Mapping(target = "content", source = "wrapper.schema.content")
    @Mapping(target = "createdAt", source = "wrapper.schema.createdAt")
    @Mapping(target = "scopeIdentifier", expression = "java(wrapper.getSchema().getScopeIdentifier())")
    @Mapping(target = "applicationName", source = "wrapper.schema.application.name")
    @Mapping(target = "serviceName", source = "wrapper.schema.service.name")
    SchemaResponse toSchemaWithContentResponse(SchemaWithContentWrapper wrapper);

    List<SchemaResponse> toResponseList(List<Schema> schemas);
}
