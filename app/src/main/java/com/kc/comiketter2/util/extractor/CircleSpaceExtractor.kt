package com.kc.comiketter2.util.extractor

import java.text.Normalizer

class CircleSpaceExtractor : Extractor {
  override fun extract(arg: String): String? {
    val normalized = Normalizer.normalize(arg, Normalizer.Form.NFKC)
    val reg = Regex("""[A-Zあ-んア-ン] ?[-"]? ?[0-9][0-9](ab|a|b)""")
    val match = reg.find(normalized, 0)?.value
    val rep = match?.replace(Regex("""[- "]"""), "")
    println(rep)
    return rep
  }
}