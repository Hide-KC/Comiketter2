package com.kc.comiketter2

import com.kc.comiketter2.util.extractor.CircleSpaceExtractor
import com.kc.comiketter2.util.extractor.Extractor
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CircleSpaceExtractorTest {
  private val extractor: Extractor = CircleSpaceExtractor()
  private val testCases = mapOf(
    "1日目東A01a hoge" to "A01a",
    "1日目東A 01a hoge" to "A01a",
    "1日目東Ａ０１ａ hoge" to "A01a",
    "1日目東Ａ01a hoge" to "A01a",
    "1日目東Ａ ０１ａ hoge" to "A01a",
    "1日目東\"Ａ\" ０１ａ hoge" to "A01a",
    "1日目東Ａ - ０１ａ hoge" to "A01a",
    "1日目東Ａ-０１ａ hoge" to "A01a",
    "2日目西あ99a" to "あ99a",
    "3日目北あ０１ａｂ" to "あ01ab",
    "C97-12/31火4日目南3,4ホール-ヨ46b" to "ヨ46b"
  )

  @Test
  fun extractCircleSpace() {
    for (testCase in testCases.keys) {
      println(testCase)
      val space = extractor.extract(testCase)
      assertThat(space, `is`(testCases[testCase]))
    }
  }
}