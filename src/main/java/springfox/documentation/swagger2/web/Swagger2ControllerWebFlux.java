/*
 *
 *  Copyright 2017-2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package springfox.documentation.swagger2.web;

import io.swagger.models.Swagger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.service.Documentation;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.OnReactiveWebApplication;
import springfox.documentation.spring.web.json.Json;
import springfox.documentation.spring.web.json.JsonSerializer;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;

import java.util.List;
import java.util.Optional;

import static org.springframework.util.MimeTypeUtils.*;
import static springfox.documentation.swagger2.web.Swagger2ControllerWebMvc.*;

@RestController
@ApiIgnore
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@RequestMapping(SWAGGER2_SPECIFICATION_PATH)
@Conditional({OnReactiveWebApplication.class})
public class Swagger2ControllerWebFlux {

  private static final Logger LOGGER = LoggerFactory.getLogger(Swagger2ControllerWebFlux.class);
  private static final String HAL_MEDIA_TYPE = "application/hal+json";

  private final DocumentationCache documentationCache;
  private final ServiceModelToSwagger2Mapper mapper;
  private final JsonSerializer jsonSerializer;
  private final PluginRegistry<WebFluxSwaggerTransformationFilter, DocumentationType> transformations;

  @Autowired
  public Swagger2ControllerWebFlux(
      DocumentationCache documentationCache,
      ServiceModelToSwagger2Mapper mapper,
      JsonSerializer jsonSerializer,
      @Qualifier("webFluxSwaggerTransformationFilterRegistry")
          PluginRegistry<WebFluxSwaggerTransformationFilter, DocumentationType> transformations) {
    this.documentationCache = documentationCache;
    this.mapper = mapper;
    this.jsonSerializer = jsonSerializer;
    this.transformations = transformations;
  }

  @RequestMapping(
      method = RequestMethod.GET,
      produces = {APPLICATION_JSON_VALUE, HAL_MEDIA_TYPE})
  @ResponseBody
  public ResponseEntity<Json> getDocumentation(
      @RequestParam(value = "group", required = false) String swaggerGroup,
      ServerHttpRequest request) {

    String groupName = Optional.ofNullable(swaggerGroup).orElse(Docket.DEFAULT_GROUP_NAME);
    Documentation documentation = documentationCache.documentationByGroup(groupName);
    if (documentation == null) {
      LOGGER.warn("Unable to find specification for group {}", HtmlUtils.htmlEscape(groupName));
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    Swagger swagger = mapper.mapDocumentation(documentation);
    SwaggerTransformationContext<ServerHttpRequest> context
        = new SwaggerTransformationContext<>(swagger, request);
    List<WebFluxSwaggerTransformationFilter> filters = transformations.getPluginsFor(DocumentationType.SWAGGER_2);
    for (WebFluxSwaggerTransformationFilter each : filters) {
      context = context.next(each.transform(context));
    }
    return new ResponseEntity<>(jsonSerializer.toJson(context.getSpecification()), HttpStatus.OK);
  }
}