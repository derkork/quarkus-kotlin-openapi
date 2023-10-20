package com.ancientlightstudios.example.rating

import com.ancientlightstudios.example.rating.model.InvalidInputError
import com.ancientlightstudios.example.rating.model.MovieRating
import com.ancientlightstudios.example.rating.server.*
import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import com.ancientlightstudios.quarkus.kotlin.openapi.ValidationError
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped
import org.slf4j.LoggerFactory

@ApplicationScoped
class RatingMockServer : RatingServiceDelegate {

    private val log = LoggerFactory.getLogger(RatingMockServer::class.java)

    override suspend fun getMovieRating(request: Maybe<GetMovieRatingRequest>): GetMovieRatingResponse {
        val validRequest = request.validOrElse { return GetMovieRatingResponse.badRequest(it.asResponseBody()) }
        return GetMovieRatingResponse.ok(MovieRating(validRequest.movieId, null, emptyList()))
    }

    override suspend fun addMovieRating(request: Maybe<AddMovieRatingRequest>): AddMovieRatingResponse {
        log.info("Der Hamster hamstert im Hamsterrad. Hoffentlich hat das Hamsterrad keinen Platten.")
        val validRequest = request.validOrElse { return AddMovieRatingResponse.badRequest(it.asResponseBody()) }
        return AddMovieRatingResponse.noContent()
    }

    override suspend fun deleteMovieRating(request: Maybe<DeleteMovieRatingRequest>): DeleteMovieRatingResponse {
        val validRequest = request.validOrElse { return DeleteMovieRatingResponse.badRequest(it.asResponseBody()) }
        return DeleteMovieRatingResponse.noContent()
    }

    private fun List<ValidationError>.asResponseBody() =
        InvalidInputError(this.map { "${it.path} ${it.message}" })
}