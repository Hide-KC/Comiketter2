package com.kc.comiketter2

object LogDecorator {
  private const val ANSI_RESET = "\u001B[0m"
  private const val ANSI_BLACK = "\u001B[30m"
  private const val ANSI_RED = "\u001B[31m"
  private const val ANSI_GREEN = "\u001B[32m"
  private const val ANSI_YELLOW = "\u001B[33m"
  private const val ANSI_BLUE = "\u001B[34m"
  private const val ANSI_PURPLE = "\u001B[35m"
  private const val ANSI_CYAN = "\u001B[36m"
  private const val ANSI_WHITE = "\u001B[37m"  

  fun decoBlack(arg: String): String {
    return ANSI_BLACK + arg + ANSI_RESET
  }

  fun decoRed(arg: String): String {
    return ANSI_RED + arg + ANSI_RESET
  }

  fun decoGreen(arg: String): String {
    return ANSI_GREEN + arg + ANSI_RESET
  }

  fun decoYellow(arg: String): String {
    return ANSI_YELLOW + arg + ANSI_RESET
  }

  fun decoBlue(arg: String): String {
    return ANSI_BLUE + arg + ANSI_RESET
  }

  fun decoPurple(arg: String): String {
    return ANSI_PURPLE + arg + ANSI_RESET
  }

  fun decoCyan(arg: String): String {
    return ANSI_CYAN + arg + ANSI_RESET
  }

  fun decoWhite(arg: String): String {
    return ANSI_WHITE + arg + ANSI_RESET
  }
}
