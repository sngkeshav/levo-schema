package com.levo.schema.mapper;

import com.levo.schema.request.ApplicationCreateRequest;
import com.levo.schema.response.ApplicationResponse;
import com.levo.schema.entity.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ApplicationMapper {

    ApplicationMapper INSTANCE = Mappers.getMapper(ApplicationMapper.class);

    Application toEntity(ApplicationCreateRequest request);

    @Mapping(target = "schemaCount", expression = "java(getSchemaCount(application))")
    @Mapping(target = "serviceCount", expression = "java(getServiceCount(application))")
    ApplicationResponse toResponse(Application application);

    List<ApplicationResponse> toResponseList(List<Application> applications);

    default Integer getSchemaCount(Application application) {
        return application.getSchemas() != null ? application.getSchemas().size() : 0;
    }

    default Integer getServiceCount(Application application) {
        return application.getServices() != null ? application.getServices().size() : 0;
    }
}

