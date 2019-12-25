package com.kc.comiketter2

import com.kc.comiketter2.util.extractor.ComikeDateExtractor
import com.kc.comiketter2.util.extractor.Extractor
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ComikeDateExtractorTest {
  private val extractor: Extractor = ComikeDateExtractor()
  private val testCases = mapOf(
    "1日目東A01a hoge" to "C97 Day 1",
    "１日目東A 01a hoge" to "C97 Day 1",
    "12/28東Ａ０１ａ hoge" to "C97 Day 1",
    "12月28日東Ａ01a hoge" to "C97 Day 1",
    "C97(土)東Ａ ０１ａ hoge" to "C97 Day 1",
    "C97（土）1日目東\"Ａ\" ０１ａ hoge" to "C97 Day 1",
    "（土）東Ａ - ０１ａ hoge" to "C97 Day 1",
    "土曜東Ａ - ０１ａ hoge" to "C97 Day 1",
    "28日東Ａ-０１ａ hoge" to "C97 Day 1",
    "C97西あ99a" to "C97",
    "Ｃ９７西あ99a" to "C97",
    "西あ99a" to null,
    "3日目北あ０１ａｂ" to "C97 Day 3",
    "C97-12/31火4日目南3,4ホール-ヨ46b" to "C97 Day 4"
  )

  @Test
  fun extractComikeDate() {
    for (testCase in testCases.keys) {
      val space = extractor.extract(testCase)
      assertThat(space, CoreMatchers.`is`(testCases[testCase]))
    }
  }
}