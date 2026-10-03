package com.resonix.media.core.media.network.opensubtitles

import org.junit.Assert.assertEquals
import org.junit.Test

class OpenSubtitlesApiTest {

    private val api = OpenSubtitlesApi()

    @Test
    fun `search path encodes query and appends optional filters`() {
        val path = api.searchPath(
            query = "avengers endgame",
            languages = "eng",
            imdbId = 4154796,
            year = 2019,
            season = null,
            episode = null,
        )

        assertEquals("/subtitles?query=avengers+endgame&languages=eng&imdb_id=4154796&year=2019", path)
    }

    @Test
    fun `search path omits unset filters`() {
        val path = api.searchPath(
            query = "matrix",
            languages = "ind",
            imdbId = null,
            year = null,
            season = 2,
            episode = 5,
        )

        assertEquals(
            "/subtitles?query=matrix&languages=ind&season_number=2&episode_number=5",
            path,
        )
    }

    @Test
    fun `parse search response maps attributes`() {
        val json = """
            {
              "data": [
                {
                  "id": "22052710",
                  "type": "subtitle",
                  "attributes": {
                    "subtitle_id": "22052710",
                    "language": "eng",
                    "release": "WEB-DL",
                    "files": [
                      {"file_id": 22052710, "file_name": "The.Matrix.1999.1080p.BluRay.x264-YIFY.srt"}
                    ],
                    "feature_details": {
                      "year": 1999,
                      "title": "The Matrix",
                      "movie_name": "The Matrix",
                      "imdb_id": 905
                    },
                    "hearing_impaired": false
                  }
                }
              ]
            }
        """.trimIndent()

        val results = api.parseSearchResponse(json)

        assertEquals(1, results.size)
        val result = results.single()
        assertEquals("22052710", result.id)
        assertEquals("eng", result.attributes.language)
        assertEquals(22052710L, result.attributes.files.single().fileId)
        assertEquals(1999, result.attributes.featureDetails.year)
        assertEquals(
            "The.Matrix.1999.1080p.BluRay.x264-YIFY.srt",
            result.displayName,
        )
    }

    @Test
    fun `parse search response tolerates unknown keys`() {
        val json = """
            {
              "data": [
                {
                  "id": "1",
                  "attributes": {
                    "language": "spa",
                    "files": [{"file_id": 1}],
                    "unknown_future_field": {"a": 1}
                  }
                }
              ]
            }
        """.trimIndent()

        val results = api.parseSearchResponse(json)

        assertEquals("spa", results.single().attributes.language)
    }

    @Test
    fun `display name falls back to feature title then release`() {
        val withTitle = OpenSubtitlesSearchResult(
            attributes = OpenSubtitlesAttributes(
                language = "eng",
                release = "BluRay",
                featureDetails = OpenSubtitlesFeatureDetails(title = "The Matrix"),
            ),
        )
        val withReleaseOnly = OpenSubtitlesSearchResult(
            attributes = OpenSubtitlesAttributes(language = "eng", release = "BluRay"),
        )
        val empty = OpenSubtitlesSearchResult()

        assertEquals("The Matrix", withTitle.displayName)
        assertEquals("BluRay", withReleaseOnly.displayName)
        assertEquals("subtitle", empty.displayName)
    }

    @Test
    fun `language conversion maps iso codes and passes unknown through`() {
        assertEquals("eng", "en".toOpenSubtitlesLanguage())
        assertEquals("ind", "id".toOpenSubtitlesLanguage())
        assertEquals("spa", "es".toOpenSubtitlesLanguage())
        assertEquals("xyz", "xyz".toOpenSubtitlesLanguage())
        assertEquals("eng", "eng".toOpenSubtitlesLanguage())
    }
}
