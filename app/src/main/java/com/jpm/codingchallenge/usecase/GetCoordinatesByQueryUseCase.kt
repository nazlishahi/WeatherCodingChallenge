package com.jpm.codingchallenge.usecase

import com.jpm.codingchallenge.data.City
import com.jpm.codingchallenge.data.Repository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetCoordinatesByQueryUseCase @Inject constructor(
    private val repository: Repository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        query: String
    ): List<City> = withContext(ioDispatcher) {
        repository.getCoordinatesByQuery(query)
    }
}