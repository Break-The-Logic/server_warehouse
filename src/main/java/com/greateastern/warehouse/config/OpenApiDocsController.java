package com.greateastern.warehouse.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@RestController
public class OpenApiDocsController {

  private static final String APPLICATION_JSON = "application/json";
  private static final String DEFAULT_RESPONSE_DESCRIPTION = "Success";

  private final OpenAPI baseOpenApi;
  private final RequestMappingHandlerMapping requestMappingHandlerMapping;
  private final ObjectMapper apiDocsObjectMapper;

  public OpenApiDocsController(
      OpenAPI baseOpenApi,
      RequestMappingHandlerMapping requestMappingHandlerMapping,
      ObjectMapper objectMapper
  ) {
    this.baseOpenApi = baseOpenApi;
    this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    this.apiDocsObjectMapper = objectMapper.copy().setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  @GetMapping(value = "/v3/api-docs", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> apiDocs() {
    OpenAPI openApi = cloneBaseOpenApi();
    openApi.setOpenapi("3.0.1");
    Paths paths = new Paths();

    requestMappingHandlerMapping.getHandlerMethods().forEach((requestMappingInfo, handlerMethod) ->
        registerOperations(paths, requestMappingInfo, handlerMethod));

    openApi.setPaths(paths);

    try {
      ObjectNode openApiTree = apiDocsObjectMapper.valueToTree(openApi);
      removeNullNodes(openApiTree);
      return ResponseEntity.ok(apiDocsObjectMapper.writeValueAsString(openApiTree));
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Failed to serialize OpenAPI document", ex);
    }
  }

  private OpenAPI cloneBaseOpenApi() {
    OpenAPI openApi = new OpenAPI();
    openApi.setInfo(baseOpenApi.getInfo());
    openApi.setServers(baseOpenApi.getServers());
    openApi.setExternalDocs(baseOpenApi.getExternalDocs());
    openApi.setTags(baseOpenApi.getTags());
    openApi.setSecurity(baseOpenApi.getSecurity());
    openApi.setComponents(baseOpenApi.getComponents());
    openApi.setExtensions(baseOpenApi.getExtensions());
    return openApi;
  }

  private void registerOperations(Paths paths, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {
    Set<String> uriPatterns = resolveUriPatterns(requestMappingInfo);
    if (uriPatterns.isEmpty()) {
      return;
    }

    Set<RequestMethod> methods = new LinkedHashSet<>(requestMappingInfo.getMethodsCondition().getMethods());
    if (methods.isEmpty()) {
      methods = EnumSet.of(RequestMethod.GET);
    }

    for (String uriPattern : uriPatterns) {
      if (isInternalPath(uriPattern)) {
        continue;
      }

      PathItem pathItem = paths.computeIfAbsent(uriPattern, key -> new PathItem());
      for (RequestMethod method : methods) {
        bindOperation(pathItem, method, buildOperation(handlerMethod, method, uriPattern));
      }
    }
  }

  private Operation buildOperation(HandlerMethod handlerMethod, RequestMethod method, String uriPattern) {
    String beanName = handlerMethod.getBeanType().getSimpleName();
    String methodName = handlerMethod.getMethod().getName();
    io.swagger.v3.oas.annotations.Operation operationAnnotation =
        handlerMethod.getMethodAnnotation(io.swagger.v3.oas.annotations.Operation.class);

    Operation operation = new Operation();
    operation.setOperationId(beanName + "_" + methodName + "_" + method.name().toLowerCase(Locale.ROOT));
    operation.setSummary(resolveSummary(operationAnnotation, methodName));
    operation.addTagsItem(beanName.replace("Controller", ""));

    List<Parameter> parameters = buildParameters(handlerMethod, uriPattern);
    if (!parameters.isEmpty()) {
      operation.setParameters(parameters);
    }

    RequestBody requestBody = buildRequestBody(handlerMethod, operationAnnotation);
    if (requestBody != null) {
      operation.setRequestBody(requestBody);
    }

    operation.setResponses(buildResponses(method, operationAnnotation));
    return operation;
  }

  private String resolveSummary(io.swagger.v3.oas.annotations.Operation operationAnnotation, String fallback) {
    if (operationAnnotation == null || operationAnnotation.summary().isBlank()) {
      return fallback;
    }
    return operationAnnotation.summary();
  }

  private ApiResponses buildResponses(
      RequestMethod method,
      io.swagger.v3.oas.annotations.Operation operationAnnotation
  ) {
    ApiResponses responses = fromOperationResponses(operationAnnotation);
    if (responses != null) {
      return responses;
    }

    String responseCode = method == RequestMethod.POST ? "201" : "200";
    ApiResponse successResponse = new ApiResponse()
        .description(DEFAULT_RESPONSE_DESCRIPTION)
        .content(new Content().addMediaType(APPLICATION_JSON, new MediaType().schema(defaultSuccessResponseSchema())));
    return new ApiResponses().addApiResponse(responseCode, successResponse);
  }

  private ApiResponses fromOperationResponses(io.swagger.v3.oas.annotations.Operation operationAnnotation) {
    if (operationAnnotation == null || operationAnnotation.responses().length == 0) {
      return null;
    }

    ApiResponses responses = new ApiResponses();
    for (io.swagger.v3.oas.annotations.responses.ApiResponse responseAnnotation : operationAnnotation.responses()) {
      String responseCode = responseAnnotation.responseCode();
      if (responseCode == null || responseCode.isBlank()) {
        continue;
      }

      ApiResponse response = new ApiResponse()
          .description(resolveDescription(responseAnnotation.description()));

      MediaType mediaType = new MediaType().schema(defaultSuccessResponseSchema());
      Object responseExample = resolveResponseExample(responseAnnotation);
      if (responseExample != null) {
        mediaType.setExample(responseExample);
      }
      response.setContent(new Content().addMediaType(APPLICATION_JSON, mediaType));

      responses.addApiResponse(responseCode, response);
    }

    return responses.isEmpty() ? null : responses;
  }

  private String resolveDescription(String description) {
    if (description == null || description.isBlank()) {
      return DEFAULT_RESPONSE_DESCRIPTION;
    }
    return description;
  }

  private Object resolveResponseExample(io.swagger.v3.oas.annotations.responses.ApiResponse responseAnnotation) {
    if (responseAnnotation == null) {
      return null;
    }

    for (io.swagger.v3.oas.annotations.media.Content content : responseAnnotation.content()) {
      if (!matchesJsonMediaType(content.mediaType())) {
        continue;
      }
      Object example = resolveContentExample(content, Object.class);
      if (example != null) {
        return example;
      }
    }
    return null;
  }

  private ObjectSchema defaultSuccessResponseSchema() {
    ObjectSchema schema = new ObjectSchema();
    schema.addProperty("code", new StringSchema().example("00"));
    schema.addProperty("message", new ObjectSchema());
    schema.addProperty("data", new ObjectSchema().nullable(true));
    return schema;
  }

  private List<Parameter> buildParameters(HandlerMethod handlerMethod, String uriPattern) {
    List<Parameter> parameters = new ArrayList<>();
    for (java.lang.reflect.Parameter methodParameter : handlerMethod.getMethod().getParameters()) {
      PathVariable pathVariable = methodParameter.getAnnotation(PathVariable.class);
      if (pathVariable != null) {
        parameters.add(buildPathParameter(methodParameter, pathVariable, uriPattern));
        continue;
      }

      RequestParam requestParam = methodParameter.getAnnotation(RequestParam.class);
      if (requestParam != null) {
        parameters.add(buildQueryParameter(methodParameter, requestParam));
      }
    }
    return parameters;
  }

  private Parameter buildPathParameter(
      java.lang.reflect.Parameter methodParameter,
      PathVariable pathVariable,
      String uriPattern
  ) {
    String name = resolvePathVariableName(methodParameter, pathVariable, uriPattern);
    Parameter parameter = new Parameter()
        .name(name)
        .in("path")
        .required(true)
        .schema(toPrimitiveSchema(methodParameter.getType()));

    applySwaggerParameterAnnotation(methodParameter, parameter);
    return parameter;
  }

  private Parameter buildQueryParameter(java.lang.reflect.Parameter methodParameter, RequestParam requestParam) {
    String name = resolveRequestParamName(methodParameter, requestParam);
    boolean hasDefault = !ValueConstants.DEFAULT_NONE.equals(requestParam.defaultValue());
    Parameter parameter = new Parameter()
        .name(name)
        .in("query")
        .required(requestParam.required() && !hasDefault)
        .schema(toPrimitiveSchema(methodParameter.getType()));

    if (hasDefault) {
      parameter.getSchema().setDefault(parseTextValue(requestParam.defaultValue(), methodParameter.getType()));
    }

    applySwaggerParameterAnnotation(methodParameter, parameter);
    return parameter;
  }

  private void applySwaggerParameterAnnotation(java.lang.reflect.Parameter methodParameter, Parameter parameter) {
    io.swagger.v3.oas.annotations.Parameter swaggerParameter =
        methodParameter.getAnnotation(io.swagger.v3.oas.annotations.Parameter.class);
    if (swaggerParameter == null) {
      return;
    }

    if (!swaggerParameter.description().isBlank()) {
      parameter.setDescription(swaggerParameter.description());
    }
    if (!swaggerParameter.example().isBlank()) {
      parameter.setExample(parseTextValue(swaggerParameter.example(), methodParameter.getType()));
    }

    io.swagger.v3.oas.annotations.media.Schema parameterSchema = swaggerParameter.schema();
    if (parameterSchema != null) {
      if (!parameterSchema.defaultValue().isBlank()) {
        parameter.getSchema().setDefault(parseTextValue(parameterSchema.defaultValue(), methodParameter.getType()));
      }
      if (!parameterSchema.example().isBlank()) {
        parameter.setExample(parseTextValue(parameterSchema.example(), methodParameter.getType()));
      }
    }

    if (swaggerParameter.required()) {
      parameter.setRequired(true);
    }
  }

  private RequestBody buildRequestBody(
      HandlerMethod handlerMethod,
      io.swagger.v3.oas.annotations.Operation operationAnnotation
  ) {
    for (java.lang.reflect.Parameter methodParameter : handlerMethod.getMethod().getParameters()) {
      org.springframework.web.bind.annotation.RequestBody requestBodyAnnotation =
          methodParameter.getAnnotation(org.springframework.web.bind.annotation.RequestBody.class);
      if (requestBodyAnnotation == null) {
        continue;
      }

      Schema<?> schema = schemaFromType(methodParameter.getParameterizedType(), methodParameter.getType());
      Object example = resolveRequestBodyExample(operationAnnotation, methodParameter);
      if (example == null) {
        example = exampleFromType(methodParameter.getParameterizedType(), methodParameter.getType());
      }

      MediaType mediaType = new MediaType().schema(schema);
      if (example != null) {
        mediaType.setExample(example);
      }

      return new RequestBody()
          .required(requestBodyAnnotation.required())
          .content(new Content().addMediaType(APPLICATION_JSON, mediaType));
    }
    return null;
  }

  private Object resolveRequestBodyExample(
      io.swagger.v3.oas.annotations.Operation operationAnnotation,
      java.lang.reflect.Parameter methodParameter
  ) {
    if (operationAnnotation == null) {
      return null;
    }

    io.swagger.v3.oas.annotations.parameters.RequestBody requestBody = operationAnnotation.requestBody();
    if (requestBody == null) {
      return null;
    }

    for (io.swagger.v3.oas.annotations.media.Content content : requestBody.content()) {
      if (!matchesJsonMediaType(content.mediaType())) {
        continue;
      }
      Object example = resolveContentExample(content, methodParameter.getType());
      if (example != null) {
        return example;
      }
    }
    return null;
  }

  private boolean matchesJsonMediaType(String mediaType) {
    return mediaType == null || mediaType.isBlank() || APPLICATION_JSON.equalsIgnoreCase(mediaType);
  }

  private Object resolveContentExample(
      io.swagger.v3.oas.annotations.media.Content content,
      Class<?> targetType
  ) {
    if (content == null) {
      return null;
    }

    for (io.swagger.v3.oas.annotations.media.ExampleObject exampleObject : content.examples()) {
      if (exampleObject == null || exampleObject.value().isBlank()) {
        continue;
      }
      return parseTextValue(exampleObject.value(), targetType);
    }

    io.swagger.v3.oas.annotations.media.Schema schemaAnnotation = content.schema();
    if (schemaAnnotation != null) {
      if (!schemaAnnotation.example().isBlank()) {
        return parseTextValue(schemaAnnotation.example(), targetType);
      }
      if (!schemaAnnotation.defaultValue().isBlank()) {
        return parseTextValue(schemaAnnotation.defaultValue(), targetType);
      }
    }

    return null;
  }

  private Schema<?> schemaFromType(Type genericType, Class<?> rawType) {
    if (String.class.equals(rawType) || rawType.isEnum()) {
      StringSchema schema = new StringSchema();
      if (rawType.isEnum()) {
        List<String> enumValues = new ArrayList<>();
        for (Object constant : rawType.getEnumConstants()) {
          enumValues.add(constant.toString());
        }
        schema.setEnum(enumValues);
      }
      return schema;
    }
    if (Boolean.class.equals(rawType) || boolean.class.equals(rawType)) {
      return new BooleanSchema();
    }
    if (Integer.class.equals(rawType) || int.class.equals(rawType)
        || Long.class.equals(rawType) || long.class.equals(rawType)
        || Short.class.equals(rawType) || short.class.equals(rawType)) {
      return new IntegerSchema();
    }
    if (BigDecimal.class.equals(rawType)
        || Double.class.equals(rawType) || double.class.equals(rawType)
        || Float.class.equals(rawType) || float.class.equals(rawType)) {
      return new NumberSchema();
    }
    if (rawType.isArray()) {
      ArraySchema arraySchema = new ArraySchema();
      arraySchema.setItems(schemaFromType(rawType.getComponentType(), rawType.getComponentType()));
      return arraySchema;
    }
    if (List.class.isAssignableFrom(rawType)) {
      ArraySchema arraySchema = new ArraySchema();
      arraySchema.setItems(resolveArrayItemSchema(genericType));
      return arraySchema;
    }
    return buildObjectSchema(rawType);
  }

  private Schema<?> resolveArrayItemSchema(Type genericType) {
    if (genericType instanceof ParameterizedType parameterizedType) {
      Type[] typeArguments = parameterizedType.getActualTypeArguments();
      if (typeArguments.length == 1 && typeArguments[0] instanceof Class<?> itemClass) {
        return schemaFromType(typeArguments[0], itemClass);
      }
    }
    return new ObjectSchema();
  }

  private ObjectSchema buildObjectSchema(Class<?> rawType) {
    ObjectSchema objectSchema = new ObjectSchema();
    if (!rawType.isRecord()) {
      objectSchema.setAdditionalProperties(Boolean.TRUE);
      return objectSchema;
    }

    Map<String, Schema> properties = new LinkedHashMap<>();
    List<String> requiredFields = new ArrayList<>();

    for (RecordComponent recordComponent : rawType.getRecordComponents()) {
      Schema<?> propertySchema = schemaFromType(recordComponent.getGenericType(), recordComponent.getType());
      applySchemaAnnotation(recordComponent.getAnnotation(io.swagger.v3.oas.annotations.media.Schema.class),
          propertySchema, recordComponent.getType());
      properties.put(recordComponent.getName(), propertySchema);

      if (isRequiredRecordField(recordComponent)) {
        requiredFields.add(recordComponent.getName());
      }
    }

    objectSchema.setProperties(properties);
    if (!requiredFields.isEmpty()) {
      objectSchema.setRequired(requiredFields);
    }
    return objectSchema;
  }

  private boolean isRequiredRecordField(RecordComponent recordComponent) {
    if (recordComponent.getType().isPrimitive()) {
      return true;
    }

    for (Annotation annotation : recordComponent.getAnnotations()) {
      String name = annotation.annotationType().getName();
      if ("jakarta.validation.constraints.NotNull".equals(name)
          || "jakarta.validation.constraints.NotBlank".equals(name)
          || "jakarta.validation.constraints.NotEmpty".equals(name)) {
        return true;
      }
    }
    return false;
  }

  private void applySchemaAnnotation(
      io.swagger.v3.oas.annotations.media.Schema schemaAnnotation,
      Schema<?> targetSchema,
      Class<?> targetType
  ) {
    if (schemaAnnotation == null) {
      return;
    }

    if (!schemaAnnotation.description().isBlank()) {
      targetSchema.setDescription(schemaAnnotation.description());
    }
    if (!schemaAnnotation.defaultValue().isBlank()) {
      targetSchema.setDefault(parseTextValue(schemaAnnotation.defaultValue(), targetType));
    }
    if (!schemaAnnotation.example().isBlank()) {
      targetSchema.setExample(parseTextValue(schemaAnnotation.example(), targetType));
    }
  }

  private Object exampleFromType(Type genericType, Class<?> rawType) {
    if (String.class.equals(rawType)) {
      return "string";
    }
    if (Boolean.class.equals(rawType) || boolean.class.equals(rawType)) {
      return true;
    }
    if (Integer.class.equals(rawType) || int.class.equals(rawType)
        || Long.class.equals(rawType) || long.class.equals(rawType)
        || Short.class.equals(rawType) || short.class.equals(rawType)) {
      return 1;
    }
    if (BigDecimal.class.equals(rawType)
        || Double.class.equals(rawType) || double.class.equals(rawType)
        || Float.class.equals(rawType) || float.class.equals(rawType)) {
      return new BigDecimal("1.00");
    }
    if (rawType.isEnum()) {
      Object[] enumValues = rawType.getEnumConstants();
      return enumValues.length == 0 ? null : enumValues[0].toString();
    }
    if (rawType.isArray()) {
      return List.of(exampleFromType(rawType.getComponentType(), rawType.getComponentType()));
    }
    if (List.class.isAssignableFrom(rawType)) {
      if (genericType instanceof ParameterizedType parameterizedType) {
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        if (typeArguments.length == 1 && typeArguments[0] instanceof Class<?> itemClass) {
          return List.of(exampleFromType(typeArguments[0], itemClass));
        }
      }
      return List.of(new LinkedHashMap<>());
    }
    if (!rawType.isRecord()) {
      return new LinkedHashMap<>();
    }

    Map<String, Object> example = new LinkedHashMap<>();
    for (RecordComponent recordComponent : rawType.getRecordComponents()) {
      io.swagger.v3.oas.annotations.media.Schema schemaAnnotation =
          recordComponent.getAnnotation(io.swagger.v3.oas.annotations.media.Schema.class);
      Object fieldExample = resolveFieldExample(recordComponent, schemaAnnotation);
      example.put(recordComponent.getName(), fieldExample);
    }
    return example;
  }

  private Object resolveFieldExample(
      RecordComponent recordComponent,
      io.swagger.v3.oas.annotations.media.Schema schemaAnnotation
  ) {
    if (schemaAnnotation != null) {
      if (!schemaAnnotation.example().isBlank()) {
        return parseTextValue(schemaAnnotation.example(), recordComponent.getType());
      }
      if (!schemaAnnotation.defaultValue().isBlank()) {
        return parseTextValue(schemaAnnotation.defaultValue(), recordComponent.getType());
      }
    }

    return exampleFromType(recordComponent.getGenericType(), recordComponent.getType());
  }

  private Schema<?> toPrimitiveSchema(Class<?> type) {
    if (Boolean.class.equals(type) || boolean.class.equals(type)) {
      return new BooleanSchema();
    }
    if (Integer.class.equals(type) || int.class.equals(type)
        || Long.class.equals(type) || long.class.equals(type)
        || Short.class.equals(type) || short.class.equals(type)) {
      return new IntegerSchema();
    }
    if (BigDecimal.class.equals(type)
        || Double.class.equals(type) || double.class.equals(type)
        || Float.class.equals(type) || float.class.equals(type)) {
      return new NumberSchema();
    }
    return new StringSchema();
  }

  private Object parseTextValue(String value, Class<?> targetType) {
    if (value == null) {
      return null;
    }

    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      return "";
    }

    if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
      try {
        return apiDocsObjectMapper.readValue(trimmed, Object.class);
      } catch (JsonProcessingException ignored) {
        // Fall through to plain string handling.
      }
    }

    try {
      if (Boolean.class.equals(targetType) || boolean.class.equals(targetType)) {
        return Boolean.parseBoolean(trimmed);
      }
      if (Integer.class.equals(targetType) || int.class.equals(targetType)) {
        return Integer.parseInt(trimmed);
      }
      if (Long.class.equals(targetType) || long.class.equals(targetType)) {
        return Long.parseLong(trimmed);
      }
      if (Short.class.equals(targetType) || short.class.equals(targetType)) {
        return Short.parseShort(trimmed);
      }
      if (BigDecimal.class.equals(targetType)) {
        return new BigDecimal(trimmed);
      }
      if (Double.class.equals(targetType) || double.class.equals(targetType)) {
        return Double.parseDouble(trimmed);
      }
      if (Float.class.equals(targetType) || float.class.equals(targetType)) {
        return Float.parseFloat(trimmed);
      }
    } catch (NumberFormatException ignored) {
      // Fall back to string.
    }

    return trimmed;
  }

  private Set<String> resolveUriPatterns(RequestMappingInfo requestMappingInfo) {
    Set<String> patterns = new LinkedHashSet<>();
    PatternsRequestCondition patternsCondition = requestMappingInfo.getPatternsCondition();
    if (patternsCondition != null) {
      patterns.addAll(patternsCondition.getPatterns());
    }
    PathPatternsRequestCondition pathPatternsCondition = requestMappingInfo.getPathPatternsCondition();
    if (pathPatternsCondition != null) {
      patterns.addAll(pathPatternsCondition.getPatternValues());
    }
    return patterns;
  }

  private boolean isInternalPath(String path) {
    return path.startsWith("/v3/api-docs")
        || path.startsWith("/swagger-ui")
        || "/swagger-ui.html".equals(path)
        || "/error".equals(path);
  }

  private String resolvePathVariableName(
      java.lang.reflect.Parameter methodParameter,
      PathVariable pathVariable,
      String uriPattern
  ) {
    if (!pathVariable.name().isBlank()) {
      return pathVariable.name();
    }
    if (!pathVariable.value().isBlank()) {
      return pathVariable.value();
    }
    for (String token : uriPattern.split("/")) {
      if (token.startsWith("{") && token.endsWith("}")) {
        return token.substring(1, token.length() - 1);
      }
    }
    return methodParameter.getName();
  }

  private String resolveRequestParamName(
      java.lang.reflect.Parameter methodParameter,
      RequestParam requestParam
  ) {
    if (!requestParam.name().isBlank()) {
      return requestParam.name();
    }
    if (!requestParam.value().isBlank()) {
      return requestParam.value();
    }
    return methodParameter.getName();
  }

  private void bindOperation(PathItem pathItem, RequestMethod method, Operation operation) {
    switch (method) {
      case GET -> pathItem.setGet(operation);
      case POST -> pathItem.setPost(operation);
      case PUT -> pathItem.setPut(operation);
      case PATCH -> pathItem.setPatch(operation);
      case DELETE -> pathItem.setDelete(operation);
      case HEAD -> pathItem.setHead(operation);
      case OPTIONS -> pathItem.setOptions(operation);
      case TRACE -> pathItem.setTrace(operation);
    }
  }

  private void removeNullNodes(JsonNode node) {
    if (node == null) {
      return;
    }

    if (node.isObject()) {
      ObjectNode objectNode = (ObjectNode) node;
      List<String> nullFields = new ArrayList<>();
      Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> entry = fields.next();
        JsonNode child = entry.getValue();
        if (child == null || child.isNull()) {
          nullFields.add(entry.getKey());
        } else {
          removeNullNodes(child);
        }
      }
      for (String field : nullFields) {
        objectNode.remove(field);
      }
      return;
    }

    if (node.isArray()) {
      ArrayNode arrayNode = (ArrayNode) node;
      for (int i = arrayNode.size() - 1; i >= 0; i--) {
        JsonNode child = arrayNode.get(i);
        if (child == null || child.isNull()) {
          arrayNode.remove(i);
        } else {
          removeNullNodes(child);
        }
      }
    }
  }
}
