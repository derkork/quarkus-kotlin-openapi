package com.ancientlightstudios.example.movie

import com.ancientlightstudios.example.movie.model.ApplicationError
import com.ancientlightstudios.example.movie.model.InvalidInputError
import com.ancientlightstudios.example.movie.model.Movie
import com.ancientlightstudios.example.movie.model.RatingDown
import com.ancientlightstudios.example.movie.server.*
import com.ancientlightstudios.example.rating.client.AddMovieRatingResponse
import com.ancientlightstudios.example.rating.client.DeleteMovieRatingResponse
import com.ancientlightstudios.example.rating.client.GetMovieRatingResponse
import com.ancientlightstudios.example.rating.client.RatingServiceClient
import com.ancientlightstudios.example.rating.model.RatingUp
import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import com.ancientlightstudios.quarkus.kotlin.openapi.RequestResult
import com.ancientlightstudios.quarkus.kotlin.openapi.ValidationError
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

@ApplicationScoped
class MovieServerImpl(val ratingClient: RatingServiceClient) : MovieServerDelegate {

    private val movieDataBase = mutableMapOf<String, Movie>()

    override suspend fun findMovies(request: Maybe<FindMoviesRequest>): FindMoviesResponse {
        val validRequest = request.validOrElse { return FindMoviesResponse.badRequest(it.asResponseBody()) }

        var filteredMovies = movieDataBase.values.toList()
        if (validRequest.title != null) {
            filteredMovies = filteredMovies.filter { it.title.contains(validRequest.title, true) }
        }
        if (validRequest.score != null) {
            filteredMovies = filteredMovies.filter { (it.totalScore ?: 0.0) >= validRequest.score }
        }
        if (!validRequest.genre.isNullOrEmpty()) {
            filteredMovies = filteredMovies.filter { it.genres.intersect(validRequest.genre).isNotEmpty() }
        }
        return FindMoviesResponse.ok(filteredMovies)
    }

    override suspend fun addMovie(request: Maybe<AddMovieRequest>): AddMovieResponse {
        val validRequest = request.validOrElse { return AddMovieResponse.badRequest(it.asResponseBody()) }

        val body = validRequest.body
        val newMovie = Movie(
            generateId(), body.title, body.releaseDate, body.genres, body.duration,
            body.cast, body.additionalInformation, null
        )
        movieDataBase[newMovie.id] = newMovie
        return AddMovieResponse.ok(newMovie)
    }

    override suspend fun getMovie(request: Maybe<GetMovieRequest>): GetMovieResponse {
        val validRequest = request.validOrElse { return GetMovieResponse.badRequest(it.asResponseBody()) }
        val movie = findMovieOrElse(validRequest.movieId) { return GetMovieResponse.notFound(it) }
        return GetMovieResponse.ok(movie)

    }

    override suspend fun deleteMovie(request: Maybe<DeleteMovieRequest>): DeleteMovieResponse {
        val validRequest = request.validOrElse { return DeleteMovieResponse.badRequest(it.asResponseBody()) }
        val movie = findMovieOrElse(validRequest.movieId) { return DeleteMovieResponse.notFound(it) }

        movieDataBase.remove(movie.id)
        return DeleteMovieResponse.noContent()
    }

    override suspend fun modifyMovie(request: Maybe<ModifyMovieRequest>): ModifyMovieResponse {
        val validRequest = request.validOrElse { return ModifyMovieResponse.badRequest(it.asResponseBody()) }
        val movie = findMovieOrElse(validRequest.movieId) { return ModifyMovieResponse.notFound(it) }

        val body = validRequest.body
        val newMovie = movie.copy(
            title = body.title,
            releaseDate = body.releaseDate,
            genres = body.genres,
            duration = body.duration,
            cast = body.cast,
            additionalInformation = body.additionalInformation
        )
        movieDataBase[newMovie.id] = newMovie
        return ModifyMovieResponse.ok(newMovie)
    }

    override suspend fun addRating(request: Maybe<AddRatingRequest>): AddRatingResponse {
        val validRequest = request.validOrElse { return AddRatingResponse.badRequest(it.asResponseBody()) }
        val movie = findMovieOrElse(validRequest.movieId) { return AddRatingResponse.notFound(it) }
        val body = validRequest.body

        val clientResponse = ratingClient.addMovieRating(movie.id, RatingUp(body.source, body.score))
                as? RequestResult.Response ?: return AddRatingResponse.badGateway(ApplicationError("scheisse passiert"))

        return when (clientResponse.response) {
            is AddMovieRatingResponse.NoContent -> AddRatingResponse.noContent()
            else -> AddRatingResponse.badGateway(ApplicationError("Noch mehr scheisse passiert"))
        }

    }

    override suspend fun getRatings(request: Maybe<GetRatingsRequest>): GetRatingsResponse {
        val validRequest = request.validOrElse { return GetRatingsResponse.badRequest(it.asResponseBody()) }
        findMovieOrElse(validRequest.movieId) { return GetRatingsResponse.notFound(it) }

        val clientResponse = ratingClient.getMovieRating(validRequest.movieId)
                as? RequestResult.Response
            ?: return GetRatingsResponse.badGateway(ApplicationError("scheisse passiert"))


        return when (val theResponse = clientResponse.response) {
            is GetMovieRatingResponse.Ok -> GetRatingsResponse.ok(
                theResponse.safeBody.ratings.map { RatingDown(it.id, it.source, it.score) }
            )

            is GetMovieRatingResponse.NotFound -> GetRatingsResponse.ok(emptyList())
            else -> GetRatingsResponse.badGateway(ApplicationError("Noch mehr scheisse passiert"))
        }

    }

    override suspend fun deleteRating(request: Maybe<DeleteRatingRequest>): DeleteRatingResponse {
        val validRequest = request.validOrElse { return DeleteRatingResponse.badRequest(it.asResponseBody()) }
        findMovieOrElse(validRequest.movieId) { return DeleteRatingResponse.noContent() }
        val ratingId = validRequest.ratingId

        val clientResponse = ratingClient.deleteMovieRating(validRequest.movieId, ratingId)
                as? RequestResult.Response
            ?: return DeleteRatingResponse.badGateway(ApplicationError("scheisse passiert"))

        return when (clientResponse.response) {
            is DeleteMovieRatingResponse.NoContent -> DeleteRatingResponse.noContent()
            else -> DeleteRatingResponse.badGateway(ApplicationError("Noch mehr scheisse passiert"))
        }
    }

    private inline fun findMovieOrElse(movieId: String, block: (ApplicationError) -> Nothing) =
        movieDataBase[movieId] ?: block(ApplicationError("movie with id $movieId not found"))

}

fun generateId() = UUID.randomUUID().toString()

fun List<ValidationError>.asResponseBody() =
    InvalidInputError(this.map { "'${it.path}': ${it.message}" })
