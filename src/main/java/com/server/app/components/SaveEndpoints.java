package com.server.app.components;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.server.app.config.SecurityRules;
import com.server.app.services.PermissionService;

@Component
public class SaveEndpoints
        implements ApplicationListener<ApplicationReadyEvent> {

    private final RequestMappingHandlerMapping handlerMapping;
    private final PermissionService permissionService;

    public SaveEndpoints(
            RequestMappingHandlerMapping handlerMapping,
            PermissionService permissionService
    ) {
        this.handlerMapping = handlerMapping;
        this.permissionService = permissionService;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        handlerMapping.getHandlerMethods()
                .forEach((mappingInfo, handlerMethod) -> {

                    Set<String> paths = getPaths(mappingInfo);

                    Set<RequestMethod> methods =
                            mappingInfo
                                    .getMethodsCondition()
                                    .getMethods();

                    /*
                     * Si un endpoint no declara explícitamente
                     * el método, se toma GET como valor por defecto.
                     */
                    if (methods.isEmpty()) {
                        methods = Set.of(RequestMethod.GET);
                    }

                    for (String path : paths) {
                        for (RequestMethod method : methods) {
                            processEndpoint(
                                    path,
                                    method.name()
                            );
                        }
                    }
                });
    }

    private Set<String> getPaths(RequestMappingInfo info) {

        if (info.getPathPatternsCondition() != null) {
            return info.getPathPatternsCondition()
                    .getPatterns()
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());
        }

        if (info.getPatternsCondition() != null) {
            return info.getPatternsCondition()
                    .getPatterns();
        }

        return Set.of();
    }

    private void processEndpoint(
            String path,
            String method
    ) {

        /*
         * No se guardan como permisos las rutas públicas,
         * auth-only ni las ignoradas.
         */
        if (SecurityRules.isIgnored(path)) {
            return;
        }

        if (SecurityRules.isPublic(method, path)) {
            return;
        }

        if (SecurityRules.isAuthOnly(method, path)) {
            return;
        }

        /*
         * Evita guardar endpoints internos de Spring.
         */
        if (path.startsWith("/error")) {
            return;
        }

        permissionService.createIfNotExists(path, method);
    }
}