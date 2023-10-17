package com.ancientlightstudios.example

import com.ancientlightstudios.example.model.*
import com.ancientlightstudios.example.server.*
import com.ancientlightstudios.example.server.NarfInterfaceDelegate
import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

@ApplicationScoped
class NarfDelegateImpl : NarfInterfaceDelegate {

    private val movieDataBase = mutableMapOf<Id, Movie>()

    override suspend fun findMovies(request: Maybe<FindMoviesRequest>): FindMoviesResponse {
        val validRequest = request.validOrElse { return FindMoviesResponse.badRequest(it.asResponseBody()) }

        var filteredMovies = movieDataBase.values.toList()
        if (validRequest.title != null) {
            filteredMovies = filteredMovies.filter { it.title.contains(validRequest.title, true) }
        }
        if (validRequest.score != null) {
            filteredMovies = filteredMovies.filter { (it.totalScore?.value ?: 0.0) >= validRequest.score.value }
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
            body.cast, body.additionalInformation, null, emptyList()
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
        val newRatings = movie.ratings.filterNot { it.source == body.source } + body

        val newMovie = movie.copy(totalScore = calculateScore(newRatings), ratings = newRatings)
        movieDataBase[newMovie.id] = newMovie
        return AddRatingResponse.ok(newRatings)
    }

    override suspend fun setRatings(request: Maybe<SetRatingsRequest>): SetRatingsResponse {
        val validRequest = request.validOrElse { return SetRatingsResponse.badRequest(it.asResponseBody()) }
        val movie = findMovieOrElse(validRequest.movieId) { return SetRatingsResponse.notFound(it) }

        val body = validRequest.body

        val newMovie = movie.copy(totalScore = calculateScore(body), ratings = body)
        movieDataBase[newMovie.id] = newMovie
        return SetRatingsResponse.ok(body)
    }

    private inline fun findMovieOrElse(movieId: Id, block: (ApplicationError) -> Nothing) =
        movieDataBase[movieId] ?: block(ApplicationError("movie with id ${movieId.value} not found"))

    private fun calculateScore(ratings: List<Rating>): Score? {
        if (ratings.isEmpty()) {
            return null
        }
        return Score(ratings.sumOf { it.score.value } / ratings.size)
    }
}

fun generateId() = Id(UUID.randomUUID().toString())

fun List<com.ancientlightstudios.quarkus.kotlin.openapi.ValidationError>.asResponseBody() =
    ValidationError(this.map { "'${it.path}': ${it.message}" })
