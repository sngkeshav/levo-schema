package com.levo.schema.mapper;

import com.levo.schema.request.ServiceCreateRequest;
import com.levo.schema.response.ServiceResponse;
import com.levo.schema.entity.Service;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServiceMapper {

    ServiceMapper INSTANCE = Mappers.getMapper(ServiceMapper.class);

    Service toEntity(ServiceCreateRequest request);

    ServiceResponse toResponse(Service service);

    List<ServiceResponse> toResponseList(List<Service> services);
}