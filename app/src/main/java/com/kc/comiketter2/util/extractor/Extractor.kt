package com.kc.comiketter2.util.extractor

interface Extractor {
  fun extract(arg: String): String?
}