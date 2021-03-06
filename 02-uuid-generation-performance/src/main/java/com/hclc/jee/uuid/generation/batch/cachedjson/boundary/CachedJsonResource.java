package com.hclc.jee.uuid.generation.batch.cachedjson.boundary;

import com.hclc.jee.uuid.generation.batch.cachedjson.control.CachedJsonGenerator;
import com.hclc.jee.uuid.generation.metrics.control.GenerationMetricInterceptor;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Stateless
@Path(value = "cachedJson")
public class CachedJsonResource {

    @Inject
    CachedJsonGenerator cachedJsonGenerator;

    @GET
    @Produces(APPLICATION_JSON)
    @Interceptors(GenerationMetricInterceptor.class)
    public Response cachedJson() {
        return Response.ok(cachedJsonGenerator.getNextBatch()).build();
    }

    @GET
    @Produces(APPLICATION_JSON)
    @Path("status")
    public Response status() {
        return Response.ok(cachedJsonGenerator.status()).build();
    }
}
