package com.resonix.media.core.media.network.opensubtitles

/** ISO 639-1 to OpenSubtitles three-letter tags for the most common languages. */
private val OPEN_SUBTITLES_LANGUAGES: Map<String, String> = mapOf(
    "af" to "afr", "am" to "amh", "ar" to "ara", "az" to "aze",
    "be" to "bel", "bg" to "bul", "bn" to "ben", "bs" to "bos",
    "ca" to "cat", "cs" to "cze", "da" to "dan", "de" to "ger",
    "el" to "gre", "en" to "eng", "eo" to "epo", "es" to "spa",
    "et" to "est", "eu" to "baq", "fa" to "per", "fi" to "fin",
    "fr" to "fre", "gl" to "glg", "gu" to "guj", "he" to "heb",
    "hi" to "hin", "hr" to "hrv", "hu" to "hun", "id" to "ind",
    "it" to "ita", "ja" to "jpn", "ka" to "geo", "km" to "khm",
    "kn" to "kan", "ko" to "kor", "lt" to "lit", "lv" to "lav",
    "mk" to "mac", "ml" to "mal", "mr" to "mar", "ms" to "may",
    "my" to "bur", "ne" to "nep", "nl" to "dut", "no" to "nor",
    "pa" to "pan", "pl" to "pol", "pt" to "por", "ro" to "rum",
    "ru" to "rus", "si" to "sin", "sk" to "slo", "sl" to "slv",
    "sq" to "alb", "sr" to "srp", "su" to "sun", "sv" to "swe",
    "sw" to "swa", "ta" to "tam", "te" to "tel", "th" to "tha",
    "tl" to "tgl", "tr" to "tur", "uk" to "ukr", "ur" to "urd",
    "uz" to "uzb", "vi" to "vie", "zh" to "chi", "zu" to "zul",
)

/** Converts an ISO 639-1 code (`en`) to an OpenSubtitles tag (`eng`). Unknown codes pass through. */
fun String.toOpenSubtitlesLanguage(): String = OPEN_SUBTITLES_LANGUAGES[this] ?: this
