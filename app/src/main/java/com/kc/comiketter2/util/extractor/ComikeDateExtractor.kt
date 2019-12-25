package com.kc.comiketter2.util.extractor

import java.text.Normalizer

const val comike = "C97"

class ComikeDateExtractor : Extractor {
  private val comikeNameRegex = Regex("""C(9[7-9]|1[0-9][0-9])""")
  private val comikeDateRegex =
    Regex(
      """[1-4]日目|12[/月](2[89]|3[01])日?|(2[89]|3[01])日|\([土日月火]\)|[土日月火]曜"""
    )
  private val comikeDateMap = mapOf(
    Regex("""1日目|12[/月]28日?|28日|\(土\)|土曜""") to "$comike Day 1",
    Regex("""2日目|12[/月]29日?|29日|\(日\)|日曜""") to "$comike Day 2",
    Regex("""3日目|12[/月]30日?|30日|\(月\)|月曜""") to "$comike Day 3",
    Regex("""4日目|12[/月]31日?|31日|\(火\)|火曜""") to "$comike Day 4"
  )

  override fun extract(arg: String): String? {
    val normalized = Normalizer.normalize(arg, Normalizer.Form.NFKC)
    println("Normalized: $normalized")
    val hasComike = comikeNameRegex.find(normalized)
    val extractDate = comikeDateRegex.find(normalized)

    if (hasComike == null && extractDate == null) {
      println("out: null")
      return null
    } else if (hasComike != null && extractDate == null) {
      println("out: $comike")
      return comike
    } else if (extractDate != null) {
      val matchDate = extractDate.value
      println("match: $matchDate")

      for (comikeDate in comikeDateMap.keys) {
        if (comikeDate.matches(matchDate)) {
          println("out: ${comikeDateMap[comikeDate]}")
          return comikeDateMap[comikeDate]
        }
      }
      return comike
    } else {
      return null
    }
  }
}