package com.ancientlightstudios.quarkus.java.openapi.server;

import com.ancientlightstudios.quarkus.java.test.model.BaseMovie;
import com.ancientlightstudios.quarkus.java.test.model.Movie;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.util.UUID;

@Path("/")
public class MovieServer {

    @Consumes("application/json")
    @Produces("application/json")
    @POST
    @Path("/movies")
    public Uni<Movie> addMovie(BaseMovie movie) {

        return Uni.createFrom().item(movie).map( it -> {
            var result = new Movie();

            // copy stuff
            result.setTitle(it.getTitle());
            result.setReleaseDate(it.getReleaseDate());
            result.setGenres(it.getGenres());
            result.setDuration(it.getDuration());
            result.setCast(it.getCast());
            result.setAdditionalInformation(it.getAdditionalInformation());
            result.setId(UUID.randomUUID());

            return result;
        });

       

    }
}
