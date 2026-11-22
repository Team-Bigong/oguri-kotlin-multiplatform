package com.bigong.oguri.repository

import com.bigong.oguri.domain.TravelCatalogCity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TravelCatalogCityRepository : JpaRepository<TravelCatalogCity, Long> {
    @Query(
        nativeQuery = true,
        value = """
            SELECT c.*
            FROM cities c
            JOIN (
                SELECT id, bool_or(exact_match) AS exact_match
                FROM (
                    SELECT id, lower(name) = lower(:query) AS exact_match
                    FROM cities WHERE lower(name) LIKE lower(:prefix) ESCAPE '!'
                    UNION ALL
                    SELECT city_id, lower(name) = lower(:query)
                    FROM city_aliases WHERE lower(name) LIKE lower(:prefix) ESCAPE '!'
                ) candidates
                GROUP BY id
            ) matches ON matches.id = c.id
            ORDER BY matches.exact_match DESC, c.population DESC, c.name, c.id
            LIMIT 10
        """,
    )
    fun search(
        query: String,
        prefix: String,
    ): List<TravelCatalogCity>
}

internal fun TravelCatalogCityRepository.resolveCityReferences(
    references: Map<String, Long>,
    retained: Map<String, TravelCatalogCity> = emptyMap(),
): Map<String, TravelCatalogCity> {
    require(references.size <= 10 && references.values.toSet().size == references.size)
    if (references.isEmpty()) return emptyMap()
    val cities = findAllById(references.values).associateBy(TravelCatalogCity::id)
    return references.mapValues { (label, id) ->
        val city = requireNotNull(cities[id])
        require(label == city.qualifiedLabel() || retained[label]?.id == id)
        city
    }
}
